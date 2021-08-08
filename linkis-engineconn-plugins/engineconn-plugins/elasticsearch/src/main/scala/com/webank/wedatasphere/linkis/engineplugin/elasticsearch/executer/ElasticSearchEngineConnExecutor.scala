package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer

import java.util
import java.util.concurrent.TimeUnit

import com.google.common.cache.{Cache, CacheBuilder, RemovalListener, RemovalNotification}
import com.webank.wedatasphere.linkis.common.utils.{Logging, OverloadUtils, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.entity.EngineConnTask
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.{ConcurrentComputationExecutor, EngineExecutionContext}
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client.{ElasticSearchErrorResponse, ElasticSearchExecutor, ElasticSearchJsonResponse, ElasticSearchTableResponse}
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.governance.common.protocol.task.RequestTask
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, LoadResource, NodeResource}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, ErrorExecuteResponse, ExecuteResponse}
import com.webank.wedatasphere.linkis.storage.LineRecord
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils
import org.springframework.util.CollectionUtils

import scala.collection.JavaConverters._

class ElasticSearchEngineConnExecutor(override val outputPrintLimit: Int, val id: Int, runType: String) extends ConcurrentComputationExecutor(outputPrintLimit) with Logging {

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  private val elasticSearchExecutorCache: Cache[String, ElasticSearchExecutor] = CacheBuilder.newBuilder()
    .expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .removalListener(new RemovalListener[String, ElasticSearchExecutor] {
      override def onRemoval(notification: RemovalNotification[String, ElasticSearchExecutor]): Unit = {
        notification.getValue.close
        killTask(notification.getKey)
      }
    })
    .maximumSize(EngineConnConstant.MAX_TASK_NUM).build()

  override def init():Unit = {
    super.init()
  }


  override def execute(engineConnTask: EngineConnTask): ExecuteResponse = {
    val storePath = engineConnTask.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString
    val elasticSearchExecutor = ElasticSearchExecutor(runType, engineConnTask.getProperties)
    elasticSearchExecutor.open
    elasticSearchExecutorCache.put(engineConnTask.getTaskId, elasticSearchExecutor)
    super.execute(engineConnTask)
  }

  override def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val taskId = engineExecutorContext.getJobId.get
    val elasticSearchExecutor = elasticSearchExecutorCache.getIfPresent(taskId)
    val elasticSearchResponse = elasticSearchExecutor.executeLine(code)

    elasticSearchResponse match {
      case ElasticSearchTableResponse(columns, records) =>
        val metaData = new TableMetaData(columns)
        val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
        resultSetWriter.addMetaData(metaData)
        records.foreach(record => resultSetWriter.addRecord(record))
        val output = resultSetWriter.toString
        Utils.tryQuietly {
          IOUtils.closeQuietly(resultSetWriter)
        }
        AliasOutputExecuteResponse(null, output)
      case ElasticSearchJsonResponse(content) =>
        val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TEXT_TYPE)
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

  override def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = null

  override def progress(): Float = 0.0f

  override def getProgressInfo: Array[JobProgressInfo] = Array.empty[JobProgressInfo]

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

  override def getConcurrentLimit: Int = ElasticSearchConfiguration.ENGINE_CONCURRENT_LIMIT.getValue

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
    elasticSearchExecutorCache.asMap()
      .values().asScala
      .foreach(e => e.close)
  }

  override def transformTaskStatus(task: EngineConnTask, newStatus: ExecutionNodeStatus): Unit = {
    if (ExecutionNodeStatus.isCompleted(newStatus)) {
      elasticSearchExecutorCache.invalidate(task.getTaskId)
    }
    super.transformTaskStatus(task, newStatus)
  }

}
