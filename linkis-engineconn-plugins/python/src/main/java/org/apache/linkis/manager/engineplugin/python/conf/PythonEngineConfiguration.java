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

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;

public class PythonEngineConfiguration {

  public static final CommonVars<Integer> PYTHON_CONSOLE_OUTPUT_LINE_LIMIT =
      CommonVars.apply("wds.linkis.python.line.limit", 10);

  public static final CommonVars<String> PY4J_HOME =
      CommonVars.apply("wds.linkis.python.py4j.home", getPy4jHome());

  public static final CommonVars<String> PYTHON_VERSION =
      CommonVars.apply("pythonVersion", "python3");

  public static final CommonVars<String> PYTHON_PATH =
      CommonVars.apply(
          "python.path",
          "",
          "Specify Python's extra path, which only accepts shared storage paths（指定Python额外的path，该路径只接受共享存储的路径）.");

  public static final CommonVars<TimeType> PYTHON_LANGUAGE_REPL_INIT_TIME =
      CommonVars.apply("wds.linkis.engine.python.language-repl.init.time", new TimeType("30s"));

  private static String getPy4jHome() {
    String confDir = "/conf";
    if (null != PythonEngineConfiguration.class.getResource(confDir)) {
      return PythonEngineConfiguration.class.getResource(confDir).getPath();
    } else {
      return PythonEngineConfiguration.class.getResource("/").getPath();
    }
  }
}
