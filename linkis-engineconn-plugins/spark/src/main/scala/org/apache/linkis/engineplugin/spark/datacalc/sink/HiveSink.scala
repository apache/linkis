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

package org.apache.linkis.engineplugin.spark.datacalc.sink

import org.apache.linkis.engineplugin.spark.datacalc.api.DataCalcSink
import org.apache.linkis.engineplugin.spark.datacalc.exception.{
  DatabaseNotConfigException,
  HiveSinkException
}
import org.apache.linkis.engineplugin.spark.datacalc.service.LinkisDataSourceService

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, DataFrameWriter, Dataset, Row, SparkSession}
import org.apache.spark.sql.catalyst.catalog.HiveTableRelation
import org.apache.spark.sql.execution.datasources.{HadoopFsRelation, LogicalRelation}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types.StructField

import org.slf4j.{Logger, LoggerFactory}

class HiveSink extends DataCalcSink[HiveSinkConfig] {

  private val log: Logger = LoggerFactory.getLogger(classOf[HiveSink])

  def output(spark: SparkSession, ds: Dataset[Row]): Unit = {
    val db = LinkisDataSourceService.getDatabase(config.getTargetDatabase)
    if (db == null) {
      throw new DatabaseNotConfigException(
        s"Database ${config.getTargetDatabase} is not configured!"
      )
    }

    val targetTable = db.getDatabaseName + "." + config.getTargetTable
    val targetFields = spark.table(targetTable).schema.fields
    if (config.getWriteAsFile != null && config.getWriteAsFile) {
      val partitionsColumns = spark.catalog
        .listColumns(targetTable)
        .where(col("isPartition") === true)
        .select("name")
        .collect()
        .map(_.getAs[String]("name"))
      val location = getLocation(spark, targetTable, partitionsColumns)
      val fileFormat = getTableFileFormat(spark, targetTable)

      log.info(
        s"Write $fileFormat into target table: $targetTable, location: $location, file format: $fileFormat"
      )
      val writer = getSaveWriter(
        ds,
        targetFields.filter(field => !partitionsColumns.contains(field.name)),
        targetTable
      )
      fileFormat match {
        case FileFormat.PARQUET => writer.parquet(location)
        case FileFormat.ORC => writer.orc(location)
        case _ =>
      }

      val partition = partitionsColumns
        .map(colName => s"$colName='${config.getVariables.get(colName)}'")
        .mkString(",")
      if (StringUtils.isNotBlank(partition)) {
        log.info(s"Refresh table partition: $partition")
        refreshPartition(spark, targetTable, partition)
      }
    } else {
      val writer = getSaveWriter(ds, targetFields, targetTable)
      log.info(s"InsertInto data to hive table: $targetTable")
      writer.format("hive").insertInto(targetTable)
    }
  }

  def getSaveWriter(
      ds: Dataset[Row],
      targetFields: Array[StructField],
      targetTable: String
  ): DataFrameWriter[Row] = {
    val dsSource = sequenceFields(ds, ds.schema.fields, targetFields, targetTable)
    val sourceFields = dsSource.schema.fields

    // 开启强校验时，校验字段类型是否匹配
    if (config.getStrongCheck != null && config.getStrongCheck) {
      for (i <- sourceFields.indices) {
        val targetField = targetFields(i)
        val sourceField = sourceFields(i)
        if (!targetField.dataType.equals(sourceField.dataType)) {
          logFields(sourceFields, targetFields)
          throw new HiveSinkException(
            s"${i + 1}st column (${sourceField.name}[${sourceField.dataType}]) name or data type does not match target table column (${targetField.name}[${targetField.dataType}])"
          )
        }
      }
    }

    val writer = dsSource.repartition(config.getNumPartitions).write.mode(config.getSaveMode)
    if (config.getOptions != null && !config.getOptions.isEmpty) {
      writer.options(config.getOptions)
    }
    writer
  }

