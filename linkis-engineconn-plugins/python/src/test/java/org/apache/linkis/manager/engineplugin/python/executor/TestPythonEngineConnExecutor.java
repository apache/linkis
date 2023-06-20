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

package org.apache.linkis.manager.engineplugin.python.executor;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.conf.DWCArgumentsParser;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.engineconn.common.creation.DefaultEngineCreationContext;
import org.apache.linkis.engineconn.common.creation.EngineCreationContext;
import org.apache.linkis.engineconn.common.engineconn.EngineConn;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.manager.engineplugin.python.factory.PythonEngineConnFactory;
import org.apache.linkis.manager.engineplugin.python.hook.PythonVersionEngineHook;

import scala.collection.mutable.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPythonEngineConnExecutor {

  public void initService(String port) {
    System.setProperty("wds.linkis.server.version", "v1");
    System.setProperty(
        "wds.linkis.engineconn.plugin.default.class",
        "org.apache.linkis.manager.engineplugin.python.PythonEngineConnPlugin");
    HashMap<String, String> map = new HashMap<>();
    map.put("spring.mvc.servlet.path", "/api/rest_j/v1");
    map.put("server.port", port);
    map.put("spring.application.name", "SparkSqlExecutor");
    map.put("eureka.client.register-with-eureka", "false");
    map.put("eureka.client.fetch-registry", "false");
    try {
      DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(map.toMap(null)));
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testExecuteLine() {
    initService("26381");
    PythonVersionEngineHook hookPre = new PythonVersionEngineHook();
    PythonEngineConnFactory engineConnFactory = new PythonEngineConnFactory();
    EngineCreationContext engineCreationContext = new DefaultEngineCreationContext();
    String path = this.getClass().getResource("/").getPath();
    System.setProperty("HADOOP_CONF_DIR", "./");
    System.setProperty(
        "wds.linkis.python.py4j.home",
        path.substring(0, path.indexOf("/target")) + "/src/main/py4j");
    EngineConn engineConn = engineConnFactory.createEngineConn(engineCreationContext);
    hookPre.beforeCreateEngineConn(engineCreationContext);
    PythonEngineConnExecutor executor =
        (PythonEngineConnExecutor)
            engineConnFactory.newExecutor(1, engineCreationContext, engineConn);
    executor.init();
    Assertions.assertTrue(executor.isEngineInitialized());
    if (!System.getProperty("os.name").startsWith("Windows")) {
      EngineExecutionContext engineExecutionContext =
          new EngineExecutionContext(executor, Utils.getJvmUser());
      String code = "for i in range(10):\n    print(i)";
      Object response = executor.executeLine(engineExecutionContext, code);
      Assertions.assertNotNull(response);
      executor.close();
    }
  }
}
