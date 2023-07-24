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

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.imexport.util.{BackGroundServiceUtils, ImExportUtils}
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.hadoop.common.utils.HDFSUtils
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.storage.excel.XlsUtils

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.IOUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

import java.io.{BufferedInputStream, File, FileInputStream}
import java.util.Locale

import scala.collection.JavaConverters._

/**
 */
object LoadData {

  def loadDataToTable(spark: SparkSession, source: String, destination: String): Unit = {
    val src = BDPJettyServerHelper.jacksonJson
      .readValue(source, classOf[java.util.HashMap[String, Object]])
      .asScala
      .toMap
    val dst = BDPJettyServerHelper.jacksonJson
      .readValue(destination, classOf[java.util.HashMap[String, Object]])
      .asScala
      .toMap
    create_table_from_a_file(spark, src, dst)
  }

  def loadDataToTableByFile(spark: SparkSession, destinationPath: String, source: String): Unit = {
    val destination = BackGroundServiceUtils.exchangeExecutionCode(destinationPath)
    val src = BDPJettyServerHelper.jacksonJson
      .readValue(source, classOf[java.util.HashMap[String, Object]])
      .asScala
      .toMap
    val dst = BDPJettyServerHelper.jacksonJson
      .readValue(destination, classOf[java.util.HashMap[String, Object]])
      .asScala
      .toMap
    create_table_from_a_file(spark, src, dst)
  }

  def create_table_from_a_file(
      spark: SparkSession,
      source: Map[String, Any],
      destination: Map[String, Any]
  ): Unit = {

    var path = source.getOrElse("path", "").toString
    val pathType = source.getOrElse("pathType", "share").toString
    var hasHeader = Utils.tryCatch(source.getOrElse("hasHeader", false).toString.toBoolean) {
      case e: Exception => false
    }
    val sheetName = source.getOrElse("sheet", "Sheet1").toString
    val dateFormat = source.getOrElse("dateFormat", "yyyy-MM-dd").toString
    val suffix = path.substring(path.lastIndexOf("."))
    val sheetNames = sheetName.split(",").toBuffer.asJava

    var fs: FileSystem = null
    val database = destination.getOrElse("database", "").toString
    val tableName = destination.getOrElse("tableName", "").toString

    val importData = Utils.tryCatch(destination.getOrElse("importData", true).toString.toBoolean) {
      case e: Exception => true
    }
    val isPartition = Utils.tryCatch {
      destination.getOrElse("isPartition", true).toString.toBoolean
    } { case e: Exception =>
      val flag = BigInt(destination.getOrElse("isPartition", 0).toString)
      if (flag == 1) true else false
    }
    val isOverwrite =
      Utils.tryCatch(destination.getOrElse("isOverwrite", false).toString.toBoolean) {
        case e: Exception => false
      }
    val partition = destination.getOrElse("partition", "ds").toString
    val partitionValue = destination.getOrElse("partitionValue", "1993-01-02").toString

    val columns = destination.getOrElse("columns", "").asInstanceOf[List[Map[String, Any]]]

    val dateFormats =
      columns.map(_.getOrElse("dateFormat", "yyyy-MM-dd").toString)
    var isFirst = true
    val dateFormatsJson = new StringBuilder()
    dateFormats.foreach(f => {
      if (isFirst) isFirst = false else dateFormatsJson.append(";")
      dateFormatsJson.append(f)
    })
    val indexesStr = String.join(",", columns.map(_.getOrElse("index", 0).toString).asJava)

    if ("hdfs".equalsIgnoreCase(pathType)) {
      if (".xls".equalsIgnoreCase(suffix)) {
        val config = HDFSUtils.getConfiguration(HadoopConf.HADOOP_ROOT_USER.getValue)
        config.setBoolean("fs.hdfs.impl.disable.cache", true)
        fs = HDFSUtils.getHDFSUserFileSystem(System.getProperty("user.name"), config)
        path = XlsUtils.excelToCsv(fs.open(new Path(path)), fs, hasHeader, sheetNames)
        hasHeader = false
      } else {
        path = if (SparkConfiguration.IS_VIEWFS_ENV.getValue) path else "hdfs://" + path
      }
    } else {
      if (".xlsx".equalsIgnoreCase(suffix)) {
        path = "file://" + path
      } else if (".xls".equalsIgnoreCase(suffix)) {
        val config = HDFSUtils.getConfiguration(HadoopConf.HADOOP_ROOT_USER.getValue)
        config.setBoolean("fs.hdfs.impl.disable.cache", true)
        fs = HDFSUtils.getHDFSUserFileSystem(System.getProperty("user.name"), config)
        path = XlsUtils.excelToCsv(new FileInputStream(path), fs, hasHeader, sheetNames)
        hasHeader = false
      } else {
        val config = HDFSUtils.getConfiguration(HadoopConf.HADOOP_ROOT_USER.getValue)
        config.setBoolean("fs.hdfs.impl.disable.cache", true)
        fs = HDFSUtils.getHDFSUserFileSystem(System.getProperty("user.name"), config)
        path = copyFileToHdfs(path, fs)
      }
    }

    val df = if (".xlsx".equalsIgnoreCase(suffix)) {
      // info(dateFormatsJson.toString()+ "----------")
      spark.read
        .format("com.webank.wedatasphere.spark.excel")
        .option("useHeader", hasHeader)
        .option("maxRowsInMemory", 100)
        .option("sheetName", sheetName)
        // .option("dateFormat", dateFormat)
        .option("indexes", indexesStr)
        .option("dateFormats", dateFormatsJson.toString())
        .schema(StructType(getFields(columns)))
        .load(path)
    } else {
      CsvRelation.csvToDF(spark, StructType(getFields(columns)), hasHeader, path, source, columns)
    }
    // warn(s"Fetched ${df.columns.length} col(s) : ${df.count()} row(s).")
    df.createOrReplaceTempView("tempTable")
    try {
      if (importData) {
        if (isPartition) {
          if (isOverwrite) {
            spark.sql(
              s"INSERT OVERWRITE TABLE  $database.$tableName partition($partition='$partitionValue') select * from tempTable"
            )
          } else {
            spark.sql(
              s"INSERT INTO  $database.$tableName partition($partition='$partitionValue') select * from tempTable"
            )
          }
        } else {
          if (isOverwrite) {
            spark.sql(s"INSERT OVERWRITE TABLE  $database.$tableName select * from tempTable")
          } else {
            spark.sql(s"INSERT INTO   $database.$tableName select * from tempTable")
          }
        }
      } else {
        if (spark.catalog.tableExists(database, tableName)) {
          spark.sql(s"drop table if exists $database.$tableName")
        }
        if (isPartition) {
          val columnSql = getColumnSql(columns)
          val sql =
            s"create table $database.$tableName($columnSql) PARTITIONED BY (`$partition` string) stored as orc tblproperties ('orc.compress'='SNAPPY')"
          spark.sql(sql)
          spark.sql(
            s"INSERT OVERWRITE TABLE  $database.$tableName partition($partition='$partitionValue') select * from tempTable"
          )
        } else {
          val columnSql = getColumnSql(columns)
          val sql =
            s"create table $database.$tableName($columnSql) stored as orc tblproperties ('orc.compress'='SNAPPY')"
          spark.sql(sql)
          spark.sql(s"INSERT OVERWRITE TABLE  $database.$tableName select * from tempTable")
        }
      }
    } catch {
      case t: Throwable =>
        if (!importData) {
          ImExportUtils.tryAndIngoreError(spark.sql(s"drop table $database.$tableName"))
        }
        throw t
    } finally {
      if (fs != null) {
        fs.delete(new Path(path), true)
        fs.close()
      }
    }
    // warn(s"create table $database $tableName Success")
  }

