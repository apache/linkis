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

package org.apache.linkis.manager.engineplugin.python.executor

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.conf.DWCArgumentsParser
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.creation.{
  DefaultEngineCreationContext,
  EngineCreationContext
}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.manager.engineplugin.python.factory.PythonEngineConnFactory
import org.apache.linkis.manager.engineplugin.python.hook.PythonVersionEngineHook

import scala.collection.mutable

import org.junit.jupiter.api.{Assertions, Test}

class TestPythonEngineConnExecutor {

  def initService(port: String): Unit = {
    System.setProperty("wds.linkis.server.version", "v1")
    System.setProperty(
      "wds.linkis.engineconn.plugin.default.class",
      "org.apache.linkis.manager.engineplugin.python.PythonEngineConnPlugin"
    )
    val map = new mutable.HashMap[String, String]()
    map.put("spring.mvc.servlet.path", "/api/rest_j/v1")
    map.put("server.port", port)
    map.put("spring.application.name", "SparkSqlExecutor")
    map.put("eureka.client.register-with-eureka", "false")
    map.put("eureka.client.fetch-registry", "false")
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(map.toMap))
  }

  @Test
  def testExecuteLine: Unit = {
    initService("26381")
    val hookPre = new PythonVersionEngineHook
    val engineConnFactory: PythonEngineConnFactory = new PythonEngineConnFactory
    val engineCreationContext: EngineCreationContext = new DefaultEngineCreationContext
    val path = this.getClass.getResource("/").getPath
    System.setProperty("HADOOP_CONF_DIR", "./")
    System.setProperty(
      "wds.linkis.python.py4j.home",
      path.substring(0, path.indexOf("/target")) + "/src/main/py4j"
    )
    val engineConn = engineConnFactory.createEngineConn(engineCreationContext)
    hookPre.beforeCreateEngineConn(engineCreationContext)
    val executor = engineConnFactory
      .newExecutor(1, engineCreationContext, engineConn)
      .asInstanceOf[PythonEngineConnExecutor]
    executor.init()
    Assertions.assertTrue(executor.isEngineInitialized)
    if (!System.getProperty("os.name").startsWith("Windows")) {
//      engineConn.getEngineConnSession.asInstanceOf[PythonSession].onPythonScriptInitialized(1)
//      hookPre.beforeExecutionExecute(engineCreationContext, engineConn)
      val engineExecutionContext = new EngineExecutionContext(executor, Utils.getJvmUser)
      val code = "for i in range(10):\n    print(i)"
      val response = executor.executeLine(engineExecutionContext, code)
      Assertions.assertNotNull(response)
      executor.close()
    }
  }

}
