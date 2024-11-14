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

  private val sqlType = getDQLSqlType

  def generatorMetrics(): DataFrame = {
    var df: DataFrame = null
    val metricsMap: java.util.Map[String, Long] = metricType match {
      case "stage" =>
        val metrics = StageMetrics(sparkSession)
        sqlType match {
          case "SELECT" =>
            df = sparkSession.sql(sql)
            metrics.runAndMeasure(df.show(0))
            metrics.aggregateStageMetricsJavaMap()
          case "INSERT" =>
            df = metrics.runAndMeasure(sparkSession.sql(sql))
            metrics.aggregateStageMetricsJavaMap()
          case _ =>
            df = sparkSession.sql(sql)
            new java.util.HashMap[String, Long]()
        }
      case "task" =>
        val metrics = TaskMetrics(sparkSession)
        sqlType match {
          case "SELECT" =>
            df = sparkSession.sql(sql)
            metrics.runAndMeasure(df.show(0))
            metrics.aggregateTaskMetricsJavaMap()
          case "INSERT" =>
            df = metrics.runAndMeasure(sparkSession.sql(sql))
            metrics.aggregateTaskMetricsJavaMap()
          case _ =>
            df = sparkSession.sql(sql)
            new java.util.HashMap[String, Long]()
        }
      case _ =>
        df = sparkSession.sql(sql)
        new java.util.HashMap[String, Long]()
    }
    if (MapUtils.isNotEmpty(metricsMap)) {
      val retMap = new util.HashMap[String, Object]()
      retMap.put("execution_code", sql)
      retMap.put("metrics", metricsMap)
      val mapper = new ObjectMapper()
      val bytes = mapper.writeValueAsBytes(retMap)
      val fs = FSFactory.getFs(outputPath)
      if (!fs.exists(outputPath.getParent)) fs.mkdirs(outputPath.getParent)
      val out = fs.write(outputPath, true)
      out.write(bytes)
      IOUtils.close(out)
      fs.close()
    }
    df
  }

  private def getDQLSqlType: String = {
    val parser = sparkSession.sessionState.sqlParser
    val logicalPlan = parser.parsePlan(sql)

    val planName = logicalPlan.getClass.getSimpleName
    planName match {
      case "UnresolvedWith" | "Project" | "GlobalLimit" => "SELECT"
      case "InsertIntoStatement" | "CreateTableAsSelectStatement" | "CreateTableAsSelect" =>
        "INSERT"
      case _ =>
        logger.info("当前SQL解析类型为{}, 跳过sparkmeasure", planName)
        planName
    }
  }

}
