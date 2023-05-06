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

package org.apache.spark.sql.execution.datasources.csv

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.storage.{domain => wds}
import org.apache.linkis.storage.resultset.ResultSetReaderFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}

import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{DecimalType, IntegerType, ShortType, StructField, StructType, _}

import java.util

/**
 */
object DolphinToSpark {

  private val bigDecimalPrecision =
    CommonVars("wds.linkis.dolphin.decimal.precision", 32).getValue

  private val bigDecimalScale = CommonVars("wds.linkis.dolphin.decimal.scale", 10).getValue

  def createTempView(spark: SparkSession, tableName: String, res: String): Unit = {
    createTempView(spark, tableName, res, false)
  }

  def createTempView(
      spark: SparkSession,
      tableName: String,
      res: String,
      forceReplace: Boolean
  ): Unit = {
    if (forceReplace || spark.sessionState.catalog.getTempView(tableName).isEmpty) {
      val reader = ResultSetReaderFactory.getTableResultReader(res)
      val metadata = reader.getMetaData.asInstanceOf[TableMetaData]
      val rowList = new util.ArrayList[Row]()
      var len = SparkConfiguration.DOLPHIN_LIMIT_LEN.getValue
      while (reader.hasNext && len > 0) {
        rowList.add(Row.fromSeq(reader.getRecord.asInstanceOf[TableRecord].row))
        len = len - 1
      }
      val df: DataFrame = spark.createDataFrame(rowList, metadataToSchema(metadata))
      df.createOrReplaceTempView(tableName)
    }
  }

  def metadataToSchema(metaData: TableMetaData): StructType = {
    new StructType(
      metaData.columns.map(field => StructField(field.columnName, toSparkType(field.dataType)))
    )
  }

  def toSparkType(dataType: wds.DataType): DataType = dataType match {
    case wds.DataType.NullType => NullType
    // case wds.StringType | wds.CharType | wds.VarcharType | wds.StructType | wds.ListType | wds.ArrayType | wds.MapType => StringType
    case wds.DataType.BooleanType => BooleanType
    case wds.DataType.ShortIntType => ShortType
    case wds.DataType.IntType => IntegerType
    case wds.DataType.LongType => LongType
    case wds.DataType.BigIntType => LongType
    case wds.DataType.FloatType => FloatType
    case wds.DataType.DoubleType => DoubleType
    case wds.DataType.DecimalType => DecimalType(bigDecimalPrecision, bigDecimalScale)
    case wds.DataType.DateType => DateType
    // case wds.TimestampType => TimestampType
    case wds.DataType.BinaryType => BinaryType
    case _ => StringType
  }

}
