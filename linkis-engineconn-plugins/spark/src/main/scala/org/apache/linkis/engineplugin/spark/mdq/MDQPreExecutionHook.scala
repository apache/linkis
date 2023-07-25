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

package org.apache.linkis.engineplugin.spark.mdq

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.common.SparkKind
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary._
import org.apache.linkis.engineplugin.spark.exception.MDQErrorException
import org.apache.linkis.engineplugin.spark.extension.SparkPreExecutionHook
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.protocol.mdq.{DDLRequest, DDLResponse}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.storage.utils.StorageUtils

import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

import javax.annotation.PostConstruct

import java.util

@Component
class MDQPreExecutionHook extends SparkPreExecutionHook with Logging {

  @PostConstruct
  def init(): Unit = {
    SparkPreExecutionHook.register(this)
  }

  override def hookName: String = "MDQPreHook"

  override def callPreExecutionHook(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): String = {

    val codeLanguageLabel = engineExecutionContext.getLabels
      .filter(l => null != l && l.isInstanceOf[CodeLanguageLabel])
      .head
    val runType: String = codeLanguageLabel match {
      case l: CodeLanguageLabel =>
        l.getCodeType
      case _ =>
        ""
    }
    if (
        StringUtils.isEmpty(runType) || !SparkKind.FUNCTION_MDQ_TYPE.equalsIgnoreCase(
          runType
        ) || (code != null && code.contains(SparkConfiguration.SCALA_PARSE_APPEND_CODE))
    ) {
      return code
    }
    val sender = Sender.getSender(SparkConfiguration.MDQ_APPLICATION_NAME.getValue)
    val params = new util.HashMap[String, Object]()
    params.put("user", StorageUtils.getJvmUser)
    params.put("code", code)
    var resp: Any = null
    Utils.tryCatch {
      resp = sender.ask(DDLRequest(params))
    } { case e: Exception =>
      logger.error(s"Call MDQ rpc failed, ${e.getMessage}", e)
      throw new MDQErrorException(
        REQUEST_MDQ_FAILED.getErrorCode,
        REQUEST_MDQ_FAILED.getErrorDesc + s", ${e.getMessage}"
      )
    }
    resp match {
      case DDLResponse(postCode) => postCode
      case _ =>
        throw new MDQErrorException(
          REQUEST_MDQ_FAILED.getErrorCode,
          REQUEST_MDQ_FAILED.getErrorDesc
        )
    }
  }

}
