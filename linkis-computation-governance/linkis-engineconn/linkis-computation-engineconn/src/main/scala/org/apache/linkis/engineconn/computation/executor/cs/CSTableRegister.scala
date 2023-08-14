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

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.io.resultset.ResultSetWriter
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.cs.client.service.CSTableService
import org.apache.linkis.cs.client.utils.{ContextServiceUtils, SerializeHelper}
import org.apache.linkis.cs.common.entity.enumeration.{ContextScope, ContextType}
import org.apache.linkis.cs.common.entity.metadata.{CSColumn, CSTable}
import org.apache.linkis.cs.common.entity.source.CommonContextKey
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.storage.domain.Column
import org.apache.linkis.storage.utils.StorageUtils

import org.apache.commons.lang3.StringUtils

import java.util.Date

object CSTableRegister extends Logging {

  def registerTempTable(
      engineExecutionContext: EngineExecutionContext,
      writer: ResultSetWriter[_ <: MetaData, _ <: Record],
      alias: String,
      columns: Array[Column]
  ): Unit = {

    val contextIDValueStr =
      ContextServiceUtils.getContextIDStrByMap(engineExecutionContext.getProperties)
    val nodeNameStr =
      ContextServiceUtils.getNodeNameStrByMap(engineExecutionContext.getProperties)

    if (StringUtils.isNotBlank(contextIDValueStr) && StringUtils.isNotBlank(nodeNameStr)) {
      logger.info(s"Start to register TempTable nodeName:$nodeNameStr")
      writer.flush()
      val tableName =
        if (StringUtils.isNotBlank(alias)) {
          s"${CSCommonUtils.CS_TMP_TABLE_PREFIX}${nodeNameStr}_${alias}"
        } else {
          var i = 1;
          var rsName: String = null;
          while (StringUtils.isEmpty(rsName)) {
            val tmpTable = s"${CSCommonUtils.CS_TMP_TABLE_PREFIX}${nodeNameStr}_rs${i}"
            i = i + 1
            val contextKey = new CommonContextKey
            contextKey.setContextScope(ContextScope.FRIENDLY)
            contextKey.setContextType(ContextType.METADATA)
            contextKey.setKey(CSCommonUtils.getTableKey(nodeNameStr, tmpTable))
            val table = CSTableService
              .getInstance()
              .getCSTable(contextIDValueStr, SerializeHelper.serializeContextKey(contextKey))
            if (null == table) {
              rsName = tmpTable
            }
          }
          rsName
        }
      val csTable = new CSTable
      csTable.setName(tableName)
      csTable.setAlias(alias)
      csTable.setAvailable(true)
      csTable.setComment("cs temp table")
      csTable.setCreateTime(new Date())
      csTable.setCreator(StorageUtils.getJvmUser)
      csTable.setExternalUse(true)
      csTable.setImport(false)
      csTable.setLocation(writer.toString)
      csTable.setPartitionTable(false)
      csTable.setView(true)
      val csColumns = columns.map { column =>
        val csColumn = new CSColumn
        csColumn.setName(column.columnName)
        csColumn.setType(column.dataType.getTypeName)
        csColumn.setComment(column.comment)
        csColumn
      }
      csTable.setColumns(csColumns)
      val contextKey = new CommonContextKey
      contextKey.setContextScope(ContextScope.PUBLIC)
      contextKey.setContextType(ContextType.METADATA)
      contextKey.setKey(CSCommonUtils.getTableKey(nodeNameStr, tableName))
      CSTableService
        .getInstance()
        .putCSTable(contextIDValueStr, SerializeHelper.serializeContextKey(contextKey), csTable)
      logger.info(s"Finished to register TempTable nodeName:$nodeNameStr")
    }
  }

}
