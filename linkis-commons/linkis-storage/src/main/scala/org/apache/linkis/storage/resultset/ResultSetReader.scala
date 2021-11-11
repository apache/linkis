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

import java.io.InputStream
import org.apache.linkis.common.io.resultset.{ResultSet, ResultSetReader}
import org.apache.linkis.common.io.{FsPath, MetaData, Record}
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.exception.StorageErrorException
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord, TableResultSet}


object ResultSetReader {

  def getResultSetReader[K <: MetaData, V <: Record](resultSet: ResultSet[K, V], inputStream: InputStream): ResultSetReader[K, V] = {
    new StorageResultSetReader[K, V](resultSet, inputStream)
  }

  def getResultSetReader[K <: MetaData, V <: Record](resultSet: ResultSet[K, V], value: String): ResultSetReader[K, V] = {
    new StorageResultSetReader[K, V](resultSet, value)
  }

  def getResultSetReader(res: String):ResultSetReader[_ <: MetaData, _ <: Record]= {
    val rsFactory = ResultSetFactory.getInstance
    if (rsFactory.isResultSet(res)) {
      val resultSet = rsFactory.getResultSet(res)
      ResultSetReader.getResultSetReader(resultSet, res)
    }else {
      val resPath = new FsPath(res)
      val resultSet = rsFactory.getResultSetByPath(resPath)
      val fs = FSFactory.getFs(resPath)
      fs.init(null)
      ResultSetReader.getResultSetReader(resultSet, fs.read(resPath))
    }
  }

  def getTableResultReader(res: String):ResultSetReader[TableMetaData,TableRecord] = {
    val rsFactory = ResultSetFactory.getInstance
    if (rsFactory.isResultSet(res)) {
      val resultSet = rsFactory.getResultSet(res)
      if (ResultSetFactory.TABLE_TYPE != resultSet.resultSetType()) {
        throw new StorageErrorException(52002, "Result sets that are not tables are not supported(不支持不是表格的结果集)")
      }
      ResultSetReader.getResultSetReader(resultSet.asInstanceOf[TableResultSet], res)
    }else {
      val resPath = new FsPath(res)
      val resultSet = rsFactory.getResultSetByPath(resPath)
      if (ResultSetFactory.TABLE_TYPE != resultSet.resultSetType()) {
        throw new StorageErrorException(52002, "Result sets that are not tables are not supported(不支持不是表格的结果集)")
      }
      val fs = FSFactory.getFs(resPath)
      fs.init(null)
      ResultSetReader.getResultSetReader(resultSet.asInstanceOf[TableResultSet], fs.read(resPath))
    }
  }

}
