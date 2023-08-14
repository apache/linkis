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

package org.apache.linkis.engineplugin.impala.executor

import org.apache.commons.collections.MapUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{OverloadUtils, Utils}
import org.apache.linkis.engineconn.common.password.{CommandPasswordCallback, StaticPasswordCallback}
import org.apache.linkis.engineconn.computation.executor.execute.{ConcurrentComputationExecutor, EngineExecutionContext}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineplugin.impala.client.ImpalaResultSet.Row
import org.apache.linkis.engineplugin.impala.client.exception.{ImpalaEngineException, ImpalaErrorCodeSummary}
import org.apache.linkis.engineplugin.impala.client.protocol.{ExecProgress, ExecStatus}
import org.apache.linkis.engineplugin.impala.client.thrift.{ImpalaThriftClient, ImpalaThriftSessionFactory}
import org.apache.linkis.engineplugin.impala.client.{ExecutionListener, ImpalaClient, ImpalaResultSet}
import org.apache.linkis.engineplugin.impala.conf.ImpalaConfiguration._
import org.apache.linkis.engineplugin.impala.conf.ImpalaEngineConfig
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.linkis.storage.domain.Column
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.springframework.util.CollectionUtils

import java.io.FileInputStream
import java.security.KeyStore
import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import javax.net.SocketFactory
import javax.net.ssl._
import javax.security.auth.callback.{Callback, CallbackHandler, NameCallback, PasswordCallback}
import scala.collection.JavaConverters._

