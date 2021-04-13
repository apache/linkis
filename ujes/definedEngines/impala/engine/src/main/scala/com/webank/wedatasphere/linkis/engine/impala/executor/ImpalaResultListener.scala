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

package com.webank.wedatasphere.linkis.engine.impala.executor

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.{ExecProgress, ExecStatus}
import com.webank.wedatasphere.linkis.engine.impala.client.{ImpalaResultSet, ResultListener}
import com.webank.wedatasphere.linkis.engine.impala.common.ImpalaUtils
import com.webank.wedatasphere.linkis.engine.impala.exception.ImpalaQueryFailedException
import com.webank.wedatasphere.linkis.storage.domain.Column
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class ImpalaResultListener extends ResultListener with Logging{

  private var impalaJobID: String = _

  private var sqlProgress: Float = 0.0f
  
	private var totalScanRanges: Int = 0;
  
	private var completedScanRanges: Int = 0;
								
  private var engineExecutorContext: EngineExecutorContext = _


  def getJobID(): String = this.impalaJobID

  def getSqlProgress(): Float  = this.sqlProgress
  
  def getTotalScanRanges(): Int  = this.totalScanRanges
  
  def getCompletedScanRanges(): Int  = this.completedScanRanges

  @Override def setJobId(jobId : String){
     this.impalaJobID = jobId;
  }

  def setEngineExecutorContext(engineExecutorContext: EngineExecutorContext) {
    this.engineExecutorContext = engineExecutorContext
  }
    
  @Override def success(resultSet: ImpalaResultSet) {
    val startTime = System.currentTimeMillis()
    var columnsString: java.util.List[String] = resultSet.getColumns()
    info(s"Time taken: ${ImpalaUtils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results.")
    if (null == columnsString) {
      throw ImpalaQueryFailedException(41005, "cannot get the schemas of column.")
    }
    import scala.collection.JavaConverters._
    val columns = columnsString.asScala.map(fieldSchema => Column(justFieldName(fieldSchema), null, null)).toArray[Column]
    if (null==columns||columns.size == 0) {
      info(s"Fetched  0 col(s) : 0 row(s) in impala")
      return
    }
    val metaData = new TableMetaData(columns)
    debug("Scala field Schemas are " + columns.mkString(" "))
    val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    resultSetWriter.addMetaData(metaData)
    var rows:Int = 0
    var columnCount:Int = 0
    while(resultSet.next()){
        val arr:Array[Object] = resultSet.getValues
        val arrAny:ArrayBuffer[Any] = new ArrayBuffer[Any]()
        if (arr.length != 0) arr foreach arrAny.add else for(i <-1 to columns.length) arrAny add ""
        resultSetWriter.addRecord(new TableRecord(arrAny.toArray))
        rows += 1
    }
    columnCount = if (columns != null) columns.length else 0
    engineExecutorContext.sendResultSet(resultSetWriter)
    IOUtils.closeQuietly(resultSetWriter)
    engineExecutorContext.appendStdout(s"Fetched  $columnCount col(s) : $rows row(s) in impala")
    info(s"Fetched  $columnCount col(s) : $rows row(s) in impala")
  }

  private def justFieldName(schemaName: String): String = {
    val arr = schemaName.split("\\.")
    if (arr.length == 2) arr(1) else schemaName
  }

  @Override def error(status: ExecStatus) {
    val errorException = ImpalaQueryFailedException(41004, "impala query failed:" + status.getErrorMessage())
    throw errorException
  }

  @Override def progress(progress: ExecProgress) {
    totalScanRanges = progress.getTotalScanRanges().asInstanceOf[Int];
    completedScanRanges = progress.getCompletedScanRanges().asInstanceOf[Int];
    sqlProgress = (progress.getCompletedScanRanges().asInstanceOf[Float] / progress.getTotalScanRanges().asInstanceOf[Float]);
  }

   
  @Override def message(message: java.util.List[String] ) {
    message.foreach(e => info(e))
  }
}