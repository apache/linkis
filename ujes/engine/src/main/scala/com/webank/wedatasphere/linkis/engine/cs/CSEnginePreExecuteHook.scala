/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.cs

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.cs.client.utils.ContextServiceUtils
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils
import com.webank.wedatasphere.linkis.engine.PropertiesExecuteRequest
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.extension.EnginePreExecuteHook
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest

/**
  * @author peacewong
  * @date 2020/3/5 17:13
  */
class CSEnginePreExecuteHook extends EnginePreExecuteHook with Logging {

  private val csResourceParser: CSResourceParser = new CSResourceParser

  override val hookName: String = "ContextServicePreHook"


  override def callPreExecuteHook(engineExecutorContext: EngineExecutorContext, executeRequest: ExecuteRequest, code: String): String = executeRequest match {
    case propertiesExecuteRequest: PropertiesExecuteRequest =>
      var parsedCode = code
      val contextIDValueStr = ContextServiceUtils.getContextIDStrByMap(propertiesExecuteRequest.properties)
      val nodeNameStr = ContextServiceUtils.getNodeNameStrByMap(propertiesExecuteRequest.properties)
      engineExecutorContext.addProperty(CSCommonUtils.CONTEXT_ID_STR, contextIDValueStr)
      engineExecutorContext.addProperty(CSCommonUtils.NODE_NAME_STR, nodeNameStr)
      info(s"Start to call cs engine pre hook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr")
      parsedCode = csResourceParser.parse(propertiesExecuteRequest, parsedCode, contextIDValueStr, nodeNameStr)

      info(s"Finished to call cs engine pre hook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr")
      parsedCode
    case _ => code
  }
}
