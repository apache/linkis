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

package org.apache.linkis.manager.engineplugin.python.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

object PythonEngineConfiguration {

  val PYTHON_CONSOLE_OUTPUT_LINE_LIMIT = CommonVars("wds.linkis.python.line.limit", 10)

  val PY4J_HOME =
    CommonVars("wds.linkis.python.py4j.home", getPy4jHome)

  val PYTHON_VERSION = CommonVars("pythonVersion", "python3")

  val PYTHON_PATH: CommonVars[String] = CommonVars[String](
    "python.path",
    "",
    "Specify Python's extra path, which only accepts shared storage paths（指定Python额外的path，该路径只接受共享存储的路径）."
  )

  val PYTHON_LANGUAGE_REPL_INIT_TIME =
    CommonVars[TimeType]("wds.linkis.engine.python.language-repl.init.time", new TimeType("30s"))

  private def getPy4jHome(): String = {
    val confDir = "/conf"
    if (null != this.getClass.getResource(confDir)) {
      this.getClass.getResource(confDir).getPath
    } else {
      this.getClass.getResource("/").getPath
    }
  }

}
