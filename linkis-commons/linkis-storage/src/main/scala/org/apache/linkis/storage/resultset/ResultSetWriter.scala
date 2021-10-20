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
 
package org.apache.linkis.storage.resultset

import org.apache.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import org.apache.linkis.common.io.{FsPath, MetaData, Record}

import scala.collection.mutable.ArrayBuffer


object ResultSetWriter {
  def getResultSetWriter[K <: MetaData, V <: Record](resultSet: ResultSet[K,V], maxCacheSize: Long, storePath: FsPath):ResultSetWriter[K, V] =
    new StorageResultSetWriter[K, V](resultSet, maxCacheSize, storePath)

  def getResultSetWriter[K <: MetaData, V <: Record](resultSet: ResultSet[K,V], maxCacheSize: Long, storePath: FsPath, proxyUser:String):ResultSetWriter[K, V] ={
    val writer = new StorageResultSetWriter[K, V](resultSet, maxCacheSize, storePath)
    writer.setProxyUser(proxyUser)
    writer
  }


  def getRecordByWriter(writer: ResultSetWriter[_ <:MetaData,_ <:Record],limit:Long): Array[Record] ={
    val res = writer.toString
    getRecordByRes(res,limit)
  }

  def getRecordByRes(res: String,limit:Long): Array[Record] ={
    val reader = ResultSetReader.getResultSetReader(res)
    var count = 0
    val records = new ArrayBuffer[Record]()
    reader.getMetaData
    while (reader.hasNext && count < limit){
      records += reader.getRecord
      count = count + 1
    }
    records.toArray
  }

  def getLastRecordByRes(res: String):Record = {
    val reader = ResultSetReader.getResultSetReader(res)
    reader.getMetaData
    while (reader.hasNext ){
     reader.getRecord
    }
    reader.getRecord
  }
}