  def logFields(sourceFields: Array[StructField], targetFields: Array[StructField]): Unit = {
    log.info(s"sourceFields: ${sourceFields.mkString("Array(", ", ", ")")}")
    log.info(s"targetFields: ${targetFields.mkString("Array(", ", ", ")")}")
  }

  def sequenceFields(
      dsSource: Dataset[Row],
      sourceFields: Array[StructField],
      targetFields: Array[StructField],
      targetTable: String
  ): DataFrame = {
    if (targetFields.length != sourceFields.length) {
      logFields(sourceFields, targetFields)
      throw new HiveSinkException(
        s"$targetTable requires that the data to be inserted have the same number of columns as the target table: target table has ${targetFields.length} column(s) but the inserted data has ${sourceFields.length} column(s)"
      )
    }

    // 这里字段名都转小写，spark 元数据的字段里有大小写混合的，但hive的都是小写
    val sourceFieldMap = sourceFields.map(field => field.name.toLowerCase -> field).toMap
    val targetFieldMap = targetFields.map(field => field.name.toLowerCase -> field).toMap

    val subSet = targetFieldMap.keySet -- sourceFieldMap.keySet
    if (subSet.isEmpty) {
      // 重排字段顺序，防止字段顺序不一致
      dsSource.select(targetFields.map(field => col(field.name)): _*)
    } else if (subSet.size == targetFieldMap.size) {
      // 字段名都无法对应时，字段按顺序重命名为目标表字段
      log.info("None target table fields match with source fields, write in order")
      dsSource.toDF(targetFields.map(field => field.name): _*)
    } else {
      throw new HiveSinkException(
        s"$targetTable fields(${subSet.mkString(",")}) are not exist in source fields"
      )
    }
  }

  /**
   * 获取hive表存储位置
   *
   * @param spark
   * @param targetTable
   *   表名 database.table
   * @return
   *   表路径
   */
  def getLocation(
      spark: SparkSession,
      targetTable: String,
      partitionsColumns: Array[String]
  ): String = {
    val locations =
      spark.sql(s"desc formatted $targetTable").filter(col("col_name") === "Location").collect()
    var location: String = locations(0).getString(1)
    for (partitionColName <- partitionsColumns) {
      if (
          !config.getVariables.containsKey(partitionColName) ||
          StringUtils.isBlank(config.getVariables.get(partitionColName))
      ) {
        throw new HiveSinkException(
          s"Please set [${partitionsColumns.mkString(", ")}] in variables"
        )
      }
      location += s"/$partitionColName=${config.getVariables.get(partitionColName)}"
    }
    location
  }

  def getTableFileFormat(spark: SparkSession, targetTable: String): FileFormat.Value = {
    try {
      var fileFormat: FileFormat.FileFormat = FileFormat.OTHER
      spark.table(targetTable).queryExecution.optimizedPlan match {
        case logicalRelation: LogicalRelation =>
          logicalRelation.relation match {
            case hadoopFsRelation: HadoopFsRelation =>
              hadoopFsRelation.fileFormat match {
                case _: org.apache.spark.sql.execution.datasources.orc.OrcFileFormat =>
                  fileFormat = FileFormat.ORC
                case _: org.apache.spark.sql.execution.datasources.parquet.ParquetFileFormat =>
                  fileFormat = FileFormat.PARQUET
                case dataSourceRegister: DataSourceRegister =>
                  fileFormat = FileFormat.withName(dataSourceRegister.shortName.toUpperCase)
                case _ =>
              }
          }
        case hiveTableRelation: HiveTableRelation =>
        // todo
      }
      fileFormat
    } catch {
      case _: Exception => FileFormat.OTHER
    }
  }

  def refreshPartition(spark: SparkSession, targetTable: String, partition: String): Unit = {
    spark.sql(s"ALTER TABLE $targetTable DROP IF EXISTS partition($partition)")
    spark.sql(s"ALTER TABLE $targetTable ADD IF NOT EXISTS partition($partition)")
  }

}

object FileFormat extends Enumeration {
  type FileFormat = Value
  val ORC, PARQUET, OTHER = Value
}
