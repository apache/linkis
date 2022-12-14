/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineplugin.elasticsearch.executor.client.impl

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration
import org.apache.linkis.engineplugin.elasticsearch.exception.EsConvertResponseException
import org.apache.linkis.engineplugin.elasticsearch.executor.client.{
  ElasticSearchErrorResponse,
  ElasticSearchExecutor,
  ElasticSearchResponse,
  EsClient,
  EsClientFactory,
  ResponseHandler
}
import org.apache.linkis.engineplugin.elasticsearch.executor.client.ResponseHandler
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.executer.{
  AliasOutputExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}
import org.apache.linkis.server.JMap
import org.apache.linkis.storage.utils.StorageUtils

import java.util
import java.util.Locale
import java.util.concurrent.CountDownLatch

import org.elasticsearch.client.{Cancellable, Response, ResponseListener}

class ElasticSearchExecutorImpl(runType: String, properties: util.Map[String, String])
    extends ElasticSearchExecutor {

  private var client: EsClient = _
  private var cancelable: Cancellable = _
  private var user: String = _

  override def open: Unit = {
    this.client = EsClientFactory.getRestClient(properties)
    this.user = properties.getOrDefault(TaskConstant.UMUSER, StorageUtils.getJvmUser)
    runType.trim.toLowerCase(Locale.getDefault) match {
      case "essql" | "sql" =>
        properties.putIfAbsent(
          ElasticSearchConfiguration.ES_HTTP_ENDPOINT.key,
          ElasticSearchConfiguration.ES_HTTP_SQL_ENDPOINT.getValue(properties)
        )
      case _ =>
    }
  }

  override def executeLine(code: String): ElasticSearchResponse = {
    val realCode = code.trim()
    logger.info(s"es client begins to run $runType code:\n ${realCode.trim}")
    val countDown = new CountDownLatch(1)
    var executeResponse: ElasticSearchResponse = ElasticSearchErrorResponse("INCOMPLETE")
    cancelable = client.execute(
      realCode,
      properties,
      new ResponseListener {
        override def onSuccess(response: Response): Unit = {
          executeResponse = convertResponse(response)
          countDown.countDown()
        }
        override def onFailure(exception: Exception): Unit = {
          executeResponse =
            ElasticSearchErrorResponse("EsEngineExecutor execute fail. ", null, exception)
          countDown.countDown()
        }
      }
    )
    countDown.await()
    executeResponse
  }

  // convert response to executeResponse
  private def convertResponse(response: Response): ElasticSearchResponse =
    Utils.tryCatch[ElasticSearchResponse] {
      val statusCode = response.getStatusLine.getStatusCode
      if (statusCode >= 200 && statusCode < 300) {
        ResponseHandler.handle(response)
      } else {
        ElasticSearchErrorResponse(
          "EsEngineExecutor convert response fail. response code: " + response.getStatusLine.getStatusCode
        )
      }
    } { case t: Throwable =>
      ElasticSearchErrorResponse("EsEngineExecutor convert response error.", null, t)
    }

  override def close: Unit = cancelable match {
    case c: Cancellable => c.cancel()
    case _ =>
  }

}
