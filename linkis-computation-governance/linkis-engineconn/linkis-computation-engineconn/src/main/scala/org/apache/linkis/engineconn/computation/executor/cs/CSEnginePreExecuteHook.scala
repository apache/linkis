/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.computation.executor.cs

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.cs.client.utils.ContextServiceUtils
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineconn.computation.executor.utlis.ComputationEngineConstant


class CSEnginePreExecuteHook extends ComputationExecutorHook with Logging {

  private val csResourceParser: CSResourceParser = new CSResourceParser

  override def getHookName: String = "ContextServicePreHook"

  override def getOrder(): Int = ComputationEngineConstant.CS_HOOK_ORDER

  override def beforeExecutorExecute(engineExecutionContext: EngineExecutionContext, engineCreationContext: EngineCreationContext, code: String): String = {
    val props = engineExecutionContext.getProperties
    if (null != props && props.containsKey(CSCommonUtils.CONTEXT_ID_STR)) {
        var parsedCode = code
        val contextIDValueStr = ContextServiceUtils.getContextIDStrByMap (props)
        val nodeNameStr = ContextServiceUtils.getNodeNameStrByMap (props)
        engineExecutionContext.addProperty (CSCommonUtils.CONTEXT_ID_STR, contextIDValueStr)
        engineExecutionContext.addProperty (CSCommonUtils.NODE_NAME_STR, nodeNameStr)
        info (s"Start to call cs engine pre hook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr")
        parsedCode = csResourceParser.parse (props, parsedCode, contextIDValueStr, nodeNameStr)
        info (s"Finished to call cs engine pre hook,contextID is $contextIDValueStr, nodeNameStr is $nodeNameStr")
        parsedCode
    } else {
      code
    }
  }


}
