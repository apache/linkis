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

package org.apache.linkis.engineplugin.spark.executor

import org.apache.linkis.common.exception.FatalException
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineplugin.spark.common.{Kind, SparkDataCalc}
import org.apache.linkis.engineplugin.spark.datacalc.DataCalcExecution
import org.apache.linkis.engineplugin.spark.datacalc.model.{DataCalcArrayData, DataCalcGroupData}
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.utils.EngineUtils
import org.apache.linkis.governance.common.paser.EmptyCodeParser
import org.apache.linkis.scheduler.executer.{
  CompletedExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}

import org.apache.commons.lang.exception.ExceptionUtils

import java.lang.reflect.InvocationTargetException

class SparkDataCalcExecutor(sparkEngineSession: SparkEngineSession, id: Long)
    extends SparkEngineConnExecutor(sparkEngineSession.sparkContext, id) {

  override def init(): Unit = {
    setCodeParser(new EmptyCodeParser)
    super.init()
    logger.info("spark data-calc executor start")
  }

  override def runCode(
      executor: SparkEngineConnExecutor,
      code: String,
      context: EngineExecutionContext,
      jobGroup: String
  ): ExecuteResponse = {
    logger.info("DataCalcExecutor run query: " + code)
    context.appendStdout(s"${EngineUtils.getName} >> $code")
    Utils.tryCatch {
      val execType = context.getProperties.getOrDefault("exec-type", "array").toString
      if ("group" == execType) {
        val (sources, transformations, sinks) =
          DataCalcExecution.getPlugins(DataCalcGroupData.getData(code))
        DataCalcExecution.execute(sparkEngineSession.sparkSession, sources, transformations, sinks)
      } else {
        val plugins = DataCalcExecution.getPlugins(DataCalcArrayData.getData(code))
        DataCalcExecution.execute(sparkEngineSession.sparkSession, plugins)
      }
      SuccessExecuteResponse().asInstanceOf[CompletedExecuteResponse]
    } {
      case e: InvocationTargetException =>
        logger.error("execute sparkDataCalc has InvocationTargetException!", e)
        var cause = ExceptionUtils.getCause(e)
        if (cause == null) cause = e
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(e), cause)
      case e: FatalException =>
        getErrorResponse(e, true)
      case e: Exception =>
        getErrorResponse(e, false)
      case err: VirtualMachineError =>
        getErrorResponse(err, true)
      case err: Error =>
        getErrorResponse(err, false)
    }
  }

  def getErrorResponse(throwable: Throwable, needToStopEC: Boolean): ErrorExecuteResponse = {
    if (needToStopEC) {
      logger.error(
        s"execute sparkSQL has ${throwable.getClass.getName} now to set status to shutdown!",
        throwable
      )
      ExecutorManager.getInstance.getReportExecutor.tryShutdown()
    } else {
      logger.error(s"execute sparkSQL has ${throwable.getClass.getName}!", throwable)
    }
    ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(throwable), throwable)
  }

  override protected def getExecutorIdPreFix: String = "SparkDataCalcExecutor_"

  override protected def getKind: Kind = SparkDataCalc()
}
