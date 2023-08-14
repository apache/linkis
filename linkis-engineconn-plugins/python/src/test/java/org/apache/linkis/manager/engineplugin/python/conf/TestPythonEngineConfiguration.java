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

package org.apache.linkis.manager.engineplugin.python.conf;

import org.apache.linkis.common.conf.TimeType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPythonEngineConfiguration {

  @Test
  public void testConfig() {
    System.setProperty("pythonVersion", "python");
    System.setProperty("wds.linkis.python.py4j.home", this.getClass().getResource("/").getPath());
    Assertions.assertEquals(
        10, PythonEngineConfiguration.PYTHON_CONSOLE_OUTPUT_LINE_LIMIT.getValue());
    Assertions.assertEquals("python", PythonEngineConfiguration.PYTHON_VERSION.getValue());
    Assertions.assertEquals(
        new TimeType("30s").toString(),
        PythonEngineConfiguration.PYTHON_LANGUAGE_REPL_INIT_TIME.getValue().toString());
    Assertions.assertEquals(
        this.getClass().getResource("/").getPath(), PythonEngineConfiguration.PY4J_HOME.getValue());
    Assertions.assertEquals("", PythonEngineConfiguration.PYTHON_PATH.getValue());
  }
}
