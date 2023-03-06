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

package org.apache.linkis.engineplugin.elasticsearch.executor

import org.apache.linkis.common.utils.{Logging, OverloadUtils, Utils}
import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.{
  ConcurrentComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineplugin.elasticsearch.conf.{
  ElasticSearchConfiguration,
  ElasticSearchEngineConsoleConf
}
import org.apache.linkis.engineplugin.elasticsearch.executor.client.{
  ElasticSearchErrorResponse,
  ElasticSearchExecutor,
  ElasticSearchJsonResponse,
  ElasticSearchTableResponse
}
import org.apache.linkis.engineplugin.elasticsearch.executor.client.ElasticSearchErrorResponse
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadResource,
  NodeResource
}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{
  AliasOutputExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse
}
import org.apache.linkis.storage.LineRecord
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.TableMetaData

import org.apache.commons.io.IOUtils

import org.springframework.util.CollectionUtils

import java.util
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

import com.google.common.cache.{Cache, CacheBuilder, RemovalListener, RemovalNotification}

class ElasticSearchEngineConnExecutor(
    override val outputPrintLimit: Int,
    val id: Int,
    runType: String
) extends ConcurrentComputationExecutor(outputPrintLimit)
    with Logging {

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  private val elasticSearchExecutorCache: Cache[String, ElasticSearchExecutor] = CacheBuilder
    .newBuilder()
    .expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .removalListener(new RemovalListener[String, ElasticSearchExecutor] {

      override def onRemoval(
          notification: RemovalNotification[String, ElasticSearchExecutor]
      ): Unit = {
        notification.getValue.close
        val task = getTaskById(notification.getKey)
        if (!ExecutionNodeStatus.isCompleted(task.getStatus)) {
          killTask(notification.getKey)
        }
      }

    })
    .maximumSize(EngineConnConstant.MAX_TASK_NUM)
    .build()

  override def init(): Unit = {
    super.init()
  }

  override def execute(engineConnTask: EngineConnTask): ExecuteResponse = {

    val properties: util.Map[String, String] = buildRuntimeParams(engineConnTask)
    logger.info(s"The elasticsearch properties is: $properties")

    val elasticSearchExecutor = ElasticSearchExecutor(runType, properties)
    elasticSearchExecutor.open
    elasticSearchExecutorCache.put(engineConnTask.getTaskId, elasticSearchExecutor)
    super.execute(engineConnTask)
  }

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {
    val taskId = engineExecutorContext.getJobId.get
    val elasticSearchExecutor = elasticSearchExecutorCache.getIfPresent(taskId)
    val elasticSearchResponse = elasticSearchExecutor.executeLine(code)

    elasticSearchResponse match {
      case ElasticSearchTableResponse(columns, records) =>
        val metaData = new TableMetaData(columns)
        val resultSetWriter =
          engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
        resultSetWriter.addMetaData(metaData)
        records.foreach(record => resultSetWriter.addRecord(record))
        val output = resultSetWriter.toString
        Utils.tryQuietly {
          IOUtils.closeQuietly(resultSetWriter)
        }
        AliasOutputExecuteResponse(null, output)
      case ElasticSearchJsonResponse(content) =>
        val resultSetWriter =
          engineExecutorContext.createResultSetWriter(ResultSetFactory.TEXT_TYPE)
        resultSetWriter.addMetaData(null)
        content.split("\\n").foreach(item => resultSetWriter.addRecord(new LineRecord(item)))
        val output = resultSetWriter.toString
        Utils.tryQuietly {
          IOUtils.closeQuietly(resultSetWriter)
        }
        AliasOutputExecuteResponse(null, output)
      case ElasticSearchErrorResponse(message, body, cause) =>
        ErrorExecuteResponse(message, cause)
    }
  }

  private def buildRuntimeParams(engineConnTask: EngineConnTask): util.Map[String, String] = {

    // parameters specified at runtime
    var executorProperties = engineConnTask.getProperties.asInstanceOf[util.Map[String, String]]
    if (executorProperties == null) {
      executorProperties = new util.HashMap[String, String]()
    }

    // global  engine params by console
    val globalConfig: util.Map[String, String] =
      Utils.tryAndWarn(ElasticSearchEngineConsoleConf.getCacheMap(engineConnTask.getLables))

    if (!executorProperties.isEmpty) {
      globalConfig.putAll(executorProperties)
    }

    globalConfig
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = null

  override def progress(taskID: String): Float = 0.0f

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] =
    Array.empty[JobProgressInfo]

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (!CollectionUtils.isEmpty(labels)) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

  override def supportCallBackLogs(): Boolean = false

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = null

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

  override def getConcurrentLimit: Int =
    ElasticSearchConfiguration.ENGINE_CONCURRENT_LIMIT.getValue

  override def killTask(taskId: String): Unit = {
    Utils.tryAndWarn {
      val elasticSearchExecutor = elasticSearchExecutorCache.getIfPresent(taskId)
      if (null != elasticSearchExecutor) {
        elasticSearchExecutor.close
      }
    }
    super.killTask(taskId)
  }

  override def killAll(): Unit = {
    elasticSearchExecutorCache
      .asMap()
      .values()
      .asScala
      .foreach(e => e.close)
  }

  override def transformTaskStatus(task: EngineConnTask, newStatus: ExecutionNodeStatus): Unit = {
    super.transformTaskStatus(task, newStatus)
    if (ExecutionNodeStatus.isCompleted(newStatus)) {
      elasticSearchExecutorCache.invalidate(task.getTaskId)
    }
  }

}
