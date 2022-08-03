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
package org.apache.linkis.engineplugin.presto.executer

import java.net.URI
import java.util
import java.util._
import java.util.concurrent.TimeUnit

import com.facebook.presto.client._
import com.facebook.presto.spi.security.SelectedRole
import com.google.common.cache.{Cache, CacheBuilder}
import okhttp3.OkHttpClient
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{OverloadUtils, Utils}
import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.{ConcurrentComputationExecutor, EngineExecutionContext}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineplugin.presto.conf.PrestoConfiguration._
import org.apache.linkis.engineplugin.presto.conf.PrestoEngineConf
import org.apache.linkis.engineplugin.presto.exception.{PrestoClientException, PrestoStateInvalidException}
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.linkis.storage.domain.Column
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.springframework.util.CollectionUtils

import scala.collection.JavaConverters._

class PrestoEngineConnExecutor(override val outputPrintLimit: Int, val id: Int) extends ConcurrentComputationExecutor(outputPrintLimit) {

  private var okHttpClient: OkHttpClient = PrestoEngineConnExecutor.OK_HTTP_CLIENT

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  private val clientSessionCache: Cache[String, ClientSession] = CacheBuilder.newBuilder()
    .expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM).build()

  override def init: Unit = {
    setCodeParser(new SQLCodeParser)
    super.init
  }

  override def execute(engineConnTask: EngineConnTask): ExecuteResponse = {
    val user = getUserCreatorLabel(engineConnTask.getLables).getUser
    val userCreatorLabel = engineConnTask.getLables.find(_.isInstanceOf[UserCreatorLabel]).get
    val engineTypeLabel = engineConnTask.getLables.find(_.isInstanceOf[EngineTypeLabel]).get
    var configMap: util.Map[String, String] = null
    if (userCreatorLabel != null && engineTypeLabel != null) {
      configMap = PrestoEngineConf.getCacheMap((userCreatorLabel.asInstanceOf[UserCreatorLabel], engineTypeLabel.asInstanceOf[EngineTypeLabel]))
    }
    clientSessionCache.put(engineConnTask.getTaskId, getClientSession(user, engineConnTask.getProperties, configMap))
    super.execute(engineConnTask)
  }

  override def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val realCode = code.trim
    logger.info(s"presto client begins to run psql code:\n $realCode")

    val taskId = engineExecutorContext.getJobId.get

    val clientSession = clientSessionCache.getIfPresent(taskId)
    val statement = StatementClientFactory.newStatementClient(okHttpClient, clientSession, realCode)

    initialStatusUpdates(taskId, engineExecutorContext, statement)

    if (statement.isRunning || (statement.isFinished && statement.finalStatusInfo().getError == null)) {
      queryOutput(taskId, engineExecutorContext, statement)
    }

    val errorResponse = verifyServerError(taskId, engineExecutorContext, statement)
    if (errorResponse == null) {
      // update session
      clientSessionCache.put(taskId, updateSession(clientSession, statement))
      SuccessExecuteResponse()
    } else {
      errorResponse
    }

  }

  override def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = null

  // todo
  override def progress(taskID: String): Float = 0.0f

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = Array.empty[JobProgressInfo]

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
    val properties = EngineConnObject.getEngineCreationContext.getOptions
    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory = properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")) {
        properties.put(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key, settingClientMemory + "g")
      }
    }
    val resource = new CommonNodeResource
    val usedResource = new LoadResource(OverloadUtils.getProcessMaxMemory, 1)
    resource.setUsedResource(usedResource)
    resource
  }

  override def getId(): String = Sender.getThisServiceInstance.getInstance + s"_$id"

  override def getConcurrentLimit: Int = ENGINE_CONCURRENT_LIMIT.getValue

  private def getClientSession(user: String, taskParams: util.Map[String, Object], cacheMap: util.Map[String, String]): ClientSession = {
    val configMap = new util.HashMap[String, String]()
    // 运行时指定的参数优先级大于管理台配置优先级
    if (!CollectionUtils.isEmpty(cacheMap)) configMap.putAll(cacheMap)
    taskParams.asScala.foreach {
      case (key: String, value: Object) if value != null => configMap.put(key, String.valueOf(value))
      case _ =>
    }
    val httpUri: URI = URI.create(PRESTO_URL.getValue(configMap))
    val source: String = PRESTO_SOURCE.getValue(configMap)
    val catalog: String = PRESTO_CATALOG.getValue(configMap)
    val schema: String = PRESTO_SCHEMA.getValue(configMap)

    val properties: util.Map[String, String] = configMap.asScala
      .filter(tuple => tuple._1.startsWith("presto.session."))
      .map(tuple => (tuple._1.substring("presto.session.".length), tuple._2))
      .asJava

    val clientInfo: String = "Linkis"
    val transactionId: String = null
    val traceToken: util.Optional[String] = Optional.empty()
    val clientTags: util.Set[String] = Collections.emptySet()
    val timeZonId = TimeZone.getDefault.getID
    val locale: Locale = Locale.getDefault
    val resourceEstimates: util.Map[String, String] = Collections.emptyMap()
    val preparedStatements: util.Map[String, String] = Collections.emptyMap()
    val roles: java.util.Map[String, SelectedRole] = Collections.emptyMap()
    val extraCredentials: util.Map[String, String] = Collections.emptyMap()

    val clientRequestTimeout: io.airlift.units.Duration = new io.airlift.units.Duration(0, TimeUnit.MILLISECONDS)

    new ClientSession(httpUri, user, source, traceToken, clientTags, clientInfo, catalog, schema, timeZonId, locale,
      resourceEstimates, properties, preparedStatements, roles, extraCredentials, transactionId, clientRequestTimeout)
  }

  private def getUserCreatorLabel(labels: Array[Label[_]]): UserCreatorLabel = {
    labels
      .find(l => l.isInstanceOf[UserCreatorLabel])
      .get
      .asInstanceOf[UserCreatorLabel]
  }

  private def initialStatusUpdates(taskId: String, engineExecutorContext: EngineExecutionContext, statement: StatementClient): Unit = {
    while (statement.isRunning
      && (statement.currentData().getData == null || statement.currentStatusInfo().getUpdateType != null)) {
      engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId))
      statement.advance()
    }
  }

  private def queryOutput(taskId: String, engineExecutorContext: EngineExecutionContext, statement: StatementClient): Unit = {
    var columnCount = 0
    var rows = 0
    val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    Utils.tryFinally({
      var results: QueryStatusInfo = null
      if (statement.isRunning) {
        results = statement.currentStatusInfo()
      } else {
        results = statement.finalStatusInfo()
      }
      if (results.getColumns == null) {
        throw new RuntimeException("presto columns is null.")
      }
      val columns = results.getColumns.asScala
        .map(column => Column(column.getName, column.getType, "")).toArray[Column]
      columnCount = columns.length
      resultSetWriter.addMetaData(new TableMetaData(columns))
      while (statement.isRunning) {
        val data = statement.currentData().getData
        if (data != null) for (row <- data.asScala) {
          val rowArray = row.asScala.map(r => String.valueOf(r))
          resultSetWriter.addRecord(new TableRecord(rowArray.toArray))
          rows += 1
        }
        engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId))
        statement.advance()
      }
    })(IOUtils.closeQuietly(resultSetWriter))

    info(s"Fetched $columnCount col(s) : $rows row(s) in presto")
    engineExecutorContext.appendStdout(LogUtils.generateInfo(s"Fetched $columnCount col(s) : $rows row(s) in presto"));
    engineExecutorContext.sendResultSet(resultSetWriter)
    IOUtils.closeQuietly(resultSetWriter)
  }

  // check presto error
  private def verifyServerError(taskId: String, engineExecutorContext: EngineExecutionContext, statement: StatementClient): ErrorExecuteResponse = {
    engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId))
    if (statement.isFinished) {
      val info: QueryStatusInfo = statement.finalStatusInfo()
      if (info.getError != null) {
        val error = Objects.requireNonNull(info.getError);
        val message: String = s"Presto execute failed (#${info.getId}): ${error.getMessage}"
        var cause: Throwable = null
        if (error.getFailureInfo != null) {
          cause = error.getFailureInfo.toException
        }
        engineExecutorContext.appendStdout(LogUtils.generateERROR(ExceptionUtils.getFullStackTrace(cause)))
        ErrorExecuteResponse(ExceptionUtils.getMessage(cause), cause)
      } else null
    } else if (statement.isClientAborted) {
      warn(s"Presto statement is killed.")
      null
    } else if (statement.isClientError) {
      throw PrestoClientException("Presto client error.")
    } else {
      throw PrestoStateInvalidException("Presto status error. Statement is not finished.")
    }
  }

  private def updateSession(clientSession: ClientSession, statement: StatementClient): ClientSession = {
    var newSession = clientSession
    // update catalog and schema if present
    if (statement.getSetCatalog.isPresent || statement.getSetSchema.isPresent) {
      newSession = ClientSession.builder(newSession)
        .withCatalog(statement.getSetCatalog.orElse(newSession.getCatalog))
        .withSchema(statement.getSetSchema.orElse(newSession.getSchema))
        .build
    }

    // update transaction ID if necessary
    if (statement.isClearTransactionId) newSession = ClientSession.stripTransactionId(newSession)

    var builder: ClientSession.Builder = ClientSession.builder(newSession)

    if (statement.getStartedTransactionId != null) builder = builder.withTransactionId(statement.getStartedTransactionId)

    // update session properties if present
    if (!statement.getSetSessionProperties.isEmpty || !statement.getResetSessionProperties.isEmpty) {
      val sessionProperties: util.Map[String, String] = new util.HashMap[String, String](newSession.getProperties)
      sessionProperties.putAll(statement.getSetSessionProperties)
      sessionProperties.keySet.removeAll(statement.getResetSessionProperties)
      builder = builder.withProperties(sessionProperties)
    }

    // update session roles
    if (!statement.getSetRoles.isEmpty) {
      val roles: util.Map[String, SelectedRole] = new util.HashMap[String, SelectedRole](newSession.getRoles)
      roles.putAll(statement.getSetRoles)
      builder = builder.withRoles(roles)
    }

    // update prepared statements if present
    if (!statement.getAddedPreparedStatements.isEmpty || !statement.getDeallocatedPreparedStatements.isEmpty) {
      val preparedStatements: util.Map[String, String] = new util.HashMap[String, String](newSession.getPreparedStatements)
      preparedStatements.putAll(statement.getAddedPreparedStatements)
      preparedStatements.keySet.removeAll(statement.getDeallocatedPreparedStatements)
      builder = builder.withPreparedStatements(preparedStatements)
    }

    newSession
  }

  override def killAll(): Unit = {

  }

}

object PrestoEngineConnExecutor {

  private val OK_HTTP_CLIENT: OkHttpClient = new OkHttpClient.Builder().socketFactory(new SocketChannelSocketFactory)
    .connectTimeout(PRESTO_HTTP_CONNECT_TIME_OUT.getValue, TimeUnit.SECONDS)
    .readTimeout(PRESTO_HTTP_READ_TIME_OUT.getValue, TimeUnit.SECONDS)
    .build()

}
