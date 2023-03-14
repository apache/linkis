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

import org.apache.linkis.engineplugin.spark.imexport.util.ImExportUtils

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IOUtils, LongWritable, Text}
import org.apache.hadoop.mapred.TextInputFormat
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types._

import javax.xml.bind.DatatypeConverter

import java.io.{BufferedOutputStream, FileOutputStream}
import java.math.BigDecimal
import java.sql.{Date, Timestamp}
import java.text.SimpleDateFormat
import java.util.Locale

import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import scala.util.control.Exception._

/**
 */
class CsvRelation(@transient private val source: Map[String, Any]) extends Serializable {

  val fieldDelimiter = LoadData.getMapValue[String](source, "fieldDelimiter", "\t")
  val encoding = LoadData.getMapValue[String](source, "encoding", "utf-8")
  // val hasHeader = getNodeValue[Boolean](source, "hasHeader", false)
  val nullValue = LoadData.getMapValue[String](source, "nullValue", " ")
  val nanValue = LoadData.getMapValue[String](source, "nanValue", "null")

  var exportNullValue =
    LoadData.getMapValue[String](source, "exportNullValue", "SHUFFLEOFF") match {
      case "BLANK" => ""
      case s: String => s
    }

  val quote = LoadData.getMapValue[String](source, "quote", "\"")
  val escape = LoadData.getMapValue[String](source, "escape", "\\")
  val escapeQuotes = LoadData.getMapValue[Boolean](source, "escapeQuotes", false)

  val timestampFormat = new SimpleDateFormat(
    LoadData.getMapValue[String](source, "timestampFormat", "yyyy-mm-dd hh:mm:ss"),
    Locale.US
  )

