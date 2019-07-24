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

package com.webank.wedatasphere.linkis.engine.imexport

import java.io.File

import com.webank.wedatasphere.linkis.common.utils.Logging
import org.apache.spark.sql.SparkSession
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.io.Source

/**
  * Created by allenlliu on 9/17/18.
  */
object ExportData extends Logging {
  implicit val formats = DefaultFormats

  def exportData(spark: SparkSession, dataInfo: String, destination: String): Unit = {
    exportDataFromFile(spark, parse(dataInfo).extract[Map[String, Any]], parse(destination).extract[Map[String, Any]])
  }

  def exportDataByFile(spark: SparkSession, dataInfoPath: String, destination: String): Unit = {
    val fileSource = Source.fromFile(dataInfoPath)
    val dataInfo = fileSource.mkString
    exportDataFromFile(spark, parse(dataInfo).extract[Map[String, Any]], parse(destination).extract[Map[String, Any]])
    fileSource.close()
    val file = new File(dataInfoPath)
    if (file.exists()) {
      file.delete()
    }
  }

  def exportDataFromFile(spark: SparkSession, dataInfo: Map[String, Any], dest: Map[String, Any]): Unit = {

    //Export dataFrame
    val df = spark.sql(getExportSql(dataInfo))
    //dest

    val pathType = LoadData.getMapValue[String](dest, "pathType", "share")
    val path = if ("share".equals(pathType))
      "file://" + LoadData.getMapValue[String](dest, "path")
    else
      "hdfs://" + LoadData.getMapValue[String](dest, "path")

    val hasHeader = LoadData.getMapValue[Boolean](dest, "hasHeader", false)
    val isCsv = LoadData.getMapValue[Boolean](dest, "isCsv", true)
    val isOverwrite = LoadData.getMapValue[Boolean](dest, "isOverwrite", true)
    val sheetName = LoadData.getMapValue[String](dest, "sheetName", "Sheet1")
    val fieldDelimiter = LoadData.getMapValue[String](dest, "fieldDelimiter", ",")

    if (isCsv) {
      CsvRelation.saveDFToCsv(spark, df, path, hasHeader, isOverwrite,option = Map("fieldDelimiter" -> fieldDelimiter))
    } else {
      df.write.format("com.webank.wedatasphere.spark.excel")
        .option("sheetName", sheetName)
        .option("useHeader", hasHeader)
        .mode("overwrite").save(path)
    }
    warn(s"Succeed to export data  to path:$path")
  }

  def getExportSql(dataInfo: Map[String, Any]): String = {
    val sql = new StringBuilder
    //dataInfo
    val database = LoadData.getMapValue[String](dataInfo, "database")
    val tableName = LoadData.getMapValue[String](dataInfo, "tableName")
    val isPartition = LoadData.getMapValue[Boolean](dataInfo, "isPartition", false)
    val partition = LoadData.getMapValue[String](dataInfo, "partition", "ds")
    val partitionValue = LoadData.getMapValue[String](dataInfo, "partitionValue", "1993-01-02")
    val columns = LoadData.getMapValue[String](dataInfo, "columns", "*")
    sql.append("select ").append(columns).append(" from ").append(s"$database.$tableName")
    if (isPartition) sql.append(" where ").append(s"$partition=$partitionValue")
    val sqlString = sql.toString()
    warn(s"export sql:$sqlString")
    sqlString
  }

}
