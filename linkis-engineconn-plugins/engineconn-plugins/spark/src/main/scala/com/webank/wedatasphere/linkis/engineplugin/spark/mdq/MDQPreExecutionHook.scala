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
package com.webank.wedatasphere.linkis.engineplugin.spark.mdq

import java.util
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineplugin.spark.common.SparkKind
import com.webank.wedatasphere.linkis.engineplugin.spark.config.SparkConfiguration
import com.webank.wedatasphere.linkis.engineplugin.spark.exception.MDQErrorException
import com.webank.wedatasphere.linkis.engineplugin.spark.extension.SparkPreExecutionHook
import com.webank.wedatasphere.linkis.manager.label.entity.engine.CodeLanguageLabel
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil
import com.webank.wedatasphere.linkis.protocol.mdq.{DDLRequest, DDLResponse}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils

import javax.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils


@Component
class MDQPreExecutionHook extends SparkPreExecutionHook with Logging {

  @PostConstruct
  def  init(): Unit ={
    SparkPreExecutionHook.register(this)
  }

  override def hookName: String = "MDQPreHook"

  override def callPreExecutionHook(engineExecutionContext: EngineExecutionContext, code: String): String = {

    val codeLanguageLabel = engineExecutionContext.getLabels.filter(l => null != l && l.isInstanceOf[CodeLanguageLabel]).head
    val runType: String = codeLanguageLabel match {
      case l: CodeLanguageLabel =>
        l.getCodeType
      case _ =>
        ""
    }
    if(StringUtils.isEmpty(runType) || ! SparkKind.FUNCTION_MDQ_TYPE.equalsIgnoreCase(runType)) return code
    val sender = Sender.getSender(SparkConfiguration.MDQ_APPLICATION_NAME.getValue)
    val params = new util.HashMap[String,Object]()
    params.put("user", StorageUtils.getJvmUser)
    params.put("code", code)
    var resp: Any = null
    Utils.tryCatch {
      resp = sender.ask(DDLRequest(params))
    } {
      case e: Exception =>
        error(s"Call MDQ rpc failed, ${e.getMessage}", e)
        throw new MDQErrorException(40010, s"向MDQ服务请求解析为可以执行的sql时失败, ${e.getMessage}")
    }
    resp match {
      case DDLResponse(postCode) => postCode
      case _ => throw new MDQErrorException(40010, "The request to the MDQ service failed to resolve into executable SQL(向MDQ服务请求解析为可以执行的sql时失败)")
    }
  }
}
