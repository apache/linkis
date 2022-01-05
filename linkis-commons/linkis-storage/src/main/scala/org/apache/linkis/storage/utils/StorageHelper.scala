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
 
package org.apache.linkis.storage.utils

import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}


object StorageHelper {

  def main(args: Array[String]): Unit = {
    if(args.length < 2) println("Usage method params eg:getTableResLines path")
    val method = args(0)
    val params = args.slice(1,args.length)
    Thread.sleep(10000L)

    method match {
      case "getTableResLines" => getTableResLines(params)
      case "getTableRes" => getTableRes(params)
      case "createNewFile" => createNewFile(params)
      case _ => println("There is no such method")
    }
  }

  /**
    * Get the number of table result set file lines(获得表格结果集文件行数)
    *
    * @param args
    */
  def  getTableResLines(args: Array[String]) = {
    val resPath = StorageUtils.getFsPath(args(0))
    val resultSetFactory = ResultSetFactory.getInstance
    val resultSet = resultSetFactory.getResultSetByType(ResultSetFactory.TABLE_TYPE)
    val fs = FSFactory.getFs(resPath)
    fs.init(null)
    val reader = ResultSetReader.getResultSetReader(resultSet,fs.read(resPath))
    val rmetaData = reader.getMetaData
    rmetaData.asInstanceOf[TableMetaData].columns.foreach(println)
    var num = 0
    Thread.sleep(10000L)
    while (reader.hasNext){
      reader.getRecord
      num = num + 1
    }
    println(num)
    reader.close()
  }

  def getTableRes(args: Array[String]): Unit ={
    val len = Integer.parseInt(args(1))
    val max = len + 10
    val resPath = StorageUtils.getFsPath(args(0))
    val resultSetFactory = ResultSetFactory.getInstance
    val resultSet = resultSetFactory.getResultSetByType(ResultSetFactory.TABLE_TYPE)
    val fs = FSFactory.getFs(resPath)
    fs.init(null)
    val reader = ResultSetReader.getResultSetReader(resultSet,fs.read(resPath))
    val rmetaData = reader.getMetaData
    rmetaData.asInstanceOf[TableMetaData].columns.foreach(println)
    rmetaData.asInstanceOf[TableMetaData].columns.map(_.columnName + ",").foreach(print)
    var num = 0
    while (reader.hasNext){
      num = num + 1
      if(num > max) return
      if(num > len){
        val record = reader.getRecord
        record.asInstanceOf[TableRecord].row.foreach{ value =>
          print(value.toString)
          print(",")
        }
        println()
      }
    }
  }

  def createNewFile(args: Array[String]): Unit ={
    val resPath = StorageUtils.getFsPath(args(0))
    val proxyUser = StorageUtils.getJvmUser
    FileSystemUtils.createNewFile(resPath, proxyUser,true)
    println("success")
  }

}
