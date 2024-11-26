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

package org.apache.linkis.engineplugin.spark.sparkmeasure

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.FSFactory

import org.apache.commons.collections4.MapUtils
import org.apache.commons.io.IOUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util

import ch.cern.sparkmeasure.{StageMetrics, TaskMetrics}
import com.fasterxml.jackson.databind.ObjectMapper

class SparkSqlMeasure(
    sparkSession: SparkSession,
    sql: String,
    metricType: String,
    outputPath: FsPath
) extends Logging {

  private val sqlType: String = determineSqlType

  def begin(metrics: Either[StageMetrics, TaskMetrics]): Unit = {
    metrics match {
      case Left(stageMetrics) =>
        stageMetrics.begin()
      case Right(taskMetrics) =>
        taskMetrics.begin()
    }
  }

  def end(metrics: Either[StageMetrics, TaskMetrics]): Unit = {
    metrics match {
      case Left(stageMetrics) =>
        stageMetrics.end()
      case Right(taskMetrics) =>
        taskMetrics.end()
    }
  }

  private def enableSparkMeasure: Boolean = {
    sqlType match {
      case "SELECT" | "INSERT" => true
      case _ => false
    }
  }

  def getSparkMetrics: Option[Either[StageMetrics, TaskMetrics]] = {
    if (enableSparkMeasure) {
      metricType match {
        case "stage" => Some(Left(StageMetrics(sparkSession)))
        case "task" => Some(Right(TaskMetrics(sparkSession)))
        case _ => None
      }
    } else {
      None
    }
  }

  def outputMetrics(metrics: Either[StageMetrics, TaskMetrics]): Unit = {
    if (enableSparkMeasure) {
      val metricsMap = collectMetrics(metrics)

      if (MapUtils.isNotEmpty(metricsMap)) {
        val retMap = new util.HashMap[String, Object]()
        retMap.put("execution_code", sql)
        retMap.put("metrics", metricsMap)

        val mapper = new ObjectMapper()
        val bytes = mapper.writeValueAsBytes(retMap)

        val fs = FSFactory.getFs(outputPath)
        try {
          if (!fs.exists(outputPath.getParent)) fs.mkdirs(outputPath.getParent)
          val out = fs.write(outputPath, true)
          try {
            out.write(bytes)
          } finally {
            IOUtils.closeQuietly(out)
          }
        } finally {
          fs.close()
        }
      }
    }
  }

  private def determineSqlType: String = {
    val parser = sparkSession.sessionState.sqlParser
    val logicalPlan = parser.parsePlan(sql)

    logicalPlan.getClass.getSimpleName match {
      case "UnresolvedWith" | "Project" | "GlobalLimit" => "SELECT"
      case "InsertIntoStatement" | "CreateTableAsSelectStatement" | "CreateTableAsSelect" =>
        "INSERT"
      case planName =>
        logger.info(s"Unsupported sql type")
        planName
    }
  }

  private def collectMetrics(
      metrics: Either[StageMetrics, TaskMetrics]
  ): java.util.Map[String, Long] = {
    metrics match {
      case Left(stageMetrics) =>
        stageMetrics.aggregateStageMetricsJavaMap()
      case Right(taskMetrics) =>
        taskMetrics.aggregateTaskMetricsJavaMap()
      case _ => new util.HashMap[String, Long]()
    }
  }

}
