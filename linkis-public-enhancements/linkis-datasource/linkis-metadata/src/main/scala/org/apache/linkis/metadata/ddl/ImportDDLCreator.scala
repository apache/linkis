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

package org.apache.linkis.metadata.ddl

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.metadata.conf.MdqConfiguration
import org.apache.linkis.metadata.domain.mdq.bo.{MdqTableBO, MdqTableFieldsInfoBO}
import org.apache.linkis.metadata.errorcode.LinkisMetadataErrorCodeSummary._
import org.apache.linkis.metadata.exception.MdqIllegalParamException
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.FileSystemUtils

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils

import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util
import java.util.Date

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

object ImportDDLCreator extends DDLCreator {

  override def createDDL(mdqTableInfo: MdqTableBO, user: String): String = {
    val importType = mdqTableInfo.getImportInfo.getImportType
    importType.intValue() match {
      case 0 => HiveImportDDLHelper.generateCode(mdqTableInfo, user)
      case 1 => FileImportDDLHelper.generateCode(mdqTableInfo, user)
      case 2 => FileImportDDLHelper.generateCode(mdqTableInfo, user)
      case x: Int => throw MdqIllegalParamException(s"unrecognized import type $x")
    }
  }

}

trait ImportHelper {
  def generateCode(mdqTableVO: MdqTableBO, user: String): String
}

object FileImportDDLHelper extends ImportHelper with Logging {

  private val CODE_STORE_PREFIX = CommonVars("wds.linkis.mdq.store.prefix", "hdfs:///apps-data/")
  private val CODE_STORE_SUFFIX = CommonVars("wds.linkis.mdq.store.suffix", "")
  private val CHARSET = "utf-8"
  private val CODE_SPLIT = ";"
  private val LENGTH_SPLIT = "#"

  override def generateCode(mdqTableBO: MdqTableBO, user: String): String = {
    logger.info(
      s"begin to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} using File way"
    )
    var createTableCode = new StringBuilder
    val importInfo = mdqTableBO.getImportInfo
    val _source =
      if (StringUtils.isEmpty(importInfo.getSource)) {
        throw MdqIllegalParamException(IMPORT_HIVE_SOURCE_IS_NULL.getErrorDesc)
      } else {
        importInfo.getSource
      }
    val _destination =
      if (StringUtils.isEmpty(importInfo.getDestination)) {
        throw MdqIllegalParamException(IMPORT_HIVE_SOURCE_IS_NULL.getErrorDesc)
      } else {
        importInfo.getDestination
      }
    val source = "val source = \"\"\"" + _source + "\"\"\"\n"
    createTableCode.append(source)
    val storePath = storeExecutionCode(_destination, user)
    if (null == storePath) {
      val destination = "val destination = \"\"\"" + _destination + "\"\"\"\n"
      createTableCode.append(destination)
//      createTableCode.append("org.apache.linkis.engine.imexport.LoadData.loadDataToTable(spark,source,destination)")
      createTableCode.append(
        MdqConfiguration.SPARK_MDQ_IMPORT_CLAZZ.getValue + ".loadDataToTable(spark,source,destination)"
      )
    } else {
      val destination = "val destination = \"\"\"" + storePath + "\"\"\"\n"
      createTableCode.append(destination)
      createTableCode.append(
        MdqConfiguration.SPARK_MDQ_IMPORT_CLAZZ.getValue + ".loadDataToTableByFile(spark,destination,source)"
      )
    }
    val resultCode = createTableCode.toString()
    logger.info(
      s"end to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} code is $resultCode"
    )
    resultCode
    //    if(storePath == null) {
    //      newExecutionCode += "org.apache.linkis.engine.imexport.LoadData.loadDataToTable(spark,source,destination)"
    //    }else{
    //      newExecutionCode += "org.apache.linkis.engine.imexport.LoadData.loadDataToTableByFile(spark,destination,source)"
    //    }
  }

  def storeExecutionCode(destination: String, user: String): String = {
    if (destination.length < 60000) return null
    val path: String = getCodeStorePath(user)
    val fsPath: FsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, user).asInstanceOf[FileSystem]
    fileSystem.init(null)
    var os: OutputStream = null
    var position = 0L
    val codeBytes = destination.getBytes(CHARSET)
    Utils.tryFinally {
      path.intern() synchronized {
        if (!fileSystem.exists(fsPath)) FileSystemUtils.createNewFile(fsPath, user, true)
        os = fileSystem.write(fsPath, false)
        position = fileSystem.get(path).getLength
        IOUtils.write(codeBytes, os)
      }
    } {
      if (fileSystem != null) fileSystem.close()
      IOUtils.closeQuietly(os)
    }
    val length = codeBytes.length
    path + CODE_SPLIT + position + LENGTH_SPLIT + length
  }

  private def getCodeStorePath(user: String): String = {
    val date: String = DateFormatUtils.format(new Date, "yyyyMMdd")
    s"${CODE_STORE_PREFIX.getValue}${user}${CODE_STORE_SUFFIX.getValue}/executionCode/${date}/_bgservice"
  }

}

