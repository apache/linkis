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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.spark.datacalc.api.DataCalcSink
import org.apache.linkis.engineplugin.spark.datacalc.exception.HiveSinkException
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql._
import org.apache.spark.sql.execution.datasources.{HadoopFsRelation, LogicalRelation}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types.StructField

class HiveSink extends DataCalcSink[HiveSinkConfig] with Logging {

  def output(spark: SparkSession, ds: Dataset[Row]): Unit = {
    val targetTable =
      if (StringUtils.isBlank(config.getTargetDatabase)) config.getTargetTable
      else config.getTargetDatabase + "." + config.getTargetTable
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

      logger.info(
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
        logger.info(s"Refresh table partition: $partition")
        refreshPartition(spark, targetTable, partition)
      }
    } else {
      val writer = getSaveWriter(ds, targetFields, targetTable)
      logger.info(s"InsertInto data to hive table: $targetTable")
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

    // Compare column's data type when [strongCheck] is true
    if (config.getStrongCheck != null && config.getStrongCheck) {
      for (i <- sourceFields.indices) {
        val targetField = targetFields(i)
        val sourceField = sourceFields(i)
        if (!targetField.dataType.equals(sourceField.dataType)) {
          logFields(sourceFields, targetFields)
          throw new HiveSinkException(
            SparkErrorCodeSummary.DATA_CALC_COLUMN_NOT_MATCH.getErrorCode,
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
    logger.info(s"sourceFields: ${sourceFields.mkString("Array(", ", ", ")")}")
    logger.info(s"targetFields: ${targetFields.mkString("Array(", ", ", ")")}")
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
        SparkErrorCodeSummary.DATA_CALC_COLUMN_NUM_NOT_MATCH.getErrorCode,
        s"$targetTable requires that the data to be inserted have the same number of columns " +
          s"as the target table: target table has ${targetFields.length} column(s) " +
          s"but the inserted data has ${sourceFields.length} column(s)"
      )
    }

    // hive columns is lowercase
    val sourceFieldMap = sourceFields.map(field => field.name.toLowerCase -> field).toMap
    val targetFieldMap = targetFields.map(field => field.name.toLowerCase -> field).toMap

    val subSet = targetFieldMap.keySet -- sourceFieldMap.keySet
    if (subSet.isEmpty) {
      // sort column
      dsSource.select(targetFields.map(field => col(field.name)): _*)
    } else if (subSet.size == targetFieldMap.size) {
      logger.info("None target table fields match with source fields, write in order")
      dsSource.toDF(targetFields.map(field => field.name): _*)
    } else {
      throw new HiveSinkException(
        SparkErrorCodeSummary.DATA_CALC_FIELD_NOT_EXIST.getErrorCode,
        s"$targetTable fields(${subSet.mkString(",")}) are not exist in source fields"
      )
    }
  }

  /**
   * get hive table location
   *
   * @param spark
   * @param targetTable
   * @return
   *   hive table location
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
          SparkErrorCodeSummary.DATA_CALC_VARIABLE_NOT_EXIST.getErrorCode,
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
                case _: org.apache.spark.sql.execution.datasources.parquet.ParquetFileFormat =>
                  fileFormat = FileFormat.PARQUET
                case dataSourceRegister: DataSourceRegister =>
                  fileFormat = FileFormat.withName(dataSourceRegister.shortName.toUpperCase)
                case _ =>
                  if (hadoopFsRelation.fileFormat.getClass.getSimpleName.equals("OrcFileFormat")) {
                    fileFormat = FileFormat.ORC
                  }
              }
          }
        // case hiveTableRelation: HiveTableRelation =>
        // todo please note `HiveTableRelation` was added after spark 2.2.1
        case _ =>
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
