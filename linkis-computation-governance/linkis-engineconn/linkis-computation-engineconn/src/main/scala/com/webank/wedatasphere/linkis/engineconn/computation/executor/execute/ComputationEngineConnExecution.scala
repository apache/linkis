/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.execute

import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.execution.EngineExecution
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{Executor, ResourceExecutor}
import com.webank.wedatasphere.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import com.webank.wedatasphere.linkis.engineconn.executor.service.ManagerService
import com.webank.wedatasphere.linkis.governance.common.exception.engineconn.{EngineConnExecutorErrorCode, EngineConnExecutorErrorException}
import com.webank.wedatasphere.linkis.manager.common.protocol.resource.ResourceUsedProtocol
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper


class ComputationEngineConnExecution extends EngineExecution with Logging {

  private val executorManager: ExecutorManager = ExecutorManager.getInstance()

  /**
    * 创建出默认的Executor
    *
    * @param engineCreationContext
   * @param engineConn
   */
  override def execute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    Utils.tryCatch{
      init(engineCreationContext)
      val defaultExecutor = executorManager.getDefaultExecutor.asInstanceOf[ComputationExecutor]
      reportUsedResource(defaultExecutor, engineCreationContext)
      reportLabel(defaultExecutor.getExecutorLabels())
      info(s"Engine Executor ${defaultExecutor.getId()} started.")
      info("Debug: engineCreationContext: " + BDPJettyServerHelper.gson.toJson(engineCreationContext))
    }{
      t =>
        error(s"Init executor error. ", t)
        throw new EngineConnExecutorErrorException(EngineConnExecutorErrorCode.INIT_EXECUTOR_FAILED, "Init executor failed. ", t)
    }
  }

  private def init(engineCreationContext: EngineCreationContext): Unit = {
    //    msContext.getPublisher.publish(RequestTaskExecute)
    val listenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext
    listenerBusContext.getEngineConnAsyncListenerBus.start()
  }

  private def reportUsedResource(executor: Executor, engineCreationContext: EngineCreationContext): Unit = {
    Utils.tryCatch(
      executor match {
        case resourceExecutor: ResourceExecutor =>
          ManagerService.getManagerService.reportUsedResource(ResourceUsedProtocol(Sender.getThisServiceInstance, resourceExecutor.getCurrentNodeResource(), engineCreationContext.getTicketId))
        case _ =>
          info("do not to report usedResource")
      }
    ) {
      t => error("ReportUsedResource error. cause: " + t.getCause)
    }
  }

  private def reportLabel(labels: util.List[Label[_]]): Unit = {
    ManagerService.getManagerService.labelReport(labels)
    info("Reported label to am.")
  }
}