  def copyFileToHdfs(path: String, fs: FileSystem): String = {
    val file = new File(path)
    if (file.isDirectory) {
      throw new Exception("Import must be a file, not a directory(导入的必须是文件，不能是目录)")
    }
    val in = new BufferedInputStream(new FileInputStream(file))
    val hdfsPath =
      "/tmp/" + System.getProperty("user.name") + "/" + System.currentTimeMillis + file.getName
    val out = fs.create(new Path(hdfsPath), true)
    IOUtils.copyBytes(in, out, 4096)
    out.hsync()
    IOUtils.closeStream(in)
    IOUtils.closeStream(out)
    hdfsPath
  }

  def getMapValue[T](map: Map[String, Any], key: String, default: T = null.asInstanceOf[T]): T = {
    val value = map.get(key).map(_.asInstanceOf[T]).getOrElse(default)
    if (StringUtils.isEmpty(value.toString)) {
      default
    } else {
      value
    }
  }

  def getColumnSql(columns: List[Map[String, Any]]): String = {
    val sql = new StringBuilder
    columns.foreach { column =>
      val name =
        if (column("name") != null) column("name").asInstanceOf[String]
        else {
          throw new IllegalArgumentException(
            "When create a table, the field name must be defined(建立新表时，字段名必须定义)"
          )
        }
      sql.append("`").append(name).append("` ")
      val dataType =
        column.getOrElse("type", "string").asInstanceOf[String].toLowerCase(Locale.getDefault())
      sql.append(dataType)
      dataType match {
        case "char" | "varchar" =>
          val length = column.getOrElse("length", 20).toString.toInt
          sql.append(s"($length)")
        case "decimal" =>
          val precision = column.getOrElse("precision", 20).toString.toInt
          val scale = column.getOrElse("scale", 4).toString.toInt
          sql.append(s"($precision,$scale)")
        case _ =>
      }
      val comment = column.getOrElse("comment", "").toString
      if (StringUtils.isNotEmpty(comment)) {
        sql.append(" comment ").append(s"'$comment' ")
      }
      sql.append(",")
    }
    sql.toString.substring(0, sql.length - 1)
  }

  def getFields(columns: List[Map[String, Any]]): Array[StructField] = {
    columns.map { column =>
      val name =
        if (column("name") != null) column("name").asInstanceOf[String]
        else {
          throw new IllegalArgumentException(
            "When create a table, the field name must be defined(建立新表时，字段名必须定义)"
          )
        }
      val dataType = column.getOrElse("type", "string").asInstanceOf[String]
      val precision = Utils.tryCatch(column.getOrElse("precision", 20).toString.toInt) {
        case e: Exception => 20
      }
      val scale = Utils.tryCatch(column.getOrElse("scale", 4).toString.toInt) { case e: Exception =>
        4
      }
      StructField(name, toDataType(dataType.toLowerCase(Locale.getDefault), precision, scale), true)
    }.toArray
  }

  def toDataType(dataType: String, precision: Int, scale: Int): DataType = dataType match {
    case "void" | "null" => NullType
    case "string" | "char" | "varchar" => StringType
    case "boolean" => BooleanType
    case "short" => ShortType
    case "int" | "tinyint" | "integer" | "smallint" => IntegerType
    case "long" | "bigint" => LongType
    case "float" => FloatType
    case "double" => DoubleType
    case "date" => DateType
    case "timestamp" => TimestampType
    case "binary" => BinaryType
    case "decimal" => DecimalType(precision, scale)
    case _ => throw new IllegalArgumentException(s"unknown dataType $dataType.")
  }

}
