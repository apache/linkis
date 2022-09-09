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

package org.apache.linkis.engineplugin.hive.hook

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineplugin.hive.executor.HiveEngineConnExecutor
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}

import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._

class HiveAddJarsEngineHook extends EngineConnHook with Logging {

  private val JARS = "jars"

  private val addSql = "add jar "

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {}

  override def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {}

  override def afterExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = Utils.tryAndError {
    val options = engineCreationContext.getOptions
    var jars: String = ""
    val udf_jars = CommonVars(Environment.UDF_JARS.toString, "", "UDF jar PAth").getValue
    logger.info("udf jar_path:" + udf_jars)
    options.asScala foreach { case (key, value) =>
      if (JARS.equals(key)) {
        jars = value
      }
    }
    if (StringUtils.isNotEmpty(udf_jars)) {
      if (StringUtils.isNotEmpty(jars)) {
        jars = jars + "," + udf_jars
      } else {
        jars = udf_jars
      }
    }
    val codeLanguageLabel = new CodeLanguageLabel
    codeLanguageLabel.setCodeType(RunType.HIVE.toString)
    val labels = Array[Label[_]](codeLanguageLabel)

    if (StringUtils.isNotBlank(jars)) {
      jars.split(",") foreach { jar =>
        try {
          val sql = addSql + jar
          logger.info("begin to run hive sql {}", sql)
          ExecutorManager.getInstance.getExecutorByLabels(labels) match {
            case executor: HiveEngineConnExecutor =>
              executor.executeLine(new EngineExecutionContext(executor), sql)
            case _ =>
          }
        } catch {
          case t: Throwable => logger.error(s"run hive sql ${addSql + jar} failed", t)
        }
      }
    }
  }

}
