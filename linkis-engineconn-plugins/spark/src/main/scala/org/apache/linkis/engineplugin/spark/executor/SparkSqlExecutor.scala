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

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.common.{Kind, SparkSQL}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.sparkmeasure.SparkSqlMeasure
import org.apache.linkis.engineplugin.spark.utils.{DirectPushCache, EngineUtils}
import org.apache.linkis.governance.common.constant.job.JobRequestConstants
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.scheduler.executer._

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.spark.sql.DataFrame

import java.lang.reflect.InvocationTargetException
import java.util
import java.util.Date

import scala.collection.JavaConverters._

import ch.cern.sparkmeasure.{StageMetrics, TaskMetrics}

class SparkSqlExecutor(
    sparkEngineSession: SparkEngineSession,
    id: Long,
    options: util.Map[String, String]
) extends SparkEngineConnExecutor(sparkEngineSession.sparkContext, id) {

  override def init(): Unit = {

    setCodeParser(new SQLCodeParser)
    super.init()
    logger.info("spark sql executor start")
  }

  override protected def getKind: Kind = SparkSQL()

  // Only used in the scenario of direct pushing, dataFrame won't be fetched at a time,
  // It will cache the lazy dataFrame in memory and return the result when client .
  private def submitResultSetIterator(taskId: String, df: DataFrame): Unit = {
    if (!DirectPushCache.isTaskCached(taskId)) {
      DirectPushCache.submitExecuteResult(taskId, df)
    } else {
      logger.error(s"Task $taskId already exists in resultSet cache.")
    }
  }

  override protected def runCode(
      executor: SparkEngineConnExecutor,
      code: String,
      engineExecutionContext: EngineExecutionContext,
      jobGroup: String
  ): ExecuteResponse = {

    if (
        engineExecutionContext.getCurrentParagraph == 1 && engineExecutionContext.getProperties
          .containsKey(JobRequestConstants.LINKIS_JDBC_DEFAULT_DB)
    ) {
      val defaultDB =
        engineExecutionContext.getProperties
          .get(JobRequestConstants.LINKIS_JDBC_DEFAULT_DB)
          .asInstanceOf[String]
      logger.info(s"set default DB to $defaultDB")
      sparkEngineSession.sqlContext.sql(s"use $defaultDB")
    }

    logger.info("SQLExecutor run query: " + code)
    engineExecutionContext.appendStdout(s"${EngineUtils.getName} >> $code")
    val standInClassLoader = Thread.currentThread().getContextClassLoader
    try {
      val sqlStartTime = System.currentTimeMillis()
      Thread
        .currentThread()
        .setContextClassLoader(sparkEngineSession.sparkSession.sharedState.jarClassLoader)
      val extensions =
        org.apache.linkis.engineplugin.spark.extension.SparkSqlExtension.getSparkSqlExtensions()

      // Start capturing Spark metrics
      val sparkMeasure: Option[SparkSqlMeasure] =
        createSparkMeasure(engineExecutionContext, sparkEngineSession, code)
      val sparkMetrics: Option[Either[StageMetrics, TaskMetrics]] = sparkMeasure.flatMap {
        measure =>
          val metrics = measure.getSparkMetrics
          metrics.foreach(measure.begin)
          metrics
      }

      val df = sparkEngineSession.sqlContext.sql(code)

      Utils.tryQuietly(
        extensions.foreach(
          _.afterExecutingSQL(
            sparkEngineSession.sqlContext,
            code,
            df,
            SparkConfiguration.SQL_EXTENSION_TIMEOUT.getValue,
            sqlStartTime
          )
        )
      )

      if (engineExecutionContext.isEnableDirectPush) {
        submitResultSetIterator(lastTask.getTaskId, df)
      } else {
        SQLSession.showDF(
          sparkEngineSession.sparkContext,
          jobGroup,
          df,
          null,
          SparkConfiguration.SHOW_DF_MAX_RES.getValue,
          engineExecutionContext
        )
      }

      // Stop capturing Spark metrics and output the records to the specified file.
      sparkMeasure.foreach { measure =>
        sparkMetrics.foreach(measure.end)
        sparkMetrics.foreach(measure.outputMetrics)
      }

      SuccessExecuteResponse()
    } catch {
      case e: InvocationTargetException =>
        var cause = ExceptionUtils.getCause(e)
        if (cause == null) cause = e
        // error("execute sparkSQL failed!", cause)
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(e), cause)
      case ite: Exception =>
        // error("execute sparkSQL failed!", ite)
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(ite), ite)
    } finally {
      Thread.currentThread().setContextClassLoader(standInClassLoader)
    }
  }

  private def createSparkMeasure(
      engineExecutionContext: EngineExecutionContext,
      sparkEngineSession: SparkEngineSession,
      code: String
  ): Option[SparkSqlMeasure] = {
    val sparkMeasureType = engineExecutionContext.getProperties
      .getOrDefault(SparkConfiguration.SPARKMEASURE_AGGREGATE_TYPE, "")
      .toString

    if (sparkMeasureType.nonEmpty) {
      val outputPrefix = SparkConfiguration.SPARKMEASURE_OUTPUT_PREFIX.getValue(options)
      val outputPath = FsPath.getFsPath(
        outputPrefix,
        LabelUtil.getUserCreator(engineExecutionContext.getLabels.toList.asJava)._1,
        sparkMeasureType,
        JobUtils.getJobIdFromMap(engineExecutionContext.getProperties),
        new Date().getTime.toString
      )
      Some(new SparkSqlMeasure(sparkEngineSession.sparkSession, code, sparkMeasureType, outputPath))
    } else {
      None
    }
  }

  override protected def getExecutorIdPreFix: String = "SparkSqlExecutor_"
  def getSparkEngineSession: SparkEngineSession = sparkEngineSession
}
