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

package org.apache.linkis.manager.engineplugin.python.hook;

import org.apache.linkis.engineconn.common.creation.EngineCreationContext;
import org.apache.linkis.engineconn.common.engineconn.EngineConn;
import org.apache.linkis.engineconn.common.hook.EngineConnHook;
import org.apache.linkis.manager.engineplugin.python.conf.PythonEngineConfiguration;
import org.apache.linkis.manager.engineplugin.python.executor.PythonSession;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonVersionEngineHook implements EngineConnHook {

  private static final Logger logger = LoggerFactory.getLogger(PythonVersionEngineHook.class);

  private String pythonVersion = "python3";
  private String pythonExtraPackage = "file:///mnt/bdap/test/test/test.zip";

  @Override
  public void beforeCreateEngineConn(EngineCreationContext engineCreationContext) {
    Map<String, String> params = new HashMap<>();
    if (engineCreationContext.getOptions() != null) {
      params.putAll(engineCreationContext.getOptions());
    }
    pythonVersion = params.getOrDefault("python.version", "python3");
    pythonExtraPackage =
        params.getOrDefault("python.application.pyFiles", "file:///mnt/bdap/test/test/test.zip");
    logger.info("python version => {}", pythonVersion);
    logger.info("python application pyFiles => {}", pythonExtraPackage);
  }

  @Override
  public void beforeExecutionExecute(
      EngineCreationContext engineCreationContext, EngineConn engineConn) {
    logger.info("use python execute print cmd hello");
    if (engineConn.getEngineConnSession() instanceof PythonSession) {
      PythonSession pythonSession = (PythonSession) engineConn.getEngineConnSession();
      pythonSession.execute("print(1/2)");
      logger.info(
          "print python version => {}", PythonEngineConfiguration.PYTHON_VERSION.getValue());
    } else {
      logger.error(
          "Invalid pythonSession : {}", engineConn.getEngineConnSession().getClass().getName());
    }
  }

  @Override
  public void afterExecutionExecute(
      EngineCreationContext engineCreationContext, EngineConn engineConn) {}

  @Override
  public void afterEngineServerStartFailed(
      EngineCreationContext engineCreationContext, Throwable throwable) {}

  @Override
  public void afterEngineServerStartSuccess(
      EngineCreationContext engineCreationContext, EngineConn engineConn) {}
}
