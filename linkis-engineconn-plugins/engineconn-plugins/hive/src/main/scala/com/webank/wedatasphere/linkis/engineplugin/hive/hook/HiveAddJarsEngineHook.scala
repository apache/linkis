/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineplugin.hive.hook

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.hook.EngineConnHook
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineplugin.hive.executor.HiveEngineConnExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._

class HiveAddJarsEngineHook extends EngineConnHook with Logging {

  private val JARS = "jars"

  private val addSql = "add jar "

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {}

  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = Utils.tryAndError {
    val options = engineCreationContext.getOptions
    var jars: String = ""
    options foreach {
      case (key, value) => if (JARS.equals(key)) {
        jars = value
      }
    }
    val codeLanguageLabel = new CodeLanguageLabel
    codeLanguageLabel.setCodeType(RunType.HIVE.toString)
    val labels = Array[Label[_]](codeLanguageLabel)

    if (StringUtils.isNotBlank(jars)) {
      jars.split(",") foreach {
        jar =>
          try {
            val sql = addSql + jar
            logger.info("begin to run hive sql {}", sql)
            ExecutorManager.getInstance.getExecutorByLabels(labels) match {
              case executor: HiveEngineConnExecutor =>
                executor.executeLine(new EngineExecutionContext(executor), sql)
              case _ =>
            }
          } catch {
            case t: Throwable => error(s"run hive sql ${addSql + jar} failed", t)
          }
      }
    }
  }
}
