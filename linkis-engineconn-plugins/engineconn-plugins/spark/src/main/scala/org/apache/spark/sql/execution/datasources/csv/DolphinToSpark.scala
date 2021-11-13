/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.spark.sql.execution.datasources.csv

import java.util

import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.storage.resultset.ResultSetReader
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.linkis.storage.{domain => wds}
import org.apache.spark.sql.types.{DecimalType, IntegerType, ShortType, StructField, StructType, _}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}


/**
  *
  */
object DolphinToSpark {

  val bigDecimalPrecision = 20
  val bigDecimalScale = 10

  def createTempView(spark: SparkSession, tableName: String, res: String): Unit = {
    createTempView(spark, tableName, res, false)
  }

  def createTempView(spark: SparkSession, tableName: String, res: String, forceReplace: Boolean): Unit = {
    if (forceReplace || spark.sessionState.catalog.getTempView(tableName).isEmpty) {
      val reader = ResultSetReader.getTableResultReader(res)
      val metadata = reader.getMetaData.asInstanceOf[TableMetaData]
      val rowList = new util.ArrayList[Row]()
      var len = SparkConfiguration.DOLPHIN_LIMIT_LEN.getValue
      while (reader.hasNext && len > 0){
        rowList.add(Row.fromSeq(reader.getRecord.asInstanceOf[TableRecord].row))
        len = len -1
      }
      val df: DataFrame = spark.createDataFrame(rowList,metadataToSchema(metadata))
      df.createOrReplaceTempView(tableName)
    }
  }

  def metadataToSchema(metaData: TableMetaData):StructType = {
    new StructType(metaData.columns.map(field => StructField(field.columnName,toSparkType(field.dataType))))
  }

  def toSparkType(dataType:wds.DataType):DataType = dataType match {
    case wds.NullType => NullType
    case wds.BooleanType =>  BooleanType
    case wds.ShortIntType => ShortType
    case wds.IntType => IntegerType
    case wds.LongType => LongType
    case wds.BigIntType => LongType
    case wds.FloatType => FloatType
    case wds.DoubleType  => DoubleType
    case wds.DecimalType => DecimalType(bigDecimalPrecision,bigDecimalScale)
    case wds.DateType => DateType
    case wds.BinaryType => BinaryType
    case _ => StringType
  }

}
