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

import java.security.PrivilegedExceptionAction
import java.util.concurrent.atomic.AtomicBoolean
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engine.execute.{ EngineExecutor, EngineExecutorContext, SQLCodeParser }
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.resourcemanager.{ LoadInstanceResource, Resource }
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.domain.{ Column, DataType }
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{ TableMetaData, TableRecord }
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.hadoop.security.UserGroupInformation
import org.slf4j.LoggerFactory
import com.webank.wedatasphere.linkis.engine.impala.exception.ImpalaQueryFailedException
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException
import com.webank.wedatasphere.linkis.engine.impala.client.exception.TransportException
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory.Protocol
import com.webank.wedatasphere.linkis.engine.impala.client.factory.ImpalaClientFactory.Transport
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecProgress
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecStatus
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaClient
import com.webank.wedatasphere.linkis.engine.impala.client.thrift.ImpalaThriftClientOnHiveServer2
import java.util.concurrent.LinkedBlockingQueue

/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 * 
 */
class ImpalaEngineExecutor(outputPrintLimit: Int, impalaClient: ImpalaClient, ugi: UserGroupInformation) extends EngineExecutor(outputPrintLimit, isSupportParallelism = false) with SingleTaskOperateSupport with SingleTaskInfoSupport {

  private val LOG = LoggerFactory.getLogger(getClass)

  private val nameSuffix: String = "_ImpalaEngineExecutor"

  private val name: String = Sender.getThisServiceInstance.getInstance

  private var totalProgress: Float = 0.0f

  private var singleLineProgress: Float = 0.0f

  private var stage: Int = 0

  private var engineExecutorContext: EngineExecutorContext = _

  private var singleCodeCompleted: AtomicBoolean = new AtomicBoolean(false)

  private var singleSqlProgressList: LinkedBlockingQueue[ImpalaResultListener] = new LinkedBlockingQueue[ImpalaResultListener]()

  override def getName: String = name

  override def init(): Unit = {
    transition(ExecutorState.Idle)
    LOG.info(s"Ready to change engine state!")
    setCodeParser(new SQLCodeParser)
    super.init()
  }

  override def getActualUsedResources: Resource = {
    new LoadInstanceResource(Runtime.getRuntime.totalMemory() - Runtime.getRuntime.freeMemory(), 2, 1)
  }

  override def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = {
    //Clear the data of singleSqlMap(清空singleSqlMap的数据)
    singleSqlProgressList.clear()
    singleCodeCompleted.set(false)
    var realCode = code.trim()
    //拆行执行
    while (realCode.startsWith("\n")) realCode = StringUtils.substringAfter(realCode, "\n")
    LOG.info(s"impala client begins to run hql code:\n ${realCode.trim}")
    if (realCode.trim.length > 500) {
      engineExecutorContext.appendStdout(s"$getName >> ${realCode.trim.substring(0, 500)} ...")
    } else engineExecutorContext.appendStdout(s"$getName >> ${realCode.trim}")
    val tokens = realCode.trim.split("""\s+""")
    this.engineExecutorContext = engineExecutorContext
    LOG.debug("ugi is " + ugi.getUserName)
    var hasResult: Boolean = false
    var rows: Int = 0
    var columnCount: Int = 0
    val startTime = System.currentTimeMillis()
    try {
      var impalaResultListener: ImpalaResultListener = new ImpalaResultListener()
      impalaResultListener.setEngineExecutorContext(engineExecutorContext)
      if (null == impalaClient) {
        LOG.error("null  impalaClient!!");
      }
      LOG.info(s"impala client begin submit job.")
      singleSqlProgressList.add(impalaResultListener)
      impalaClient.execute(realCode, impalaResultListener);
    } catch {
      case t: Throwable =>
        clearCurrentProgress()
        singleCodeCompleted.set(true)
        singleSqlProgressList.clear()
        LOG.error(s"query failed, reason:", t)
        return ErrorExecuteResponse(t.getMessage, t)
    }
    LOG.info(s"impala client finish job success")
    clearCurrentProgress()
    singleCodeCompleted.set(true)
    singleSqlProgressList.clear()
    SuccessExecuteResponse()
  }

  private def clearCurrentProgress(): Unit = {
    singleLineProgress = 0.0f
  }

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = {
    val completeCode = code + completedLine
    executeLine(engineExecutorContext, completeCode)
  }

  override def close(): Unit = {
    singleSqlProgressList.clear()
    Utils.tryAndWarnMsg(impalaClient.close())("close session failed")

  }

  override def progress(): Float = {
    if (engineExecutorContext != null) {
      val totalSQLs = engineExecutorContext.getTotalParagraph
      val currentSQL = engineExecutorContext.getCurrentParagraph
      val currentBegin = (currentSQL - 1) / totalSQLs.asInstanceOf[Float]
      var totalProgress: Float = 0.0F
      var nowProgress: Float = 0.0F
      import scala.collection.JavaConversions._
      singleSqlProgressList.foreach(progress => {
        LOG.info(s"impala totalProgress is $totalProgress totalSQLs: $totalSQLs, currentSQL: $currentSQL,currentBegin: $currentBegin _name: " + progress.getJobID() + " progress" + progress.getSqlProgress())
        totalProgress += progress.getSqlProgress()
      })
      try {
        nowProgress = (totalProgress / totalSQLs).asInstanceOf[Float]
      } catch {
        case e: Exception => nowProgress = 0.0f
        case _            => nowProgress = 0.0f
      }
      LOG.info(s"impala progress is $nowProgress")
      if (nowProgress.isNaN) return 0.0f
      (nowProgress + currentBegin)
    } else 0.0f
  }

  override def getProgressInfo: Array[JobProgressInfo] = {
    val arrayBuffer: ArrayBuffer[JobProgressInfo] = new ArrayBuffer[JobProgressInfo]()
    if (singleSqlProgressList.isEmpty) return arrayBuffer.toArray
    import scala.collection.JavaConversions._
    for (progress <- singleSqlProgressList) {
      arrayBuffer += JobProgressInfo(progress.getJobID(), progress.getTotalScanRanges(), 0, 0, progress.getCompletedScanRanges())
    }
    arrayBuffer.toArray
  }

  override def log(): String = ""

  override def kill(): Boolean = {
    LOG.info("impala begins to kill job")
    //逐个取消任务
    for (progress <- singleSqlProgressList) {
      Utils.tryCatch(impalaClient.cancel(progress.getJobID())) {
        case e: Exception => logger.error("c;pse")
      }
    }
    clearCurrentProgress()
    singleSqlProgressList.clear()
    LOG.info("impala killed job successfully")
    true

  }

  override def pause(): Boolean = {
    true
  }

  override def resume(): Boolean = {
    true
  }
}
