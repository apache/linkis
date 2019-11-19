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
package com.webank.wedatasphere.linkis.metadata.ddl

import java.text.SimpleDateFormat
import java.util

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.metadata.conf.MdqConfiguration
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.{MdqTableBO, MdqTableFieldsInfoBO}
import com.webank.wedatasphere.linkis.metadata.exception.MdqIllegalParamException
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object ImportDDLCreator extends DDLCreator {



  override def createDDL(mdqTableInfo:MdqTableBO, user:String): String = {
    val importType = mdqTableInfo.getImportInfo.getImportType
    importType.intValue() match {
      case 0 => HiveImportDDLHelper.generateCode(mdqTableInfo)
      case 1 => FileImportDDLHelper.generateCode(mdqTableInfo)
      case 2 => FileImportDDLHelper.generateCode(mdqTableInfo)
      case x:Int => throw MdqIllegalParamException(s"unrecognized import type $x")
    }
  }
}


trait ImportHelper{
  def generateCode(mdqTableVO: MdqTableBO):String
}

object FileImportDDLHelper extends ImportHelper with Logging {
  override def generateCode(mdqTableBO: MdqTableBO): String = {
    logger.info(s"begin to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} using File way")
    var createTableCode = new StringBuilder
    val importInfo = mdqTableBO.getImportInfo
    val args = importInfo.getArgs
    val _source = if (StringUtils.isEmpty(importInfo.getSource)) throw MdqIllegalParamException("import hive source is null")
    else importInfo.getSource
    val _destination = if (StringUtils.isEmpty(importInfo.getDestination)) throw MdqIllegalParamException("import hive source is null")
    else importInfo.getDestination
    val source = "val source = \"\"\"" + _source + "\"\"\"\n"
    var destination = "val destination = \"\"\"" + _destination +  "\"\"\"\n"
    createTableCode.append(source)
    createTableCode.append(destination)
    createTableCode.append("com.webank.bdp.dataworkcloud.engine.imexport.LoadData.loadDataToTable(spark,source,destination)")
    val resultCode = createTableCode.toString()
    logger.info(s"end to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} code is $resultCode")
    resultCode
//    if(storePath == null){
//      newExecutionCode += "com.webank.bdp.dataworkcloud.engine.imexport.LoadData.loadDataToTable(spark,source,destination)"
//    }else{
//      newExecutionCode += "com.webank.bdp.dataworkcloud.engine.imexport.LoadData.loadDataToTableByFile(spark,destination,source)"
//    }
  }
}

/**
  * hive表导入新的hive表，需要有以下四种情况要判断
  * 1.非分区表导入非分区表
  * 2.非分区表导入分区表,全表插入一个分区
  * 3.分区表导入非分区表
  * 4.分区表导入分区表
  *    4.1 源分区表含有 ds 字段,动态分区插入
  *    4.2 源分区表不含有 ds 字段,全表插入一个分区
  */

