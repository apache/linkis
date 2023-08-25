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

package org.apache.linkis.manager.engineplugin.hbase.executor

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{OverloadUtils, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{
  ConcurrentComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.governance.common.protocol.conf.{
  RequestQueryEngineConfig,
  ResponseQueryConfig
}
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadResource,
  NodeResource
}
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.engineplugin.hbase.conf.HBaseConfiguration
import org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant
import org.apache.linkis.manager.engineplugin.hbase.shell.{
  HBaseShellSession,
  HBaseShellSessionManager,
  Result
}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.CacheableProtocol
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.{RPCMapCache, Sender}
import org.apache.linkis.scheduler.executer.{
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}

import org.apache.commons.collections.CollectionUtils

import java.util
import java.util.Collections

import scala.collection.JavaConverters._

class HBaseEngineConnExecutor(val id: Int) extends ConcurrentComputationExecutor {
  private val shellSessionManager = HBaseShellSessionManager.getInstance();
  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  override def init(): Unit = {
    logger.info("hbase executor start init.")
    super.init()
  }

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {
    val realCode = code.trim()
    var properties: util.Map[String, String] = Collections.emptyMap()
    Utils.tryCatch({
      properties = getHBaseRuntimeParams(engineExecutorContext)
    }) { e: Throwable =>
      logger.error(s"try to build hbase runtime params error! $e")
      return ErrorExecuteResponse(e.getMessage, e)
    }
    logger.info(s"The hbase properties is: $properties")
    var shellSession: HBaseShellSession = null
    Utils.tryCatch({
      shellSession = shellSessionManager.getHBaseShellSession(properties)
    }) { e: Throwable =>
      logger.error(s"created hbase shell session error! $e")
      return ErrorExecuteResponse("created hbase shell session error!", e)
    }
    val result: Result = shellSession.execute(realCode)
    if (!result.isSuccess) {
      return ErrorExecuteResponse(result.getResult, result.getThrowable)
    }
    engineExecutorContext.appendStdout(result.getResult)
    SuccessExecuteResponse()
  }

  private def getHBaseRuntimeParams(
      engineExecutorContext: EngineExecutionContext
  ): util.Map[String, String] = {
    val execCodeUser = getExecCodeUser(engineExecutorContext)
    var executorProperties: util.Map[String, Object] =
      engineExecutorContext.getProperties
    if (executorProperties == null) {
      executorProperties = new util.HashMap[String, Object]()
    }
    logger.info(s"executorProperties is: $executorProperties")
    val globalConfig: util.Map[String, String] = new util.HashMap[String, String]()
    Utils.tryAndWarn(HBaseEngineConfig.getCacheMap(engineExecutorContext.getLabels))
    globalConfig.put(HBaseEngineConnConstant.KERBEROS_PROXY_USER, execCodeUser)
    if (!executorProperties.isEmpty) {
      val _executorProperties: Map[String, Object] = executorProperties.asScala.toMap
      _executorProperties.foreach(d => {
        val k: String = d._1
        val v: Object = d._2
        if (k.startsWith(HBaseEngineConnConstant.LINKIS_PREFIX)) {
          globalConfig.put(k, String.valueOf(v))
        }
      })
    }
    globalConfig
  }

  private def getExecCodeUser(engineExecutionContext: EngineExecutionContext): String = {
    val userCreatorLabel = engineExecutionContext.getLabels
      .find(_.isInstanceOf[UserCreatorLabel])
      .get
      .asInstanceOf[UserCreatorLabel]
    userCreatorLabel.getUser
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = null

  override def progress(taskID: String): Float = 0

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] =
    Array.empty[JobProgressInfo]

  override def supportCallBackLogs(): Boolean = false

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (!CollectionUtils.isEmpty(labels)) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

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

  override def getConcurrentLimit: Int = HBaseConfiguration.HBASE_CONCURRENT_LIMIT.getValue

  override def killAll(): Unit = {
    logger.info("Killing all query task.")

    logger.info("All query task has killed successfully.")
  }

  override def killTask(taskId: String): Unit = {
    logger.info(s"Killing hbase query task $taskId")
    super.killTask(taskId)
    logger.info(s"The query task $taskId has killed successfully.")
  }

  override def getId: String = Sender.getThisServiceInstance.getInstance + s"_$id"
}

object HBaseEngineConfig
    extends RPCMapCache[Array[Label[_]], String, String](
      Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue
    ) {

  override protected def createRequest(labels: Array[Label[_]]): CacheableProtocol = {
    val userCreatorLabel =
      labels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
    val engineTypeLabel =
      labels.find(_.isInstanceOf[EngineTypeLabel]).get.asInstanceOf[EngineTypeLabel]
    RequestQueryEngineConfig(userCreatorLabel, engineTypeLabel)
  }

  override protected def createMap(any: Any): util.Map[String, String] = any match {
    case response: ResponseQueryConfig => response.getKeyAndValue
  }

}