class ImpalaEngineConnExecutor(override val outputPrintLimit: Int, val id: Int)
    extends ConcurrentComputationExecutor(outputPrintLimit) {

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  private val impalaClients: util.Map[String, ImpalaClient] =
    new ConcurrentHashMap[String, ImpalaClient]()

  private val taskExecutions: util.Map[String, TaskExecution] =
    new ConcurrentHashMap[String, TaskExecution]()

  override def init: Unit = {
    setCodeParser(new SQLCodeParser)
    super.init
  }

  override def executeLine(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {
    val realCode = if (StringUtils.isBlank(code)) {
      "SELECT 1"
    } else {
      code.trim
    }

    logger.info(s"impala client begins to run code:\n $realCode")
    val taskId = engineExecutionContext.getJobId.get

    val impalaClient = getOrCreateImpalaClient(engineExecutionContext)
    val taskExecution: TaskExecution = TaskExecution(taskId, engineExecutionContext, impalaClient)
    Utils.tryFinally {
      taskExecutions.put(taskId, taskExecution)
      Utils.tryCatch {
        impalaClient.execute(realCode, taskExecution)
        taskExecution.response
      } { exception =>
        engineExecutionContext.appendStdout(
          LogUtils.generateERROR(ExceptionUtils.getStackTrace(exception))
        )
        ErrorExecuteResponse(ExceptionUtils.getMessage(exception), exception)
      }
    } {
      taskExecutions.remove(taskId)
    }
  }

  override def progress(taskId: String): Float = {
    val taskExecution = taskExecutions.get(taskId)
    if (taskExecution != null) {
      taskExecution.progressFloat
    } else {
      0
    }
  }

  override def getProgressInfo(taskId: String): Array[JobProgressInfo] = {
    val taskExecution = taskExecutions.get(taskId)
    if (taskExecution != null) {
      return taskExecution.progressArray
    }
    Array.empty[JobProgressInfo]
  }

  override def killTask(taskId: String): Unit = {
    val taskExecution = taskExecutions.get(taskId)
    if (taskExecution != null) {
      taskExecution.kill()
    }
    super.killTask(taskId)
  }

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (!CollectionUtils.isEmpty(labels)) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

  override def supportCallBackLogs(): Boolean = false

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = {
    null
  }

  override def getCurrentNodeResource(): NodeResource = {
    NodeResourceUtils.appendMemoryUnitIfMissing(
      EngineConnObject.getEngineCreationContext.getOptions
    )

    val resource = new CommonNodeResource
    val usedResource = new LoadResource(OverloadUtils.getProcessMaxMemory, 1)
    resource.setUsedResource(usedResource)
    resource
  }

  override def getId(): String = Sender.getThisServiceInstance.getInstance + s"_$id"

  override def getConcurrentLimit: Int = ENGINE_CONCURRENT_LIMIT.getValue

  private def queryOutput(
      taskId: String,
      engineExecutorContext: EngineExecutionContext,
      resultSet: ImpalaResultSet
  ): Unit = {
    var columnCount = 0
    var rows = 0
    val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    Utils.tryCatch {
      val columns = resultSet.getColumns.asScala
        .map(column => new Column(column.getName, null, null))
        .toArray[Column]
      columnCount = columns.length
      resultSetWriter.addMetaData(new TableMetaData(columns))

      var row: Row = resultSet.next()
      while (row != null) {
        val record = new TableRecord(row.getValues)
        resultSetWriter.addRecord(record)
        rows += 1
        row = resultSet.next()
      }
    } { case e: Exception =>
      IOUtils.closeQuietly(resultSetWriter)
      throw e
    }
    logger.info(s"Fetched $columnCount col(s) : $rows row(s) in impala")
    engineExecutorContext.appendStdout(
      LogUtils.generateInfo(s"Fetched $columnCount col(s) : $rows row(s) in impala")
    );
    engineExecutorContext.sendResultSet(resultSetWriter)
  }

  override def killAll(): Unit = {
    val iterator = taskExecutions.values().iterator()
    while (iterator.hasNext) {
      Utils.tryAndWarn(iterator.next().kill())
    }
    taskExecutions.clear()
  }

  override def close(): Unit = {
    killAll()
    val iterator = impalaClients.values().iterator()
    while (iterator.hasNext) {
      Utils.tryAndWarn(iterator.next().close())
    }
    impalaClients.clear()
    super.close()
  }

  case class TaskExecution(
      taskId: String,
      engineExecutionContext: EngineExecutionContext,
      impalaClient: ImpalaClient
  ) extends ExecutionListener {
    var queryId: String = ""
    var totalTasks: Int = -1
    var runningTasks: Int = -1
    var failedTasks: Int = 0
    var succeedTasks: Int = 0
    var response: CompletedExecuteResponse = SuccessExecuteResponse()

    override def error(status: ExecStatus): Unit = {
      response = ErrorExecuteResponse(
        status.getErrorMessage,
        ImpalaEngineException.of(ImpalaErrorCodeSummary.ExecutionError, status.getName)
      )
    }

    override def success(resultSet: ImpalaResultSet): Unit = {
      queryOutput(taskId, engineExecutionContext, resultSet)
    }

    override def created(queryId: String): Unit = {
      this.queryId = queryId
    }

    override def progress(progress: ExecProgress): Unit = {
      runningTasks = progress.getRunningNodes
      totalTasks = progress.getTotalScanRanges.toInt
      succeedTasks = progress.getCompletedScanRanges.toInt

      engineExecutionContext.pushProgress(progressFloat, progressArray)
    }

    override def message(messages: util.List[String]): Unit = {
      messages.forEach(new Consumer[String]() {
        override def accept(message: String): Unit = engineExecutionContext.appendStdout(message)
      })
    }

    def progressArray: Array[JobProgressInfo] = {
      Array(JobProgressInfo(queryId, totalTasks, runningTasks, failedTasks, succeedTasks))
    }

    def progressFloat: Float = {
      if (totalTasks > 0) {
        succeedTasks.toFloat / totalTasks
      } else {
        0
      }
    }

    def kill(): Unit = {
      if (StringUtils.isNotBlank(queryId)) {
        impalaClient.cancel(queryId)
      }
    }

  }

  private def getOrCreateImpalaClient(
      engineExecutionContext: EngineExecutionContext
  ): ImpalaClient = {
    val userCreatorLabel =
      engineExecutionContext.getLabels.find(_.isInstanceOf[UserCreatorLabel]).get
    val engineTypeLabel =
      engineExecutionContext.getLabels.find(_.isInstanceOf[EngineTypeLabel]).get
    var configMap: util.Map[String, String] = null
    if (userCreatorLabel != null && engineTypeLabel != null) {
      configMap = Utils.tryAndWarn(
        ImpalaEngineConfig.getCacheMap(
          (
            userCreatorLabel.asInstanceOf[UserCreatorLabel],
            engineTypeLabel.asInstanceOf[EngineTypeLabel]
          )
        )
      )
    }
    if (configMap == null) {
      configMap = new util.HashMap[String, String]()
    }

    val properties = engineExecutionContext.getProperties.asInstanceOf[util.Map[String, String]]
    if (MapUtils.isNotEmpty(properties)) {
      configMap.putAll(properties)
    }

    val impalaServers = IMPALA_SERVERS.getValue(configMap)
    val impalaMaxConnections = IMPALA_MAX_CONNECTIONS.getValue(configMap)
    val impalaSaslEnable = IMPALA_SASL_ENABLE.getValue(configMap)
    val impalaSaslProperties = IMPALA_SASL_PROPERTIES.getValue(configMap)
    val impalaSaslUsername = IMPALA_SASL_USERNAME.getValue(configMap)
    val impalaSaslPassword = IMPALA_SASL_PASSWORD.getValue(configMap)
    val impalaSaslPasswordCmd = IMPALA_SASL_PASSWORD_CMD.getValue(configMap)
    val impalaSaslMechanism = IMPALA_SASL_MECHANISM.getValue(configMap)
    val impalaSaslAuthorization = IMPALA_SASL_AUTHORIZATION_ID.getValue(configMap)
    val impalaSaslProtocol = IMPALA_SASL_PROTOCOL.getValue(configMap)
    val impalaHeartbeatSeconds = IMPALA_HEARTBEAT_SECONDS.getValue(configMap)
    val impalaQueryTimeoutSeconds = IMPALA_QUERY_TIMEOUT_SECONDS.getValue(configMap)
    val impalaQueryBatchSize = IMPALA_QUERY_BATCH_SIZE.getValue(configMap)
    val impalaQueryOptions = IMPALA_QUERY_OPTIONS.getValue(configMap)

    val impalaSslEnable = IMPALA_SSL_ENABLE.getValue(configMap)
    val impalaSslKeystore = IMPALA_SSL_KEYSTORE.getValue(configMap)
    val impalaSslKeystoreType = IMPALA_SSL_KEYSTORE_TYPE.getValue(configMap)
    val impalaSslKeystorePassword = IMPALA_SSL_KEYSTORE_PASSWORD.getValue(configMap)
    val impalaSslTruststore = IMPALA_SSL_TRUSTSTORE.getValue(configMap)
    val impalaSslTruststoreType = IMPALA_SSL_TRUSTSTORE_TYPE.getValue(configMap)
    val impalaSslTruststorePassword = IMPALA_SSL_TRUSTSTORE_PASSWORD.getValue(configMap)

    val impalaClientKey = Array(
      impalaServers,
      impalaMaxConnections,
      impalaSaslEnable,
      impalaSaslProperties,
      impalaSaslUsername,
      impalaSaslPassword,
      impalaSaslPasswordCmd,
      impalaSaslMechanism,
      impalaSaslAuthorization,
      impalaSaslProtocol,
      impalaHeartbeatSeconds,
      impalaQueryTimeoutSeconds,
      impalaQueryBatchSize,
      impalaQueryOptions,
      impalaSslEnable,
      impalaSslKeystore,
      impalaSslKeystoreType,
      impalaSslKeystorePassword,
      impalaSslTruststore,
      impalaSslTruststoreType,
      impalaSslTruststorePassword
    ).mkString("/")

    impalaClients.synchronized {
      var client = impalaClients.get(impalaClientKey)
      if (client == null) {
        val socketFactory = createSocketFactory(
          impalaSslEnable,
          impalaSslKeystore,
          impalaSslKeystoreType,
          impalaSslKeystorePassword,
          impalaSslTruststore,
          impalaSslTruststoreType,
          impalaSslTruststorePassword
        )

        val servers = impalaServers.split(',')
        val maxConnections = impalaMaxConnections
        val factory: ImpalaThriftSessionFactory = if (impalaSaslEnable) {
          val saslProperties: util.Map[String, String] = new util.TreeMap()
          Option(impalaSaslProperties)
            .map(_.split(','))
            .getOrElse(Array[String]())
            .foreach { str =>
              val kv = StringUtils.split(str, "=", 2)
              saslProperties.put(kv(0), if (kv.length > 1) kv(1) else "")
            }

          val password = impalaSaslPassword
          val passwordCmd = impalaSaslPasswordCmd
          var passwordCallback: PasswordCallback = null
          if (StringUtils.isNotBlank(passwordCmd)) {
            passwordCallback = new CommandPasswordCallback(passwordCmd);
          } else if (StringUtils.isNotBlank(password)) {
            passwordCallback = new StaticPasswordCallback(password);
          }

          val callbackHandler: CallbackHandler = new CallbackHandler() {
            override def handle(callbacks: Array[Callback]): Unit = callbacks.foreach {
              case callback: NameCallback => callback.setName(impalaSaslUsername)
              case callback: PasswordCallback => callback.setPassword(passwordCallback.getPassword)
            }
          }

          new ImpalaThriftSessionFactory(
            servers,
            maxConnections,
            socketFactory,
            impalaSaslMechanism,
            impalaSaslAuthorization,
            impalaSaslProtocol,
            saslProperties,
            callbackHandler
          )
        } else {
          new ImpalaThriftSessionFactory(servers, maxConnections, socketFactory)
        }

        val impalaClient = new ImpalaThriftClient(factory, impalaHeartbeatSeconds)
        impalaClient.setQueryTimeoutInSeconds(impalaQueryTimeoutSeconds)
        impalaClient.setBatchSize(impalaQueryBatchSize)
        Option(impalaQueryOptions)
          .map(_.split(','))
          .getOrElse(Array[String]())
          .foreach { str =>
            if (StringUtils.contains(str, "=")) {
              val kv = StringUtils.split(str, "=", 2)
              impalaClient.setQueryOption(kv(0), if (kv.length > 1) kv(1) else "")
            }
          }

        client = impalaClient
      }
      client
    }
  }

  private def createSocketFactory(
      impalaSslEnable: Boolean,
      impalaSslKeystore: String,
      impalaSslKeystoreType: String,
      impalaSslKeystorePassword: String,
      impalaSslTruststore: String,
      impalaSslTruststoreType: String,
      impalaSslTruststorePassword: String
  ): SocketFactory = {

    if (impalaSslEnable) {
      val keyStore: KeyStore = KeyStore.getInstance(impalaSslKeystoreType)
      val keyStorePassword: Array[Char] = Option(impalaSslKeystorePassword)
        .map(_.toCharArray)
        .orNull

      val in = new FileInputStream(impalaSslKeystore)
      try {
        keyStore.load(in, keyStorePassword)
      } finally {
        if (in != null) in.close()
      }
      val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm);
      keyManagerFactory.init(keyStore, keyStorePassword);
      val keyManagers: Array[KeyManager] = keyManagerFactory.getKeyManagers;

      var trustStore = keyStore
      if (StringUtils.isNotBlank(impalaSslTruststore)) {
        trustStore = KeyStore.getInstance(impalaSslTruststoreType)
        val trustStorePassword: Array[Char] = Option(impalaSslTruststorePassword)
          .map(_.toCharArray)
          .orNull
        val in = new FileInputStream(impalaSslTruststore)
        try {
          trustStore.load(in, trustStorePassword)
        } finally {
          if (in != null) in.close()
        }
      }

      // create TrustManagerFactory
      val trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
      trustManagerFactory.init(trustStore)

      // get X509TrustManager
      val trustManagers = trustManagerFactory.getTrustManagers
      if (trustManagers.length != 1 || !trustManagers(0).isInstanceOf[X509TrustManager]) {
        throw new RuntimeException(
          "Unexpected default trust managers:" + StringUtils.join(",", trustManagers)
        )
      }
      val trustManager = trustManagers(0).asInstanceOf[X509TrustManager]

      // create SSLContext
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(keyManagers, Array[TrustManager](trustManager), null)

      sslContext.getSocketFactory
    } else {
      null
    }
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = null

}