  def transfer(sc: SparkContext, path: String, encoding: String): RDD[String] = {
    sc.hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], 1)
      .map(p => new String(p._2.getBytes, 0, p._2.getLength, encoding))
  }

  def csvToDataFrame(
      spark: SparkSession,
      schema: StructType,
      hasHeader: Boolean,
      path: String,
      columns: List[Map[String, Any]]
  ): DataFrame = {
    val rdd = if ("utf-8".equalsIgnoreCase(encoding)) {
      spark.sparkContext.textFile(path)
    } else {
      transfer(spark.sparkContext, path, encoding)
    }
    val indexs = getFilterColumnIndex(rdd, fieldDelimiter, columns, hasHeader)
    // header
    val tokenRdd = if (hasHeader) {
      rdd
        .mapPartitionsWithIndex((index, iter) => if (index == 0) iter.drop(1) else iter)
        .map(row => filterColumn(row, indexs, fieldDelimiter))
    } else {
      rdd.map(row => filterColumn(row, indexs, fieldDelimiter))
    }

    val rowRdd = buildScan(tokenRdd, schema, columns)
    spark.createDataFrame(rowRdd, schema)
  }

  def filterColumn(row: String, indexs: Array[Int], fieldDelimiter: String): Array[String] = {
    val data: Array[String] = row.split(fieldDelimiter)
    data.indices.filter(indexs.contains).map(data).toArray
  }

  def getFilterColumnIndex(
      rdd: RDD[String],
      fieldDelimiter: String,
      columns: List[Map[String, Any]],
      hasHeader: Boolean
  ): Array[Int] = {
    columns.map(_.getOrElse("index", 0).toString.toInt).toArray
  }

  def buildScan(
      tokenRdd: RDD[Array[String]],
      schema: StructType,
      columns: List[Map[String, Any]]
  ): RDD[Row] = {

    tokenRdd.map(att => {
      val row = ArrayBuffer[Any]()
      for (i <- schema.indices) {
        val field = if ((allCatch opt att(i)).isDefined) {
          att(i)
        } else {
          "null"
        }
        val data = if (nanValue.equalsIgnoreCase(field)) {
          null
        } else if (schema(i).dataType != StringType && (field.isEmpty || nullValue.equals(field))) {
          null
        } else {
          val dateFormat = columns(i).getOrElse("dateFormat", "yyyy-MM-dd").toString
          val format: SimpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US)
          castTo(field, schema(i).dataType, format, null)
        }
        row += data
      }
      Row.fromSeq(row)
    })
  }

  def castTo(
      field: String,
      dataType: DataType,
      dateFormatP: SimpleDateFormat,
      timestampFormatP: SimpleDateFormat
  ): Any = {
    val value = if (escapeQuotes) field.substring(1, field.length - 1) else field
    dataType match {
      case _: ByteType => value.toByte
      case _: ShortType => value.toShort
      case _: IntegerType => value.toInt
      case _: LongType => value.toLong
      case _: FloatType => value.toFloat
      case _: DoubleType => value.toDouble
      case _: BooleanType => value.toBoolean
      case dt: DecimalType =>
        val dataum = new BigDecimal(value.replaceAll(",", ""))
        Decimal(dataum, dt.precision, dt.scale)
      case _: TimestampType =>
        new Timestamp(
          Try(timestampFormat.parse(value).getTime).getOrElse(stringToTime(value).getTime * 1000L)
        )
      case _: DateType =>
        new Date(Try(dateFormatP.parse(value).getTime).getOrElse(stringToTime(value).getTime))
      case _: StringType => value.replaceAll("\n|\t", " ")
      case t => throw new RuntimeException(s"Unsupported cast from $value to $t")
    }
  }

  def stringToTime(s: String): java.util.Date = {
    val indexOfGMT = s.indexOf("GMT")
    if (indexOfGMT != -1) {
      // ISO8601 with a weird time zone specifier (2000-01-01T00:00GMT+01:00)
      val s0 = s.substring(0, indexOfGMT)
      val s1 = s.substring(indexOfGMT + 3)
      // Mapped to 2000-01-01T00:00+01:00
      stringToTime(s0 + s1)
    } else if (!s.contains('T')) {
      // JDBC escape string
      if (s.contains(' ')) {
        Timestamp.valueOf(s)
      } else {
        Date.valueOf(s)
      }
    } else {
      DatatypeConverter.parseDateTime(s).getTime()
    }
  }

  def saveDFToCsv(
      spark: SparkSession,
      df: DataFrame,
      path: String,
      hasHeader: Boolean = true,
      isOverwrite: Boolean = false
  ): Boolean = {
    val filesystemPath = new Path(path)
    // scalastyle:off hadoopconfiguration
    spark.sparkContext.hadoopConfiguration.setBoolean("fs.hdfs.impl.disable.cache", true)
    // scalastyle:off hadoopconfiguration
    val fs = filesystemPath.getFileSystem(spark.sparkContext.hadoopConfiguration)
    fs.setVerifyChecksum(false)
    fs.setWriteChecksum(false)
    // refresh nfs cache
    try {
      fs.listFiles(filesystemPath.getParent, false)
    } catch {
      case e: Throwable =>
    }

    val out = if (fs.exists(filesystemPath)) {
      if (isOverwrite) {
        new BufferedOutputStream(fs.create(filesystemPath, isOverwrite))
      } else {
        val bufferedOutputStream = if (path.startsWith("file:")) {
          new BufferedOutputStream(new FileOutputStream(path.substring("file://".length), true))
        } else {
          new BufferedOutputStream(fs.append(filesystemPath))
        }
        bufferedOutputStream.write("\n".getBytes(encoding))
        bufferedOutputStream
      }
    } else {
      new BufferedOutputStream(fs.create(filesystemPath, isOverwrite))
    }
    val iterator =
      ImExportUtils.tryAndThrowError(df.toLocalIterator, _ => spark.sparkContext.clearJobGroup())
    try {
      val schema = df.schema
      val header = new StringBuilder
      var index = 0
      for (col <- schema) {
        header ++= col.name ++ fieldDelimiter
      }
      if (hasHeader) {
        out.write(header.substring(0, header.lastIndexOf(fieldDelimiter)).getBytes(encoding))
      } else {
        if (iterator.hasNext) {
          out.write(getLine(schema, iterator.next()).getBytes(encoding))
          index += 1
        }
      }

      while (index < Int.MaxValue && iterator.hasNext) {
        val msg = "\n" + getLine(schema, iterator.next())
        out.write(msg.getBytes(encoding))
        index += 1
      }
      // warn(s"Fetched ${df.columns.length} col(s) : ${index} row(s).")
    } catch {
      case e: Throwable =>
        throw e
    } finally {
      spark.sparkContext.clearJobGroup()
      IOUtils.closeStream(out)
      fs.close()
    }
    true
  }

  def getLine(schema: StructType, row: Row): String = {
    val msg = new StringBuilder
    schema.indices.foreach { i =>
      val data = row(i) match {
        case value: String =>
          if (("NULL".equals(value) || "".equals(value)) && !"SHUFFLEOFF".equals(exportNullValue)) {
            exportNullValue
          } else {
            value.replaceAll("\n|\t", " ")
          }
        case value: Any => value.toString
        case _ => if ("SHUFFLEOFF".equals(exportNullValue)) "NULL" else exportNullValue
      }
      msg.append(data)
      msg.append(fieldDelimiter)
    }
    msg.substring(0, msg.lastIndexOf(fieldDelimiter))
  }

}

object CsvRelation {

  def saveDFToCsv(
      spark: SparkSession,
      df: DataFrame,
      path: String,
      hasHeader: Boolean = true,
      isOverwrite: Boolean = false,
      option: Map[String, Any] = Map()
  ): Boolean = {
    new CsvRelation(option).saveDFToCsv(spark, df, path, hasHeader, isOverwrite)
  }

  def csvToDF(
      spark: SparkSession,
      schema: StructType,
      hasHeader: Boolean,
      path: String,
      source: Map[String, Any] = Map(),
      columns: List[Map[String, Any]]
  ): DataFrame = {
    new CsvRelation(source).csvToDataFrame(spark, schema, hasHeader, path, columns)
  }

}
