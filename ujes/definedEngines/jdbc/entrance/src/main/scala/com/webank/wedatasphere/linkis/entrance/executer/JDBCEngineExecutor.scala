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
package com.webank.wedatasphere.linkis.entrance.executer

import java.sql.{Connection, SQLFeatureNotSupportedException, Statement}
import java.util

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.conf.JDBCConfiguration.ENGINE_RESULT_SET_MAX_CACHE
import com.webank.wedatasphere.linkis.entrance.conf.executer.ConnectionManager
import com.webank.wedatasphere.linkis.entrance.exception.{JDBCSQLFeatureNotSupportedException, JDBCStateMentNotInitialException}
import com.webank.wedatasphere.linkis.entrance.execute.{EngineExecuteAsynReturn, EntranceEngine, EntranceJob, JDBCJobExecuteRequest, StorePathExecuteRequest}
import com.webank.wedatasphere.linkis.entrance.persistence.EntranceResultSetEngine
import com.webank.wedatasphere.linkis.protocol.engine.{JobProgressInfo, RequestTask}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.domain.{Column, DataType}
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class JDBCEngineExecutor(outputPrintLimit: Int, properties: util.HashMap[String, String])
  extends EntranceEngine(id = 0) with SingleTaskOperateSupport with SingleTaskInfoSupport {

  private val LOG = LoggerFactory.getLogger(getClass)
  private val connectionManager = ConnectionManager.getInstance()
  private var statement: Statement = null
  private var connection:Connection = null
  private val name: String = Sender.getThisServiceInstance.getInstance
  private val persistEngine = new EntranceResultSetEngine()
  //execute line number，as alias and progress line
  private var codeLine = 0
  //total line number
  private var totalCodeLineNumber = 0
  private var executeUser: String = _

  protected def executeLine(code: String, storePath: String, alias: String): ExecuteResponse = {
    val realCode = code.trim()
    LOG.info(s"jdbc client begins to run jdbc code:\n ${realCode.trim}")
    connection = connectionManager.getConnection(properties)
    statement = connection.createStatement()
    LOG.info(s"create statement is:  $statement")
    val isResultSetAvailable = statement.execute(code)
    LOG.info(s"Is ResultSet available ? : $isResultSetAvailable")
    if(isResultSetAvailable){
      LOG.info("ResultSet is available")
      val JDBCResultSet = statement.getResultSet
      if(isDDLCommand(statement.getUpdateCount(),JDBCResultSet.getMetaData().getColumnCount)){
        LOG.info(s"current result is a ResultSet Object , but there are no more results :${code} ")
        Utils.tryQuietly {
          JDBCResultSet.close()
          statement.close()
          connection.close()
        }
        return SuccessExecuteResponse()
      }else{
        val md = JDBCResultSet.getMetaData
        val metaArrayBuffer = new ArrayBuffer[Tuple2[String, String]]()
        for (i <- 1 to md.getColumnCount) {
          metaArrayBuffer.add(Tuple2(md.getColumnName(i), JDBCHelper.getTypeStr(md.getColumnType(i))))
        }
        val columns = metaArrayBuffer.map { c => Column(c._1, DataType.toDataType(c._2), "") }.toArray[Column]
        val metaData = new TableMetaData(columns)
        val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TABLE_TYPE)
        val resultSetPath = resultSet.getResultSetPath(new FsPath(storePath), alias)
        val resultSetWriter = ResultSetWriter.getResultSetWriter(resultSet, ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, executeUser)
        resultSetWriter.addMetaData(metaData)
        var count = 0
        Utils.tryCatch({
          while (count < outputPrintLimit && JDBCResultSet.next()) {
            val r: Array[Any] = columns.indices.map { i =>
              val data = JDBCResultSet.getObject(i + 1) match {
                case value: Any => value.toString
                case _ => null
              }
              data
            }.toArray
            resultSetWriter.addRecord(new TableRecord(r))
            count += 1
          }
        }) {
          case e: Exception => return ErrorExecuteResponse("query jdbc failed", e)
        }
        val output = if (resultSetWriter != null) resultSetWriter.toString else null
        Utils.tryQuietly {
          JDBCResultSet.close()
          statement.close()
          connection.close()
          IOUtils.closeQuietly(resultSetWriter)
        }
        LOG.info("sql execute completed")
        AliasOutputExecuteResponse(alias, output)
      }
    }else{
      LOG.info(s"only return affect rows : ${statement.getUpdateCount}")
      Utils.tryQuietly{
        statement.close()
        connection.close()
      }
      return SuccessExecuteResponse()
    }
  }


  override protected def callExecute(request: RequestTask): EngineExecuteAsynReturn = null

  override def progress(): Float = {
    if (totalCodeLineNumber != 0){
      return (codeLine/totalCodeLineNumber.asInstanceOf[Float])
    }else{
      return 0.0f
    }
  }

  override def getProgressInfo: Array[JobProgressInfo] = Array.empty[JobProgressInfo]

  def getName: String = name


  override def kill(): Boolean = {
    if (statement != null) {
      try {
        statement.cancel()
      } catch {
        case e: SQLFeatureNotSupportedException =>
          throw new JDBCSQLFeatureNotSupportedException("unsupport sql feature ")
        case _ =>
          throw new JDBCStateMentNotInitialException("jdbc statement not initial")
      }
    }
    true
  }

  override def pause(): Boolean = ???

  override def resume(): Boolean = ???

  override def log(): String = "JDBC Engine is running"

  override protected def callback(): Unit = {}

  override def close(): Unit = {
  }

  protected def isDDLCommand(updatedCount: Int, columnCount: Int): Boolean = {
    if (updatedCount<0 && columnCount<=0){
      return true
    }else{
      return false
    }
  }
  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    if (StringUtils.isEmpty(executeRequest.code)) {
      return IncompleteExecuteResponse("execute codes can not be empty)")
    }

    val storePath = executeRequest match {
      case storePathExecuteRequest: StorePathExecuteRequest => storePathExecuteRequest.storePath
      case _ => ""
    }

    this.executeUser =  executeRequest match {
      case jobExecuteRequest: JDBCJobExecuteRequest => jobExecuteRequest.job.asInstanceOf[EntranceJob].getUser
      case _ => StorageUtils.getJvmUser
    }

    val codes = JDBCSQLCodeParser.parse(executeRequest.code)

    if (!codes.isEmpty) {
      totalCodeLineNumber = codes.length
      codeLine = 0
      codes.foreach { code =>
        try {
          val executeRes = executeLine(code, storePath, s"_$codeLine")
          executeRes match {
            case aliasOutputExecuteResponse: AliasOutputExecuteResponse =>
              persistEngine.persistResultSet(executeRequest.asInstanceOf[JDBCJobExecuteRequest].job, aliasOutputExecuteResponse)
            case SuccessExecuteResponse() =>
              LOG.info(s"sql execute successfully : ${code}")
            case IncompleteExecuteResponse(_) =>
              LOG.error(s"sql execute failed : ${code}")
            case _ =>
              LOG.warn("no matching exception")
          }
          codeLine = codeLine + 1
        } catch {
          case e: Exception =>
            totalCodeLineNumber=0
            LOG.error("JDBC exception", e)
            return ErrorExecuteResponse("JDBC exception", e)
          case t: Throwable =>
            totalCodeLineNumber=0
            logger.error("JDBC query failed", t)
            return ErrorExecuteResponse("JDBC query failed", t)
        }finally {
          Utils.tryQuietly{
            statement.close()
            connection.close()
          }
        }
      }
    }
    totalCodeLineNumber=0
    SuccessExecuteResponse()
  }
}

