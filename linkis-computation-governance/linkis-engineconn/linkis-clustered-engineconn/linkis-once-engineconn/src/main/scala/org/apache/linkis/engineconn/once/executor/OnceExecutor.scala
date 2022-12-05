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

package org.apache.linkis.engineconn.once.executor

import org.apache.linkis.bml.client.BmlClientFactory
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.core.util.EngineConnUtils
import org.apache.linkis.engineconn.executor.entity.{
  ExecutableExecutor,
  LabelExecutor,
  ResourceExecutor
}
import org.apache.linkis.engineconn.executor.service.ManagerService
import org.apache.linkis.engineconn.once.executor.exception.OnceEngineConnErrorException
import org.apache.linkis.governance.common.protocol.task.RequestTask
import org.apache.linkis.governance.common.utils.OnceExecutorContentUtils
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.EngineConnReleaseRequest
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.{JobLabel, Label}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{
  AsynReturnExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable.ArrayBuffer

trait OnceExecutor extends ExecutableExecutor[ExecuteResponse] with LabelExecutor with Logging {

  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override final def execute(engineCreationContext: EngineCreationContext): ExecuteResponse = {
    val onceExecutorExecutionContext = createOnceExecutorExecutionContext(engineCreationContext)
    val arrayBuffer = new ArrayBuffer[Label[_]]
    executorLabels.foreach(l => arrayBuffer += l)
    onceExecutorExecutionContext.setLabels(arrayBuffer.toArray)
    initOnceExecutorExecutionContext(onceExecutorExecutionContext)
    execute(onceExecutorExecutionContext)
  }

  def execute(onceExecutorExecutionContext: OnceExecutorExecutionContext): ExecuteResponse

  protected def createOnceExecutorExecutionContext(
      engineCreationContext: EngineCreationContext
  ): OnceExecutorExecutionContext = {
    val resource =
      engineCreationContext.getOptions.get(OnceExecutorContentUtils.ONCE_EXECUTOR_CONTENT_KEY)
    if (StringUtils.isEmpty(resource)) {
      throw new OnceEngineConnErrorException(
        12560,
        OnceExecutorContentUtils.ONCE_EXECUTOR_CONTENT_KEY + " is not exist."
      )
    }
    val bmlResource = OnceExecutorContentUtils.valueToResource(resource)
    val bmlClient = BmlClientFactory.createBmlClient(engineCreationContext.getUser)
    val contentStr = Utils.tryFinally {
      val inputStream = bmlClient
        .downloadResource(
          engineCreationContext.getUser,
          bmlResource.getResourceId,
          bmlResource.getVersion
        )
        .inputStream
      Utils.tryFinally(IOUtils.toString(inputStream, Configuration.BDP_ENCODING.getValue))(
        IOUtils.closeQuietly(inputStream)
      )
    }(bmlClient.close())
    val contentMap = EngineConnUtils.GSON.fromJson(contentStr, classOf[util.Map[String, Object]])
    val onceExecutorContent = OnceExecutorContentUtils.mapToContent(contentMap)
    new OnceExecutorExecutionContext(engineCreationContext, onceExecutorContent)
  }

  protected def initOnceExecutorExecutionContext(
      onceExecutorExecutionContext: OnceExecutorExecutionContext
  ): Unit = {
    val properties = onceExecutorExecutionContext.getOnceExecutorContent.getRuntimeMap
    if (properties.containsKey(RequestTask.RESULT_SET_STORE_PATH)) {
      onceExecutorExecutionContext.setStorePath(
        properties.get(RequestTask.RESULT_SET_STORE_PATH).toString
      )
      logger.info(s"ResultSet storePath: ${onceExecutorExecutionContext.getStorePath}.")
    }
    if (onceExecutorExecutionContext.getOnceExecutorContent.getExtraLabels != null) {
      val extraLabelsList = LabelBuilderFactoryContext.getLabelBuilderFactory
        .getLabels(onceExecutorExecutionContext.getOnceExecutorContent.getExtraLabels)
      val extraLabels = new ArrayBuffer[Label[_]]()
      extraLabelsList.foreach(executorLabels += _)
      onceExecutorExecutionContext.setLabels(
        onceExecutorExecutionContext.getLabels ++: extraLabels.toArray
      )
    }
    onceExecutorExecutionContext.getLabels.foreach {
      case jobLabel: JobLabel =>
        onceExecutorExecutionContext.setJobId(jobLabel.getJobId)
        logger.info(s"JobId: ${onceExecutorExecutionContext.getJobId}.")
      case _ =>
    }
  }

  override def init(): Unit = tryReady()

  override def tryReady(): Boolean = true

}

trait ManageableOnceExecutor extends AccessibleExecutor with OnceExecutor with ResourceExecutor {

  private val notifyListeners = new ArrayBuffer[ExecuteResponse => Unit]
  private var response: ExecuteResponse = _

  override def tryReady(): Boolean = {
    transition(NodeStatus.Running)
    super.tryReady()
  }

  override def execute(
      onceExecutorExecutionContext: OnceExecutorExecutionContext
  ): ExecuteResponse = {
    submit(onceExecutorExecutionContext)
    waitToRunning()
    transition(NodeStatus.Busy)
    new AsynReturnExecuteResponse {
      override def notify(rs: ExecuteResponse => Unit): Unit = notifyListeners += rs
    }
  }

  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit

  protected def waitToRunning(): Unit

  def waitForComplete(): Unit = this synchronized wait()

  def getResponse: ExecuteResponse = response

  protected def setResponse(response: ExecuteResponse): Unit = this.response = response

  override protected def onStatusChanged(fromStatus: NodeStatus, toStatus: NodeStatus): Unit = {
    if (NodeStatus.isCompleted(toStatus)) {
      if (response == null) toStatus match {
        case NodeStatus.Success => response = SuccessExecuteResponse()
        case _ => response = ErrorExecuteResponse("Unknown reason.", null)
      }
      Utils.tryFinally(notifyListeners.foreach(_(getResponse)))(this synchronized notifyAll)
    }
    super.onStatusChanged(fromStatus, toStatus)
  }

  override def tryShutdown(): Boolean = tryFailed()

  def tryFailed(): Boolean = {
    if (isClosed) return true
    val msg = s"$getId has failed with old status $getStatus, now stop it."
    logger.error(msg)
    Utils.tryFinally {
      this.ensureAvailable(transition(NodeStatus.Failed))
      close()
    }(stopOnceExecutor(msg))
    true
  }

  def trySucceed(): Boolean = {
    if (isClosed) return true
    val msg = s"$getId has succeed with old status $getStatus, now stop it."
    logger.warn(msg)
    Utils.tryFinally {
      this.ensureAvailable(transition(NodeStatus.Success))
      close()
    }(stopOnceExecutor(msg))
    true
  }

  private def stopOnceExecutor(msg: String): Unit = {
    val engineReleaseRequest = new EngineConnReleaseRequest(
      Sender.getThisServiceInstance,
      Utils.getJvmUser,
      msg,
      EngineConnObject.getEngineCreationContext.getTicketId
    )
    engineReleaseRequest.setNodeStatus(getStatus)
    Utils.tryAndWarn(Thread.sleep(500))
    logger.info("To send release request to linkis manager")
    ManagerService.getManagerService.requestReleaseEngineConn(engineReleaseRequest)
    ShutdownHook.getShutdownHook.notifyStop()
  }

  override protected def callback(): Unit = {}

  override def supportCallBackLogs(): Boolean = true

}
