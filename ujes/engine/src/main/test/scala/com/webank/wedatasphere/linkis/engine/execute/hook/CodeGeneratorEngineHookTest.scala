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

package com.webank.wedatasphere.linkis.engine.execute.hook

import java.io.File
import java.util

import com.google.common.io.Resources
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext}
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import com.webank.wedatasphere.linkis.resourcemanager.Resource
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteRequest, ExecuteResponse}
import org.apache.commons.io.FileUtils

object CodeGeneratorEngineHookTest {
  def main(args: Array[String]): Unit = {
    val requestEngine = new TestRequestEngine
    requestEngine.properties.put(RequestEngine.ENGINE_INIT_SPECIAL_CODE,
      FileUtils.readFileToString(new File(Resources.getResource("engine_special_code").getPath)))
    val engineExecutor = new TestEngineExecutor(1, true)

    var engineHook: CodeGeneratorEngineHook = new SqlCodeGeneratorEngineHook
    engineHook.beforeCreateEngine(new util.HashMap(requestEngine.properties))
    engineHook.afterCreatedEngine(engineExecutor)
    engineHook = new PythonCodeGeneratorEngineHook
    engineHook.beforeCreateEngine(new util.HashMap(requestEngine.properties))
    engineHook.afterCreatedEngine(engineExecutor)
    engineHook = new ScalaCodeGeneratorEngineHook
    engineHook.beforeCreateEngine(new util.HashMap(requestEngine.properties))
    engineHook.afterCreatedEngine(engineExecutor)

  }
}
class TestRequestEngine extends RequestEngine {
  override val user: String = ""
  override val properties: util.Map[String, String] = new util.HashMap[String, String](){

  }
  override val creator: String = ""
}
class TestEngineExecutor(outputPrintLimit: Int, isSupportParallelism: Boolean) extends EngineExecutor(outputPrintLimit, isSupportParallelism){

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    null
  }

  override def getName: String = ""

  override def getActualUsedResources: Resource = null

  override protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = null

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = null

  override def close(): Unit = null
}