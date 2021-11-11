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
 
package org.apache.linkis.metadata.ddl


import org.apache.linkis.common.utils.Logging
import org.apache.linkis.metadata.conf.MdqConfiguration
import org.apache.linkis.metadata.domain.mdq.bo.{MdqTableBO, MdqTableFieldsInfoBO}
import org.apache.linkis.metadata.exception.MdqIllegalParamException
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object ScalaDDLCreator extends DDLCreator with SQLConst with Logging{



  override def createDDL(tableInfo:MdqTableBO, user:String): String = {
    logger.info(s"begin to generate ddl for user $user using ScalaDDLCreator")
    val dbName = tableInfo.getTableBaseInfo.getBase.getDatabase
    val tableName = tableInfo.getTableBaseInfo.getBase.getName
    val fields = tableInfo.getTableFieldsInfo
    val createTableCode = new StringBuilder
    createTableCode.append(SPARK_SQL).append(LEFT_PARENTHESES).append(MARKS).append(CREATE_TABLE)
    createTableCode.append(dbName).append(".").append(tableName)
    createTableCode.append(LEFT_PARENTHESES)
    val partitions = new ArrayBuffer[MdqTableFieldsInfoBO]()
    val fieldsArray = new ArrayBuffer[String]()
    fields foreach {
      field =>
        if (field.getPartitionField != null && field.getPartitionField == true) partitions += field else{
          val name = field.getName
          val _type = field.getType
          val desc = field.getComment
          if (StringUtils.isNotEmpty(desc)){
            fieldsArray += (name + SPACE + _type + SPACE + COMMENT + SPACE + SINGLE_MARK + desc + SINGLE_MARK)
          }else{
            fieldsArray += (name + SPACE + _type)
          }
        }
    }
    createTableCode.append(fieldsArray.mkString(COMMA)).append(RIGHT_PARENTHESES).append(SPACE)
    if (partitions.nonEmpty){
      val partitionArr = new ArrayBuffer[String]()
      partitions foreach {
        p => val name = p.getName
          val _type = p.getType
          if (StringUtils.isEmpty(name) || StringUtils.isEmpty(_type)) throw MdqIllegalParamException("partition name or type is null")
          partitionArr += (name + SPACE + _type)
      }
      createTableCode.append(PARTITIONED_BY).append(LEFT_PARENTHESES).append(partitionArr.mkString(COMMA)).
        append(RIGHT_PARENTHESES).append(SPACE)
    }
    //如果是分区表，但是没有分区字段，默认是用ds做分区
    if(partitions.isEmpty && tableInfo.getTableBaseInfo.getBase.getPartitionTable){
      val partition = MdqConfiguration.DEFAULT_PARTITION_NAME.getValue
      val _type = "string"
      createTableCode.append(PARTITIONED_BY).append(LEFT_PARENTHESES).append(partition).append(SPACE).append(_type).
        append(RIGHT_PARENTHESES).append(SPACE)
    }
    createTableCode.append(STORED_AS).append(SPACE).append(MdqConfiguration.DEFAULT_STORED_TYPE.getValue).append(SPACE)
    createTableCode.append(MARKS)
    createTableCode.append(RIGHT_PARENTHESES)
    val finalCode = createTableCode.toString()
    logger.info(s"End to create ddl code, code is $finalCode")
    finalCode
  }

  def main(args: Array[String]): Unit = {
    val filePath = "E:\\data\\json\\data.json"
    val json = scala.io.Source.fromFile(filePath).mkString
    println(json)

   // val obj = new Gson().fromJson(json, classOf[MdqTableVO])
    //val sql = createDDL(obj, "hadoop")
    //println(System.currentTimeMillis())
    //println(sql)
  }


}
