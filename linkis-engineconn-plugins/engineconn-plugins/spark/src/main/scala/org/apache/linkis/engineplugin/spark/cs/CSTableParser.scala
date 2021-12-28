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
 
package org.apache.linkis.engineplugin.spark.cs

import java.util.regex.Pattern

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.cs.client.service.CSTableService
import org.apache.linkis.cs.common.entity.metadata.CSTable
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.exception.ExecuteError
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.csv.DolphinToSpark

import scala.collection.mutable.ArrayBuffer


class CSTableParser extends Logging {

  private val pb: Pattern = Pattern.compile(CSCommonUtils.CS_TMP_TABLE_PREFIX + "[^\\s\";'()]+[$\\s]{0,1}", Pattern.CASE_INSENSITIVE)

  private val DB = "default."


  private def getCSTempTable(code: String): Array[String] = {
    val bmlResourceNames = new ArrayBuffer[String]()
    val mb = pb.matcher(code)
    while (mb.find) bmlResourceNames.append(mb.group.trim)
    bmlResourceNames.toArray
  }

  /**
    * 1. code parse cs_tamp
    * 2. getCSTable
    * 3. registerTable：暂时用dopphin，后续修改为拼接sql语句
    *
    * @param engineExecutorContext
    * @param code
    * @param contextIDValueStr
    * @param nodeNameStr
    * @return
    */
  def parse(engineExecutorContext: EngineExecutionContext, code: String, contextIDValueStr: String, nodeNameStr: String): String = {
    val csTempTables = getCSTempTable(code)
    val parsedTables = new ArrayBuffer[String]()
    csTempTables.foreach{ csTempTable =>
      val table = getCSTable(csTempTable, contextIDValueStr, nodeNameStr)
      if (null == table){
        throw new ExecuteError(40007,s"The csTable that name is $csTempTable not found in cs")
      }
      registerTempTable(table)
      parsedTables.append(csTempTable)
    }
    StringUtils.replaceEach(code, csTempTables,parsedTables.toArray)
  }

  /**
    * TODO From cs to get csTable
    * Exact Match
    * @param csTempTable
    * @return
    */
  def getCSTable(csTempTable:String,  contextIDValueStr: String, nodeNameStr: String):CSTable = {
    CSTableService.getInstance().getUpstreamSuitableTable(contextIDValueStr, nodeNameStr, csTempTable)
  }

  def registerTempTable(csTable: CSTable):Unit = {
    val spark = SparkSession.builder().enableHiveSupport().getOrCreate()
    info(s"Start to create  tempView to sparkSession viewName(${csTable.getName}) location(${csTable.getLocation})")
    DolphinToSpark.createTempView(spark, csTable.getName, csTable.getLocation, true)
    info(s"Finished to create  tempView to sparkSession viewName(${csTable.getName}) location(${csTable.getLocation})")
  }
}
