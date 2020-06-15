/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.cs

import java.util.Date

import com.webank.wedatasphere.linkis.common.io.resultset.ResultSetWriter
import com.webank.wedatasphere.linkis.common.io.{MetaData, Record}
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.cs.client.service.CSTableService
import com.webank.wedatasphere.linkis.cs.client.utils.{ContextServiceUtils, SerializeHelper}
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.{ContextScope, ContextType}
import com.webank.wedatasphere.linkis.cs.common.entity.metadata.{CSColumn, CSTable}
import com.webank.wedatasphere.linkis.cs.common.entity.source.CommonContextKey
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.storage.domain.Column
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.lang.StringUtils

/**
  * @author peacewong
  * @date 2020/3/13 16:29
  */
object CSTableRegister extends Logging{

  def registerTempTable(engineExecutorContext: EngineExecutorContext,
                        writer: ResultSetWriter[_ <: MetaData, _ <: Record], alias: String, columns: Array[Column]): Unit = {

    val contextIDValueStr = ContextServiceUtils.getContextIDStrByMap(engineExecutorContext.getProperties)
    val nodeNameStr = ContextServiceUtils.getNodeNameStrByMap(engineExecutorContext.getProperties)

    if (StringUtils.isNotBlank(contextIDValueStr) && StringUtils.isNotBlank(nodeNameStr)) {
      info(s"Start to register TempTable nodeName:$nodeNameStr")
      writer.flush()
      val tableName = if (StringUtils.isNotBlank(alias)) s"${CSCommonUtils.CS_TMP_TABLE_PREFIX}${nodeNameStr}_${alias}" else {
        var i = 1;
        var rsName: String = null;
        while (StringUtils.isEmpty(rsName)) {
          val tmpTable = s"${CSCommonUtils.CS_TMP_TABLE_PREFIX}${nodeNameStr}_rs${i}"
          i = i + 1
          val contextKey = new CommonContextKey
          contextKey.setContextScope(ContextScope.FRIENDLY)
          contextKey.setContextType(ContextType.METADATA)
          contextKey.setKey(CSCommonUtils.getTableKey(nodeNameStr, tmpTable))
          val table = CSTableService.getInstance().getCSTable(contextIDValueStr, SerializeHelper.serializeContextKey(contextKey))
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
        csColumn.setType(column.dataType.typeName)
        csColumn.setComment(column.comment)
        csColumn
      }
      csTable.setColumns(csColumns)
      val contextKey = new CommonContextKey
      contextKey.setContextScope(ContextScope.PUBLIC)
      contextKey.setContextType(ContextType.METADATA)
      contextKey.setKey(CSCommonUtils.getTableKey(nodeNameStr, tableName))
      CSTableService.getInstance().putCSTable(contextIDValueStr, SerializeHelper.serializeContextKey(contextKey), csTable)
      info(s"Finished to register TempTable nodeName:$nodeNameStr")
    }
  }
}
