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

import java.util
import javax.annotation.PostConstruct

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration
import com.webank.wedatasphere.linkis.engine.exception.MDQErrorException
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.extension.SparkPreExecutionHook
import com.webank.wedatasphere.linkis.engine.spark.common.SparkKind
import com.webank.wedatasphere.linkis.protocol.mdq.{DDLRequest, DDLResponse}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component


@Component
class MDQPreExecutionHook extends SparkPreExecutionHook with Logging{

  @PostConstruct
  def  init(): Unit ={
    SparkPreExecutionHook.register(this)
  }

  override def hookName: String = "MDQPreHook"

  override def callPreExecutionHook(engineExecutorContext: EngineExecutorContext, code: String): String = {

    val runType: String = engineExecutorContext.getProperties.get("runType") match {
      case value:String => value
      case _ => ""
    }
    if(StringUtils.isEmpty(runType) || ! SparkKind.FUNCTION_MDQ_TYPE.equalsIgnoreCase(runType)) return code
    val sender = Sender.getSender(SparkConfiguration.MDQ_APPLICATION_NAME.getValue)
    val params = new util.HashMap[String,Object]()
    params.put("user", StorageUtils.getJvmUser)
    params.put("code", code)
    sender.ask(DDLRequest(params)) match {
      case DDLResponse(postCode) => postCode
      case _ => throw new MDQErrorException(40010, "向MDQ服务请求解析为可以执行的sql时失败")
    }
  }
}
