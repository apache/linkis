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

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.engineconn.computation.executor.entity.CommonEngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import java.io.File
import java.nio.charset.StandardCharsets
import java.util

abstract class InitSQLHook extends EngineConnHook with Logging {

  private val INIT_SQL_DIR =
    CommonVars("wds.linkis.bdp.hive.init.sql.dir", "/appcom/config/hive-config/init_sql/").getValue

  private val INIT_SQL_ENABLE = CommonVars("wds.linkis.bdp.hive.init.sql.enable", false)

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {}

  override def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {}

  protected def getRunType(): String

  override def afterExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = Utils.tryAndError {
    val user: String =
      if (StringUtils.isNotBlank(engineCreationContext.getUser)) engineCreationContext.getUser
      else {
        Utils.getJvmUser
      }
    if (!INIT_SQL_ENABLE.getValue(engineCreationContext.getOptions)) {
      logger.info(s"$user engineConn skip execute init_sql")
      return
    }

    val initSql = readFile(INIT_SQL_DIR + user + "_hive.sql")
    if (StringUtils.isBlank(initSql)) {
      logger.info(s"$user init_sql is empty")
      return
    }
    logger.info(s"$user engineConn begin to run init_sql")
    val codeLanguageLabel = new CodeLanguageLabel
    codeLanguageLabel.setCodeType(getRunType())
    val labels = Array[Label[_]](codeLanguageLabel)
    val engineConnTask = new CommonEngineConnTask("init_sql")
    engineConnTask.setLabels(labels)
    engineConnTask.setCode(initSql)
    engineConnTask.setProperties(new util.HashMap[String, AnyRef]())
    engineConnTask.setStatus(ExecutionNodeStatus.Scheduled)
    ExecutorManager.getInstance.getExecutorByLabels(labels) match {
      case executor: ComputationExecutor =>
        executor.toExecuteTask(engineConnTask, internalExecute = true)
      case _ =>
    }
    logger.info(s"$user engineConn finished to run init_sql")
  }

  protected def readFile(path: String): String = {
    logger.info("read file: " + path)
    val file = new File(path)
    if (file.exists()) {
      FileUtils.readFileToString(file, StandardCharsets.UTF_8)
    } else {
      logger.info("file: [" + path + "] doesn't exist, ignore it.")
      ""
    }
  }

}

class SparkInitSQLHook extends InitSQLHook {

  override protected def getRunType(): String = RunType.SQL.toString

}

class HiveInitSQLHook extends InitSQLHook {

  override protected def getRunType(): String = RunType.HIVE.toString

}
