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

package org.apache.spark.sql.execution.datasources.csv

import java.math.BigDecimal
import java.sql.Timestamp

import com.webank.wedatasphere.linkis.common.utils.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.util.DateTimeUtils
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Exception._

/**
  * Created by allenlliu on 10/16/17.
  */
object RddToDFUtil extends Logging {

  def rddToDF(spark: SparkSession,
              tokenRdd: RDD[Array[String]],
              header: Array[String],
              options: Map[String, String] = Map.empty[String, String]): DataFrame = {
    val csvOptions = new CSVOptions(options)
    val rows: Array[Array[String]] = tokenRdd.take(25)
    val step = rows.length / 5
    var dataTypes: Array[DataType] = null
    for (i <- 0 until(rows.length, step)) {
      val row = rows(i)
      val types = row.map(
        att => if("null".equalsIgnoreCase(att)){
          CSVInferSchema.inferField(NullType, null, csvOptions)
        } else {
          CSVInferSchema.inferField(NullType, att, csvOptions)
        }
      )
      for (i <- types.indices) {
        val newType = types(i)
        val oldType = if (dataTypes != null && i < dataTypes.length) dataTypes(i) else null
        if (null != oldType && newType != oldType) {
          if (newType == StringType || oldType == StringType) {
            types(i) = StringType
          }
          else if (newType == DoubleType || oldType == DoubleType) {
            types(i) = DoubleType
          } else if (newType == LongType || oldType == LongType) {
            types(i) = LongType
          }else if (newType == NullType) {
            types(i) = oldType
          }
        }
      }
      dataTypes = types
    }
    var count = -1
    val fields: Array[StructField] = dataTypes.map(data => {
      count = count + 1
      StructField(header(count), data)
    })
    val structType: StructType = StructType(fields)
    val rowRdd = tokenRdd.map(att => {
      val row = ArrayBuffer[Any]()
      for (i <- dataTypes.indices) {
        val filed = if ((allCatch opt att(i)).isDefined) {
          att(i)
        } else {
          warn("row is Error:" + att(0))
          "NULL"
        }
        val data = if("null".equalsIgnoreCase(filed)){
          null
        } else if(dataTypes(i) != StringType && filed.isEmpty) {
          null
        } else{
          dataTypes(i) match {
            case StringType => filed
            case DoubleType => filed.toDouble
            case IntegerType => filed.toInt
            case LongType => filed.toLong
            case NullType => filed
            case BooleanType => filed.toBoolean
            case _: DecimalType => new BigDecimal(filed)
            case TimestampType => if ((allCatch opt csvOptions.timestampFormat.parse(filed)).isDefined) {
              new Timestamp(csvOptions.timestampFormat.parse(filed).getTime)
            } else if ((allCatch opt DateTimeUtils.stringToTime(filed)).isDefined) {
              new Timestamp(DateTimeUtils.stringToTime(filed).getTime)
            }
            case other: DataType =>
              throw new UnsupportedOperationException(s"Unexpected data type $other")
          }
        }
        row += data
      }
      Row.fromSeq(row)
    })
    //warn("row is Error2:" + rowRdd.take(1))
    val df = spark.createDataFrame(rowRdd, structType)
    df
  }

  def createTempView(spark: SparkSession,
                     tableName: String,
                     hdfsPath: String,
                     separator: String = "\t",
                     isIdeResult: Boolean = false,
                     inferSchema: Boolean = false
                    ): Unit = {
    if (spark.sessionState.catalog.getTempView(tableName).isEmpty) {

      val df: DataFrame = if (!isIdeResult) {
        spark.read.format("csv")
          .option("sep", separator)
          .option("inferSchema", true)
          .option("header", "true")
          .load(hdfsPath)
      } else {
        val resultRdd = spark.sparkContext.textFile(hdfsPath)
        val colRdd = resultRdd.take(2)
        val col = colRdd(1).split(separator)
        val rowRdd = resultRdd.mapPartitionsWithIndex((index,iter)=> if(index == 0) iter.drop(2) else iter).map(_.split(separator))
        if (!inferSchema) {
          val schema = new StructType(col.map(field => StructField(field, StringType)))
          val rows = rowRdd.map(att => {
            val row = ArrayBuffer[Any]()
            for (i <- col.indices) {
              val filed = if ((allCatch opt att(i)).isDefined) {
                att(i)
              } else {
                warn("row is Error:" + att(0))
                "NULL"
              }
              row += filed
            }
            Row.fromSeq(row)
          })
          spark.createDataFrame(rows, schema)
        } else {
          RddToDFUtil.rddToDF(spark, rowRdd, col, Map("inferSchema" -> "true"))
        }
      }
      df.createOrReplaceTempView(tableName)
    }
  }

  def clearExpireTable(spark: SparkSession,
                       tableName: String): Unit ={
    if (spark.sessionState.catalog.getTempView(tableName).isDefined && spark.sqlContext.isCached(tableName)){
      spark.sqlContext.uncacheTable(tableName)
    }
  }
}
