/**
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
package com.webank.wedatasphere.linkis.cs.storage

import java.util.Date

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.cs.client.service.CSTableService
import com.webank.wedatasphere.linkis.cs.common.entity.metadata.{CSColumn, CSTable}
import com.webank.wedatasphere.linkis.storage.resultset.StorageResultSetWriter
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord, TableResultSet}
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.lang.StringUtils

/**
  * @author peacewong
  * @date 2020/4/1 21:03
  */
class CSTableResultSetWriter(tableResult: TableResultSet, maxCacheSize: Long,
                             storePath: FsPath, contextIDStr: String, nodeName: String, alias: String) extends StorageResultSetWriter[TableMetaData, TableRecord](tableResult, maxCacheSize, storePath) with Logging {

  override def toString: String = {
    try {
      registerToCS
    } catch {
      case t: Throwable =>
        info("Failed to register tmp table", t)
    }
    super.toString
  }

  private def registerToCS: Unit = {

    if (StringUtils.isNotBlank(contextIDStr) && StringUtils.isNotBlank(nodeName) && !isEmpty) {
      info("Start to register resultSet to cs")
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
        csColumn.setType(column.dataType.typeName)
        csColumn.setComment(column.comment)
        csColumn
      }
      csTable.setColumns(csColumns)
      CSTableService.getInstance().registerCSTable(contextIDStr, nodeName, alias, csTable)
      info("Finished to register resultSet to cs")
    }
  }
}
