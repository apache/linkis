package com.webank.wedatasphere.linkis.engine.impala.executor

import java.util
import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener
import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException
import com.webank.wedatasphere.linkis.engine.impala.client.exception.TransportException
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory.Protocol
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory.Transport
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecProgress
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecStatus
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaClient
import com.webank.wedatasphere.linkis.engine.impala.client.thrift.ImpalaThriftClientOnHiveServer2
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaResultSet
import org.slf4j.LoggerFactory
import com.webank.wedatasphere.linkis.engine.impala.exception.ImpalaQueryFailedException
import com.webank.wedatasphere.linkis.engine.impala.common.ImpalaUtils
import com.webank.wedatasphere.linkis.storage.domain.{ Column, DataType }
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{ TableMetaData, TableRecord }
import com.webank.wedatasphere.linkis.engine.execute.{ EngineExecutor, EngineExecutorContext, SQLCodeParser }
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 * 
 */
class ImpalaResultListener extends ResultListener {

  private val LOG = LoggerFactory.getLogger(getClass)
  
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
    LOG.info(s"Time taken: ${ImpalaUtils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results.")
    if (null == columnsString) {
      throw ImpalaQueryFailedException(41005, "cannot get the schemas of column.")
    }
    import scala.collection.JavaConverters._
    val columns = columnsString.asScala.map(fieldSchema => Column(justFieldName(fieldSchema), null, null)).toArray[Column]
    val metaData = new TableMetaData(columns)
    LOG.debug("Scala field Schemas are " + columns.mkString(" "))
    val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    resultSetWriter.addMetaData(metaData)
    val result = new util.ArrayList[String]()
    var rows:Int = 0
    var columnCount:Int = 0
    while(resultSet.next()){
        val arr:Array[Object] = resultSet.getValues
        val arrAny:ArrayBuffer[Any] = new ArrayBuffer[Any]()
        if (arr.length != 0) arr foreach arrAny.add else for(i <-1 to columns.length) arrAny add ""
        LOG.debug("TableRecord are " + arrAny.toArray.mkString(" "))
        resultSetWriter.addRecord(new TableRecord(arrAny.toArray))
        rows += result.size
        result.clear()
        rows += result.size
        result.clear()
    }
    columnCount = if (columns != null) columns.length else 0
    engineExecutorContext.sendResultSet(resultSetWriter)
    IOUtils.closeQuietly(resultSetWriter)
    engineExecutorContext.appendStdout(s"Fetched  $columnCount col(s) : $rows row(s) in hive")
    LOG.info(s"Fetched  $columnCount col(s) : $rows row(s) in impala")
  }

  private def justFieldName(schemaName: String): String = {
    val arr = schemaName.split("\\.")
    if (arr.length == 2) arr(1) else schemaName
  }

  @Override def error(status: ExecStatus) {
    LOG.error("impala query failed, reason: ", status.getErrorMessage())
    val errorException = ImpalaQueryFailedException(41004, "impala query failed:" + status.getErrorMessage())
    throw errorException
  }

  @Override def progress(progress: ExecProgress) {
    totalScanRanges = progress.getTotalScanRanges().asInstanceOf[Int];
    completedScanRanges = progress.getCompletedScanRanges().asInstanceOf[Int];
    sqlProgress = (progress.getCompletedScanRanges().asInstanceOf[Float] / progress.getTotalScanRanges().asInstanceOf[Float]);
  }

   
  @Override def message(message: java.util.List[String] ) {
    message.foreach(e => LOG.info(e))
  }
}