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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.common.SparkKind
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.extension.SparkPostExecutionHook
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.protocol.mdq.{DDLCompleteResponse, DDLExecuteResponse}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}
import org.apache.linkis.storage.utils.StorageUtils

import org.apache.commons.lang3.StringUtils

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class MDQPostExecutionHook extends SparkPostExecutionHook with Logging {

  @PostConstruct
  def init(): Unit = {
    SparkPostExecutionHook.register(this)
  }

  override def hookName: String = "MDQPostHook"

  override def callPostExecutionHook(
      engineExecutionContext: EngineExecutionContext,
      executeResponse: ExecuteResponse,
      code: String
  ): Unit = {
    val codeLanguageLabel = engineExecutionContext.getLabels
      .filter(l => null != l && l.isInstanceOf[CodeLanguageLabel])
      .head
    val runType: String = codeLanguageLabel match {
      case l: CodeLanguageLabel => l.getCodeType
      case _ => ""
    }
    if (
        StringUtils.isEmpty(runType) || !SparkKind.FUNCTION_MDQ_TYPE.equalsIgnoreCase(
          runType
        ) || (code != null && code.contains(SparkConfiguration.SCALA_PARSE_APPEND_CODE))
    ) {
      return
    }
    val sender = Sender.getSender(SparkConfiguration.MDQ_APPLICATION_NAME.getValue)
    executeResponse match {
      case SuccessExecuteResponse() =>
        sender.ask(DDLExecuteResponse(true, code, StorageUtils.getJvmUser)) match {
          case DDLCompleteResponse(status) =>
            if (!status) {
              logger.warn(s"failed to execute create table :$code (执行建表失败):$code")
            }
        }
      case _ => logger.warn(s"failed to execute create table:$code (执行建表失败:$code)")
    }
  }

}
