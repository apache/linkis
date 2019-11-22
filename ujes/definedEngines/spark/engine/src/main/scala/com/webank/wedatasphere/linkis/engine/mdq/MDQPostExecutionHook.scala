/*
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
package com.webank.wedatasphere.linkis.engine.mdq

import javax.annotation.PostConstruct

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.extension.SparkPostExecutionHook
import com.webank.wedatasphere.linkis.engine.spark.common.SparkKind
import com.webank.wedatasphere.linkis.protocol.mdq.{DDLCompleteResponse, DDLExecuteResponse}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component


@Component
class MDQPostExecutionHook extends SparkPostExecutionHook with Logging{

  @PostConstruct
  def  init(): Unit ={
    SparkPostExecutionHook.register(this)
  }

  override def hookName: String = "MDQPostHook"

  override def callPostExecutionHook(engineExecutorContext: EngineExecutorContext, executeResponse: ExecuteResponse, code: String): Unit = {
    val runType: String = engineExecutorContext.getProperties.get("runType") match {
      case value:String => value
      case _ => ""
    }
    if(StringUtils.isEmpty(runType) || ! SparkKind.FUNCTION_MDQ_TYPE.equalsIgnoreCase(runType)) return
    val sender = Sender.getSender(SparkConfiguration.MDQ_APPLICATION_NAME.getValue)
    executeResponse match {
      case SuccessExecuteResponse() =>
        sender.ask(DDLExecuteResponse(true, code, StorageUtils.getJvmUser)) match {
          case DDLCompleteResponse(status) => if (! status)
            warn(s"执行建表失败:$code")
        }
      case _=> warn(s"执行建表失败:$code")
    }
  }
}
