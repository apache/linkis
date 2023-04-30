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

package org.apache.linkis.manager.engineplugin.shell.executor;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.conf.DWCArgumentsParser;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.scheduler.executer.ExecuteResponse;

import scala.collection.mutable.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestShellEngineConnExecutor {

  @Test
  public void testShellEngineConnExecutor() throws ReflectiveOperationException {
    boolean isWindows = System.getProperty("os.name").startsWith("Windows");

    System.setProperty("wds.linkis.server.version", "v1");
    System.setProperty("HADOOP_CONF_DIR", "./");
    System.setProperty(
        "wds.linkis.engineconn.plugin.default.class",
        "org.apache.linkis.manager.engineplugin.shell.ShellEngineConnPlugin");

    HashMap<String, String> map = new HashMap<>();
    map.put("spring.mvc.servlet.path", "/api/rest_j/v1");
    map.put("server.port", "26380");
    map.put("spring.application.name", "shellEngineExecutor");
    map.put("eureka.client.register-with-eureka", "false");
    map.put("eureka.client.fetch-registry", "false");
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(map.toMap(null)));
    ShellEngineConnExecutor shellEngineConnExecutor = new ShellEngineConnExecutor(1);
    shellEngineConnExecutor.init();
    Assertions.assertTrue(shellEngineConnExecutor.isEngineInitialized());
    if (!isWindows) {
      EngineExecutionContext engineExecutionContext =
          new EngineExecutionContext(shellEngineConnExecutor, Utils.getJvmUser());
      engineExecutionContext.setJobId("1");
      ExecuteResponse response = shellEngineConnExecutor.executeLine(engineExecutionContext, "id");
      Assertions.assertNotNull(response);
      shellEngineConnExecutor.close();
    }
  }
}
