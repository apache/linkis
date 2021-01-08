/**
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.entrance.executor.impl

import java.util.concurrent.CountDownLatch

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.conf.EsEntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.EsConvertResponseException
import com.webank.wedatasphere.linkis.entrance.executor.codeparser.CodeParser
import com.webank.wedatasphere.linkis.entrance.executor.esclient.EsClient
import com.webank.wedatasphere.linkis.entrance.executor.{EsEngineExecutor, ResponseHandler}
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.server.JMap
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.elasticsearch.client.{Cancellable, Response, ResponseListener}

/**
 *
 * @author wang_zh
 * @date 2020/5/11
 */
class EsEngineExecutorImpl(runType:String, client: EsClient, properties: JMap[String, String]) extends EsEngineExecutor {

  private var cancelable: Cancellable = _
  private var codeParser: CodeParser = _
  private var user: String = _

  override def open: Unit = {
    this.codeParser = runType.trim.toLowerCase match {
      case "esjson" | "json" => CodeParser.ESJSON_CODE_PARSER
      case "essql" | "sql" => {
        // set default sql endpoint
        properties.putIfAbsent(EsEntranceConfiguration.ES_HTTP_ENDPOINT.key, EsEntranceConfiguration.ES_HTTP_SQL_ENDPOINT.getValue)
        CodeParser.ESSQL_CODE_PARSER
      }
      case _ => CodeParser.ESJSON_CODE_PARSER
    }
    this.user = properties.getOrDefault(TaskConstant.UMUSER, StorageUtils.getJvmUser)
  }

  override def parse(code: String): Array[String] = {
    codeParser.parse(code)
  }

  override def executeLine(code: String, storePath: String, alias: String): ExecuteResponse = {
    val realCode = code.trim()
    info(s"es client begins to run $runType code:\n ${realCode.trim}")
    val countDown = new CountDownLatch(1)
    var executeResponse: ExecuteResponse  = SuccessExecuteResponse()
    cancelable = client.execute(realCode, properties, new ResponseListener {
      override def onSuccess(response: Response): Unit = {
        executeResponse = convertResponse(response, storePath, alias)
        countDown.countDown()
      }
      override def onFailure(exception: Exception): Unit = {
        executeResponse = ErrorExecuteResponse("EsEngineExecutor execute fail. ", exception)
        countDown.countDown()
      }
    })
    countDown.await()
    executeResponse
  }

  // convert response to executeResponse
  private def convertResponse(response: Response, storePath: String, alias: String): ExecuteResponse =  Utils.tryCatch[ExecuteResponse]{
    val statusCode = response.getStatusLine.getStatusCode
    if (statusCode >= 200 && statusCode < 300) {
      val output = ResponseHandler.RESPONSE_HANDLER.handle(response, storePath, alias, this.user)
      AliasOutputExecuteResponse(alias, output)
    } else {
      throw EsConvertResponseException("EsEngineExecutor convert response fail. response code: " + response.getStatusLine.getStatusCode)
    }
  } {
    case t: Throwable => ErrorExecuteResponse("EsEngineExecutor execute fail.", t)
  }

  override def close: Unit = cancelable match {
    case c: Cancellable => c.cancel()
    case _ =>
  }

}