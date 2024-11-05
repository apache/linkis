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

package org.apache.linkis.engineconn.computation.executor.hook

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel

abstract class PythonModuleLoadEngineConnHook
    extends PythonModuleLoad
    with EngineConnHook
    with Logging {

  override def afterExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    Utils.tryAndWarnMsg {
      val codeLanguageLabel = new CodeLanguageLabel
      codeLanguageLabel.setCodeType(runType.toString)
      logger.info(s"engineType: ${engineType}")
      val labels = Array[Label[_]](codeLanguageLabel)
      loadPythonModules(labels)
    }(s"Failed to load Python Modules: ${engineType}")

  }

  override def afterEngineServerStartFailed(
      engineCreationContext: EngineCreationContext,
      throwable: Throwable
  ): Unit = {
    logger.error(s"Failed to start Engine Server: ${throwable.getMessage}", throwable)
  }

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    logger.info("Preparing to load Python Module...")
  }

  override def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    logger.info(s"Before executing command on load Python Module.")
  }

}