/**
 * hive表导入新的hive表，需要有以下四种情况要判断
 * 1.非分区表导入非分区表 2.非分区表导入分区表,全表插入一个分区 3.分区表导入非分区表 4.分区表导入分区表 4.1 源分区表含有 ds 字段,动态分区插入 4.2 源分区表不含有 ds
 * 字段,全表插入一个分区
 */

object HiveImportDDLHelper extends ImportHelper with SQLConst with Logging {
  val DATABASE: String = "database"
  val TABLE: String = "table"
  val AS_SELECT: String = "select"
  val FROM: String = "from" + SPACE
  val DEFAULT_PARTITION_NAME = CommonVars("bdp.dataworkcloud.datasource.default.par.name", "ds")
  // 默认就是以ds作为分区
  val DEFAULT_PARTITION_DESC: String = PARTITIONED_BY + """(ds string)"""
  val STORED_AS_ORC: String = STORED_AS + SPACE + MdqConfiguration.DEFAULT_STORED_TYPE.getValue
  val INSERT_OVERWRITE: String = "insert overwrite table" + SPACE
  val DYNAMIC_MODE: String = """spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")"""
  val DYNAMIC_PAR: String = """spark.sql("set hive.exec.dynamic.partition=true")"""

  override def generateCode(mdqTableBO: MdqTableBO, user: String): String = {
    logger.info(
      s"begin to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} using Hive way"
    )
    val executeCode = new StringBuilder
    val args = mdqTableBO.getImportInfo.getArgs
    val destinationDatabase = mdqTableBO.getTableBaseInfo.getBase.getDatabase
    val destinationTable = mdqTableBO.getTableBaseInfo.getBase.getName
    if (StringUtils.isEmpty(destinationDatabase) || StringUtils.isEmpty(destinationTable)) {
      logger.error("Hive create table destination database or tablename is null")
      throw MdqIllegalParamException(HIVE_CREATE_IS_NULL.getErrorDesc)
    }
    val sourceDatabase =
      if (StringUtils.isEmpty(args.get(DATABASE))) {
        throw MdqIllegalParamException(HIVE_CREATE__TABLE_IS_NULL.getErrorDesc)
      } else {
        args.get(DATABASE)
      }
    val sourceTableName =
      if (StringUtils.isEmpty(args.get(TABLE))) {
        throw MdqIllegalParamException(HIVE_CREATE__TABLE_IS_NULL.getErrorDesc)
      } else {
        args.get(TABLE)
      }
    // 判断目标表是否是分区表，如果是分区表，先建表
    val isPartitionTable = mdqTableBO.getTableBaseInfo.getBase.getPartitionTable
    if (isPartitionTable != null && isPartitionTable == true) {
      // 是分区表，先建表，再插入数据
      executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
      executeCode
        .append(CREATE_TABLE)
        .append(destinationDatabase)
        .append(".")
        .append(destinationTable)
        .append(SPACE)
      executeCode.append(LEFT_PARENTHESES)
      val fields = mdqTableBO.getTableFieldsInfo
      val createFieldsArray = new ArrayBuffer[String]()
      val insertFieldsArray = new ArrayBuffer[String]()
      var dsCount = 0
      var partitionValue: String = null
      // 建表
      fields.asScala foreach { field =>
        val name = field.getName
        val _type = field.getType
        val desc = field.getComment
        if (!DEFAULT_PARTITION_NAME.getValue.equals(name)) {
          if (StringUtils.isNotEmpty(desc)) {
            createFieldsArray += (name + SPACE + _type + SPACE + COMMENT + SPACE + SINGLE_MARK + desc + SINGLE_MARK)
            insertFieldsArray += name
          } else {
            createFieldsArray += (name + SPACE + _type)
            insertFieldsArray += name
          }
        } else {
          dsCount += 1
          if (StringUtils.isNotBlank(field.getPartitionsValue)) {
            partitionValue = field.getPartitionsValue
          }
        }
      }
      executeCode
        .append(createFieldsArray.mkString(COMMA + SPACE))
        .append(RIGHT_PARENTHESES)
        .append(SPACE)
      executeCode.append(DEFAULT_PARTITION_DESC).append(SPACE)
      executeCode.append(STORED_AS_ORC)
      executeCode.append(MARKS).append(RIGHT_PARENTHESES).append(LINE_BREAK)
      // 判断源表是否是分区表
      val isSourceTablePartition: Boolean = checkPartitionTable(fields)
      val standardDs =
        if (StringUtils.isNotBlank(partitionValue)) {
          partitionValue
        } else {
          new SimpleDateFormat("yyyyMMdd").format(new java.util.Date(System.currentTimeMillis()))
        }
      if (!isSourceTablePartition) {
        // 插入数据
        executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
        executeCode
          .append(INSERT_OVERWRITE + destinationDatabase + "." + destinationTable)
          .append(SPACE)
        executeCode
          .append("partition")
          .append(LEFT_PARENTHESES)
          .append("ds=")
          .append("\"")
          .append(standardDs)
          .append("\"")
          .append(RIGHT_PARENTHESES)
          .append(SPACE)
        executeCode
          .append(AS_SELECT)
          .append(SPACE)
          .append(insertFieldsArray.mkString(COMMA))
          .append(SPACE)
        executeCode.append(FROM).append(sourceDatabase).append(".").append(sourceTableName)
        executeCode.append(MARKS).append(RIGHT_PARENTHESES)
      } else {
        // 如果源表有ds字段，那么doubleDs为true,如果为true，需要设置动态分区插入
        val doubleDs: Boolean = dsCount >= 2
        if (doubleDs) {
          // 动态分区插入
          executeCode.append(DYNAMIC_PAR).append(LINE_BREAK)
          executeCode.append(DYNAMIC_MODE).append(LINE_BREAK)
          executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
          executeCode
            .append(INSERT_OVERWRITE + destinationDatabase + "." + destinationTable)
            .append(SPACE)
          executeCode
            .append("partition")
            .append(LEFT_PARENTHESES)
            .append("ds")
            .append(RIGHT_PARENTHESES)
            .append(SPACE)
          executeCode
            .append(AS_SELECT)
            .append(SPACE)
            .append(insertFieldsArray.mkString(COMMA))
            .append(COMMA)
            .append("ds")
            .append(SPACE)
          executeCode.append(FROM).append(sourceDatabase).append(".").append(sourceTableName)
          executeCode.append(MARKS).append(RIGHT_PARENTHESES)
        } else {
          // 直接插入
          executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
          executeCode
            .append(INSERT_OVERWRITE + destinationDatabase + "." + destinationTable)
            .append(SPACE)
          executeCode
            .append("partition")
            .append(LEFT_PARENTHESES)
            .append("ds=")
            .append("\"")
            .append(standardDs)
            .append("\"")
            .append(RIGHT_PARENTHESES)
            .append(SPACE)
          executeCode
            .append(AS_SELECT)
            .append(SPACE)
            .append(insertFieldsArray.mkString(COMMA))
            .append(SPACE)
          executeCode.append(FROM).append(sourceDatabase).append(".").append(sourceTableName)
          executeCode.append(MARKS).append(RIGHT_PARENTHESES)
        }
      }
    } else {
      // 如果目标表不是分区表，直接将create table as select * from ...
      executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
      executeCode
        .append(CREATE_TABLE)
        .append(destinationDatabase)
        .append(".")
        .append(destinationTable)
        .append(SPACE)
      executeCode.append(AS_SELECT).append(SPACE)
      val fields = mdqTableBO.getTableFieldsInfo
      if (fields.isEmpty) {
        // 如果是空 默认用 *
        executeCode.append("*").append(SPACE)
      } else {
        val fieldArr = new ArrayBuffer[String]()
        fields.asScala filter (_ != null) foreach (fieldArr += _.getName)
        executeCode.append(fieldArr.mkString(", ")).append(SPACE)
      }
      executeCode
        .append(FROM)
        .append(SPACE)
        .append(sourceDatabase)
        .append(".")
        .append(sourceTableName)
      executeCode.append(MARKS).append(RIGHT_PARENTHESES)
    }
    val resultCode = executeCode.toString()
    logger.info(
      s"end to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} code is $resultCode"
    )
    resultCode
  }

  def checkPartitionTable(fields: util.List[MdqTableFieldsInfoBO]): Boolean = {
    var count = 0
    fields.asScala foreach { field =>
      if (field.getPartitionField != null && field.getPartitionField) count += 1
    }
    count >= 2
  }

}
