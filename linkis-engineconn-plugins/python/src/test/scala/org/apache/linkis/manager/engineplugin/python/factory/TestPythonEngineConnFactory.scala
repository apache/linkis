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

package org.apache.linkis.manager.engineplugin.python.factory

import org.apache.linkis.engineconn.common.creation.{
  DefaultEngineCreationContext,
  EngineCreationContext
}

import org.junit.jupiter.api.{Assertions, Test}

class TestPythonEngineConnFactory {

  @Test
  def testCreateExecutor: Unit = {
    System.setProperty("wds.linkis.server.version", "v1")
    System.setProperty(
      "wds.linkis.engineconn.plugin.default.class",
      "org.apache.linkis.manager.engineplugin.python.PythonEngineConnPlugin"
    )
    System.setProperty("pythonVersion", "python")
    val engineConnFactory: PythonEngineConnFactory = new PythonEngineConnFactory
    val engineCreationContext: EngineCreationContext = new DefaultEngineCreationContext
    val jMap = new java.util.HashMap[String, String]()
    jMap.put("python.version", "python")
    engineCreationContext.setOptions(jMap)
    val engineConn = engineConnFactory.createEngineConn(engineCreationContext)
    val executor = engineConnFactory.newExecutor(1, engineCreationContext, engineConn)
    Assertions.assertNotNull(executor)
  }

}
