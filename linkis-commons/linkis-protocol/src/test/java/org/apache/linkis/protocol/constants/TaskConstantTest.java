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

package org.apache.linkis.protocol.constants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TaskConstantTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String umuser = TaskConstant.UMUSER;
    String submitUser = TaskConstant.SUBMIT_USER;
    String executeUser = TaskConstant.EXECUTE_USER;
    String tasktype = TaskConstant.TASKTYPE;
    String executioncode = TaskConstant.EXECUTIONCODE;
    String jobContent = TaskConstant.JOB_CONTENT;
    String task = TaskConstant.TASK;
    String tasks = TaskConstant.TASKS;
    String params = TaskConstant.PARAMS;
    String formatcode = TaskConstant.FORMATCODE;
    String executeapplicationname = TaskConstant.EXECUTEAPPLICATIONNAME;
    String requestapplicationname = TaskConstant.REQUESTAPPLICATIONNAME;
    String scriptpath = TaskConstant.SCRIPTPATH;
    String source = TaskConstant.SOURCE;
    String runtype = TaskConstant.RUNTYPE;
    String cache = TaskConstant.CACHE;
    String paramsVariable = TaskConstant.PARAMS_VARIABLE;
    String paramsConfiguration = TaskConstant.PARAMS_CONFIGURATION;
    String paramsConfigurationStartup = TaskConstant.PARAMS_CONFIGURATION_STARTUP;
    String paramsConfigurationRuntime = TaskConstant.PARAMS_CONFIGURATION_RUNTIME;
    String paramsConfigurationSpecial = TaskConstant.PARAMS_CONFIGURATION_SPECIAL;

    Assertions.assertEquals("umUser", umuser);
    Assertions.assertEquals("submitUser", submitUser);
    Assertions.assertEquals("executeUser", executeUser);
    Assertions.assertEquals("taskType", tasktype);
    Assertions.assertEquals("executionCode", executioncode);
    Assertions.assertEquals("jobContent", jobContent);
    Assertions.assertEquals("task", task);
    Assertions.assertEquals("tasks", tasks);
    Assertions.assertEquals("params", params);
    Assertions.assertEquals("formatCode", formatcode);
    Assertions.assertEquals("executeApplicationName", executeapplicationname);
    Assertions.assertEquals("requestApplicationName", requestapplicationname);
    Assertions.assertEquals("scriptPath", scriptpath);
    Assertions.assertEquals("source", source);
    Assertions.assertEquals("runType", runtype);
    Assertions.assertEquals("cache", cache);
    Assertions.assertEquals("variable", paramsVariable);
    Assertions.assertEquals("configuration", paramsConfiguration);
    Assertions.assertEquals("startup", paramsConfigurationStartup);
    Assertions.assertEquals("runtime", paramsConfigurationRuntime);
    Assertions.assertEquals("special", paramsConfigurationSpecial);
  }
}
