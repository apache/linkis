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

package org.apache.linkis.engineplugin.spark.imexport

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.imexport.util.BackGroundServiceUtils
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.spark.sql.SparkSession

/**
 */
object ExportData extends Logging {

  def exportData(spark: SparkSession, dataInfo: String, destination: String): Unit = {
    exportDataFromFile(
      spark,
      BDPJettyServerHelper.gson.fromJson(dataInfo, classOf[Map[String, Any]]),
      BDPJettyServerHelper.gson.fromJson(destination, classOf[Map[String, Any]])
    )
  }

  def exportDataByFile(spark: SparkSession, dataInfoPath: String, destination: String): Unit = {
    val dataInfo = BackGroundServiceUtils.exchangeExecutionCode(dataInfoPath)
    exportDataFromFile(
      spark,
      BDPJettyServerHelper.gson.fromJson(dataInfo, classOf[Map[String, Any]]),
      BDPJettyServerHelper.gson.fromJson(destination, classOf[Map[String, Any]])
    )
  }

  def exportDataFromFile(
      spark: SparkSession,
      dataInfo: Map[String, Any],
      dest: Map[String, Any]
  ): Unit = {

    // Export dataFrame
    val df = spark.sql(getExportSql(dataInfo))
    // dest

    val pathType = LoadData.getMapValue[String](dest, "pathType", "share")
    val path =
      if ("share".equals(pathType)) {
        "file://" + LoadData.getMapValue[String](dest, "path")
      } else if (SparkConfiguration.IS_VIEWFS_ENV.getValue) {
        LoadData.getMapValue[String](dest, "path")
      } else "hdfs://" + LoadData.getMapValue[String](dest, "path")

    val hasHeader = LoadData.getMapValue[Boolean](dest, "hasHeader", false)
    val isCsv = LoadData.getMapValue[Boolean](dest, "isCsv", true)
    val isOverwrite = LoadData.getMapValue[Boolean](dest, "isOverwrite", true)
    val sheetName = LoadData.getMapValue[String](dest, "sheetName", "Sheet1")
    val fieldDelimiter = LoadData.getMapValue[String](dest, "fieldDelimiter", ",")
    val nullValue = LoadData.getMapValue[String](dest, "nullValue", "SHUFFLEOFF")
    val encoding = LoadData.getMapValue[String](dest, "encoding", "uft-8")

    var options = Map(
      "fieldDelimiter" -> fieldDelimiter,
      "exportNullValue" -> nullValue,
      "encoding" -> encoding
    )
    if (isCsv) {
      logger.info(
        s"Try to saveDFToCsv with path:${path},hasHeader:${hasHeader},isOverwrite:${isOverwrite},options:${options}"
      )
      CsvRelation.saveDFToCsv(spark, df, path, hasHeader, isOverwrite, options)
    } else {
      df.write
        .format("com.webank.wedatasphere.spark.excel")
        .option("sheetName", sheetName)
        .option("useHeader", hasHeader)
        .option("exportNullValue", nullValue)
        .mode("overwrite")
        .save(path)
    }
    logger.warn(s"Succeed to export data  to path:$path")
  }

  def getExportSql(dataInfo: Map[String, Any]): String = {
    val sql = new StringBuilder
    // dataInfo
    val database = LoadData.getMapValue[String](dataInfo, "database")
    val tableName = LoadData.getMapValue[String](dataInfo, "tableName")
    val isPartition = LoadData.getMapValue[Boolean](dataInfo, "isPartition", false)
    val partition = LoadData.getMapValue[String](dataInfo, "partition", "ds")
    val partitionValue = if (partition.equals("ds")) {
      LoadData.getMapValue[String](dataInfo, "partitionValue", "1993-01-02")
    } else {
      val value = LoadData.getMapValue[String](dataInfo, "partitionValue", "1993-01-02")
      s"'$value'"
    }
    val columns = LoadData.getMapValue[String](dataInfo, "columns", "*")
    sql.append("select ").append(columns).append(" from ").append(s"$database.$tableName")
    if (isPartition) sql.append(" where ").append(s"$partition=$partitionValue")
    val sqlString = sql.toString()
    logger.warn(s"export sql:$sqlString")
    sqlString
  }

}
