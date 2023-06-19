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
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, RunType}

import org.apache.commons.lang3.StringUtils

abstract class UseDatabaseEngineHook extends EngineConnHook with Logging {

  private val USE_DEFAULT_DB_ENABLE = CommonVars("linkis.bdp.use.default.db.enable", false)

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
    if (!USE_DEFAULT_DB_ENABLE.getValue(engineCreationContext.getOptions)) {
      logger.info(s"$user engineConn skip execute use default db")
      return
    }
    val database = if (StringUtils.isNotEmpty(user)) {
      user + "_ind"
    } else {
      "default"
    }
    val useDataBaseSql = "use " + database
    logger.info(s"$user begin to run init_code $useDataBaseSql")
    val codeLanguageLabel = new CodeLanguageLabel
    codeLanguageLabel.setCodeType(getRunType)
    val labels = Array[Label[_]](codeLanguageLabel)
    ExecutorManager.getInstance.getExecutorByLabels(labels) match {
      case executor: ComputationExecutor =>
        executor.executeLine(new EngineExecutionContext(executor), useDataBaseSql)
      case _ =>
    }
  }

}

class SparkUseDatabaseEngineHook extends UseDatabaseEngineHook {

  override protected def getRunType(): String = RunType.SQL.toString

}

class HiveUseDatabaseEngineHook extends UseDatabaseEngineHook {

  override protected def getRunType(): String = RunType.HIVE.toString

}
