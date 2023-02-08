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

package org.apache.linkis.orchestrator.ecm.service.impl

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.protocol.task.{
  RequestTask,
  RequestTaskKill,
  RequestTaskStatus,
  ResponseTaskStatus
}
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.RequestManagerUnlock
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import org.apache.linkis.orchestrator.ecm.service.AbstractEngineConnExecutor
import org.apache.linkis.orchestrator.ecm.utils.ECMPUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer._

/**
 */
class ComputationEngineConnExecutor(engineNode: EngineNode) extends AbstractEngineConnExecutor {

  private val locker: String = engineNode.getLock

  override def getServiceInstance: ServiceInstance = engineNode.getServiceInstance

  private def getEngineConnSender: Sender = Sender.getSender(getServiceInstance)

  override def getTicketId: String = engineNode.getTicketId

  override def close(): Unit = {
    logger.info("Start to release engineConn {}", getServiceInstance)
    val requestManagerUnlock =
      RequestManagerUnlock(getServiceInstance, locker, Sender.getThisServiceInstance)
    killAll()
    getManagerSender.send(requestManagerUnlock)
    logger.debug("Finished to release engineConn {}", getServiceInstance)
  }

  override def useEngineConn: Boolean = {
    if (isAvailable) {
      this.available = false
      true
    } else {
      false
    }
  }

  override def unUseEngineConn: Unit = {
    if (!isAvailable) {
      this.available = true
    }
  }

  override def execute(requestTask: RequestTask): ExecuteResponse = {
    logger.debug(
      "Start to submit task {} to engineConn({})",
      requestTask.getSourceID(): Any,
      getServiceInstance: Any
    )
    requestTask.setLabels(ECMPUtils.filterJobStrategyLabel(requestTask.getLabels))
    requestTask.setLock(this.locker)
    getEngineConnSender.ask(requestTask) match {
      case submitResponse: SubmitResponse =>
        logger.info(
          "Succeed to submit task {} to engineConn({}), Get asyncResponse execID is {}",
          Array(requestTask.getSourceID(), getServiceInstance, submitResponse): _*
        )
        getRunningTasks.put(submitResponse.taskId, requestTask)
        submitResponse
      case outPutResponse: OutputExecuteResponse =>
        logger.info(
          "engineConn({}) Succeed to execute task {}, and get Res",
          getServiceInstance: Any,
          requestTask.getSourceID(): Any
        )
        outPutResponse
      case errorExecuteResponse: ErrorExecuteResponse =>
        logger.error(
          "engineConn({}) Failed to execute task {} ,error msg {}",
          Array(
            getServiceInstance,
            requestTask.getSourceID(),
            errorExecuteResponse.message,
            errorExecuteResponse.t
          ): _*
        )
        errorExecuteResponse
      case successExecuteResponse: SuccessExecuteResponse =>
        logger.info(
          "engineConn({}) Succeed to execute task {}, no res",
          getServiceInstance: Any,
          requestTask.getSourceID(): Any
        )
        successExecuteResponse
      case _ =>
        throw new ECMPluginErrorException(
          ECMPluginConf.ECM_ERROR_CODE,
          s"engineConn($getServiceInstance) Failed to execute task${requestTask.getSourceID()}, get response error"
        )
    }
  }

  override def killTask(execId: String): Boolean = {
    Utils.tryCatch {
      logger.info(
        "begin to send RequestTaskKill to engineConn({}), execID: {}",
        getServiceInstance: Any,
        execId: Any
      )
      getEngineConnSender.send(RequestTaskKill(execId))
      logger.info(
        "Finished to send RequestTaskKill to engineConn({}), execID: {}",
        getServiceInstance: Any,
        execId: Any
      )
      true
    } { t: Throwable =>
      logger.error(
        "Failed to kill task {} engineConn({})",
        Array(execId, getServiceInstance, t): _*
      )
      false
    }
  }

  override def killAll(): Boolean = {
    val execIds = getRunningTasks.keySet()
    if (null == execIds || execIds.isEmpty) {
      val iterator = execIds.iterator()
      while (iterator.hasNext) {
        val execId = iterator.next()
        killTask(execId)
      }
    }
    getRunningTasks.clear()
    true
  }

  override def pause(execId: String): Boolean = {
    // TODO
    true
  }

  override def pauseAll(): Boolean = {
    // TODO
    true
  }

  override def resume(execId: String): Boolean = {
    // TODO
    true
  }

  override def resumeAll(): Boolean = {
    // TODO
    true
  }

  override def status(execId: String): ExecutionNodeStatus = {
    getEngineConnSender.ask(RequestTaskStatus(execId)) match {
      case ResponseTaskStatus(execId, status) =>
        status
      case _ =>
        throw new ECMPluginErrorException(
          ECMPluginConf.ECM_ERROR_CODE,
          s"Failed to get engineConn($getServiceInstance) status "
        )
    }
  }

  private def getManagerSender: Sender =
    Sender.getSender(GovernanceCommonConf.MANAGER_SERVICE_NAME.getValue)

}

class ComputationConcurrentEngineConnExecutor(engineNode: EngineNode, parallelism: Int)
    extends ComputationEngineConnExecutor(engineNode) {

  override def useEngineConn: Boolean = {
    isAvailable
  }

  override def isAvailable: Boolean = {
    if (parallelism > getRunningTasks.size()) {
      true
    } else {
      false
    }
  }

}