object HiveImportDDLHelper extends ImportHelper with SQLConst with Logging{
  val DATABASE:String = "database"
  val TABLE:String = "table"
  val AS_SELECT:String = "select"
  val FROM:String = "from" + SPACE
  val DEFAULT_PARTITION_NAME = CommonVars("bdp.dataworkcloud.datasource.default.par.name", "ds")
  //默认就是以ds作为分区
  val DEFAULT_PARTITION_DESC:String = PARTITIONED_BY + """(ds string)"""
  val STORED_AS_ORC:String = STORED_AS + SPACE + MdqConfiguration.DEFAULT_STORED_TYPE.getValue
  val INSERT_OVERWRITE:String = "insert overwrite table" + SPACE
  val DYNAMIC_MODE:String = """spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")"""
  val DYNAMIC_PAR:String = """spark.sql("set hive.exec.dynamic.partition=true")"""
  override def generateCode(mdqTableBO: MdqTableBO): String = {
    logger.info(s"begin to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} using Hive way")
    val executeCode = new StringBuilder
    val args = mdqTableBO.getImportInfo.getArgs
    val destinationDatabase = mdqTableBO.getTableBaseInfo.getBase.getDatabase
    val destinationTable = mdqTableBO.getTableBaseInfo.getBase.getName
    if (StringUtils.isEmpty(destinationDatabase) || StringUtils.isEmpty(destinationTable)){
      logger.error("Hive create table destination database or tablename is null")
      throw MdqIllegalParamException("Hive create table destination database or tablename is null")
    }
    val sourceDatabase = if (StringUtils.isEmpty(args.get(DATABASE))) throw MdqIllegalParamException("hive create table source database is null")
    else args.get(DATABASE)
    val sourceTableName = if (StringUtils.isEmpty(args.get(TABLE))) throw MdqIllegalParamException("hive create table source table name is null")
    else args.get(TABLE)
    //判断目标表是否是分区表，如果是分区表，先建表
    val isPartitionTable = mdqTableBO.getTableBaseInfo.getBase.getPartitionTable
    if (isPartitionTable != null && isPartitionTable == true){
      //是分区表，先建表，再插入数据
      executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
      executeCode.append(CREATE_TABLE).append(destinationDatabase).append(".").append(destinationTable).append(SPACE)
      executeCode.append(LEFT_PARENTHESES)
      val fields = mdqTableBO.getTableFieldsInfo
      val createFieldsArray = new ArrayBuffer[String]()
      val insertFieldsArray = new ArrayBuffer[String]()
      var dsCount = 0
      var partitionValue:String = null
      //建表
      fields foreach  {
        field => val name = field.getName
          val _type = field.getType
          val desc = field.getComment
          if (!DEFAULT_PARTITION_NAME.getValue.equals(name)){
            if (StringUtils.isNotEmpty(desc)){
              createFieldsArray += (name + SPACE + _type + SPACE + COMMENT + SPACE + SINGLE_MARK + desc + SINGLE_MARK)
              insertFieldsArray += name
            }else{
              createFieldsArray += (name + SPACE + _type)
              insertFieldsArray += name
            }
          }else{
            dsCount += 1
            if (StringUtils.isNotBlank(field.getPartitionsValue)) partitionValue = field.getPartitionsValue
          }
      }
      executeCode.append(createFieldsArray.mkString(COMMA + SPACE)).append(RIGHT_PARENTHESES).append(SPACE)
      executeCode.append(DEFAULT_PARTITION_DESC).append(SPACE)
      executeCode.append(STORED_AS_ORC)
      executeCode.append(MARKS).append(RIGHT_PARENTHESES).append(LINE_BREAK)
      //判断源表是否是分区表
      val isSourceTablePartition:Boolean = checkPartitionTable(fields)
      val standardDs = if (StringUtils.isNotBlank(partitionValue)) partitionValue else
        new SimpleDateFormat("yyyyMMdd").format(new java.util.Date(System.currentTimeMillis()))
      if (!isSourceTablePartition){
        //插入数据
        executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
        executeCode.append(INSERT_OVERWRITE + destinationDatabase + "." + destinationTable).append(SPACE)
        executeCode.append("partition").append(LEFT_PARENTHESES).append("ds=").append(standardDs).append(RIGHT_PARENTHESES).append(SPACE)
        executeCode.append(AS_SELECT).append(SPACE).append(insertFieldsArray.mkString(COMMA)).append(SPACE)
        executeCode.append(FROM).append(sourceDatabase).append(".").append(sourceTableName)
        executeCode.append(MARKS).append(RIGHT_PARENTHESES)
      }else{
        //如果源表有ds字段，那么doubleDs为true,如果为true，需要设置动态分区插入
        val doubleDs:Boolean = dsCount >= 2
        if (doubleDs){
          //动态分区插入
          executeCode.append(DYNAMIC_PAR).append(LINE_BREAK)
          executeCode.append(DYNAMIC_MODE).append(LINE_BREAK)
          executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
          executeCode.append(INSERT_OVERWRITE + destinationDatabase + "." + destinationTable).append(SPACE)
          executeCode.append("partition").append(LEFT_PARENTHESES).append("ds").append(RIGHT_PARENTHESES).append(SPACE)
          executeCode.append(AS_SELECT).append(SPACE).append(insertFieldsArray.mkString(COMMA)).append(COMMA).append("ds").append(SPACE)
          executeCode.append(FROM).append(sourceDatabase).append(".").append(sourceTableName)
          executeCode.append(MARKS).append(RIGHT_PARENTHESES)
        }else{
          //直接插入
          executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
          executeCode.append(INSERT_OVERWRITE + destinationDatabase + "." + destinationTable).append(SPACE)
          executeCode.append("partition").append(LEFT_PARENTHESES).append("ds=").append(standardDs).append(RIGHT_PARENTHESES).append(SPACE)
          executeCode.append(AS_SELECT).append(SPACE).append(insertFieldsArray.mkString(COMMA)).append(SPACE)
          executeCode.append(FROM).append(sourceDatabase).append(".").append(sourceTableName)
          executeCode.append(MARKS).append(RIGHT_PARENTHESES)
        }
      }
    }else{
      // 如果目标表不是分区表，直接将create table as select * from ...
      executeCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS)
      executeCode.append(CREATE_TABLE).append(destinationDatabase).append(".").append(destinationTable).append(SPACE)
      executeCode.append(AS_SELECT).append(SPACE)
      val fields = mdqTableBO.getTableFieldsInfo
      if (fields.isEmpty){
        //如果是空 默认用 *
        executeCode.append("*").append(SPACE)
      }else{
        val fieldArr = new ArrayBuffer[String]()
        fields filter (_ != null)  foreach (fieldArr += _.getName)
        executeCode.append(fieldArr.mkString(", ")).append(SPACE)
      }
      executeCode.append(FROM).append(SPACE).append(sourceDatabase).append(".").append(sourceTableName)
      executeCode.append(MARKS).append(RIGHT_PARENTHESES)
    }
    val resultCode = executeCode.toString()
    logger.info(s"end to generate code for ${mdqTableBO.getTableBaseInfo.getBase.getName} code is $resultCode")
    resultCode
  }

  def checkPartitionTable(fields:util.List[MdqTableFieldsInfoBO]):Boolean = {
    var count = 0
    fields foreach {
      field => if(field.getPartitionField != null && field.getPartitionField) count += 1
    }
    count >= 2
  }



}


