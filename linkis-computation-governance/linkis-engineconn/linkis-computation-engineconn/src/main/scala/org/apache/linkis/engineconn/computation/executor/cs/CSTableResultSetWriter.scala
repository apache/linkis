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

package org.apache.linkis.engineconn.computation.executor.cs

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.cs.client.service.CSTableService
import org.apache.linkis.cs.common.entity.metadata.{CSColumn, CSTable}
import org.apache.linkis.storage.resultset.StorageResultSetWriter
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord, TableResultSet}
import org.apache.linkis.storage.utils.StorageUtils

import org.apache.commons.lang3.StringUtils

import java.util.Date

class CSTableResultSetWriter(
    tableResult: TableResultSet,
    maxCacheSize: Long,
    storePath: FsPath,
    contextIDStr: String,
    nodeName: String,
    alias: String
) extends StorageResultSetWriter[TableMetaData, TableRecord](tableResult, maxCacheSize, storePath)
    with Logging {

  override def close(): Unit = {
    Utils.tryCatch {
      registerToCS
    } { case t: Throwable =>
      logger.info("Failed to register tmp table", t)
    }
    super.close()
  }

  private def registerToCS: Unit = {

    if (StringUtils.isNotBlank(contextIDStr) && StringUtils.isNotBlank(nodeName) && !isEmpty) {
      logger.info("Start to register resultSet to cs")
      flush()
      val csTable = new CSTable
      csTable.setAlias(alias)
      csTable.setAvailable(true)
      csTable.setComment("cs temp table")
      csTable.setCreateTime(new Date())
      csTable.setCreator(StorageUtils.getJvmUser)
      csTable.setExternalUse(true)
      csTable.setImport(false)
      csTable.setLocation(toFSPath.getSchemaPath)
      csTable.setPartitionTable(false)
      csTable.setView(true)
      val csColumns = getMetaData.asInstanceOf[TableMetaData].columns.map { column =>
        val csColumn = new CSColumn
        csColumn.setName(column.columnName)
        csColumn.setType(column.dataType.getTypeName)
        csColumn.setComment(column.comment)
        csColumn
      }
      csTable.setColumns(csColumns)
      CSTableService.getInstance().registerCSTable(contextIDStr, nodeName, alias, csTable)
      logger.info("Finished to register resultSet to cs")
    }
  }

}
