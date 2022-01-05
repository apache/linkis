/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.linkis.governance.common.protocol.task.{RequestTask, RequestTaskKill, RequestTaskStatus, ResponseTaskStatus}
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.RequestManagerUnlock
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import org.apache.linkis.orchestrator.ecm.service.AbstractEngineConnExecutor
import org.apache.linkis.orchestrator.ecm.utils.ECMPUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer._


/**
  *
  *
  */
class ComputationEngineConnExecutor(engineNode: EngineNode) extends AbstractEngineConnExecutor {

  private val locker: String = engineNode.getLock

  override def getServiceInstance: ServiceInstance = engineNode.getServiceInstance

  private def getEngineConnSender: Sender = Sender.getSender(getServiceInstance)

  override def close(): Unit = {
    info(s"Start to release engineConn $getServiceInstance")
    val requestManagerUnlock = RequestManagerUnlock(getServiceInstance, locker, Sender.getThisServiceInstance)
    killAll()
    getManagerSender.send(requestManagerUnlock)
    debug(s"Finished to release engineConn $getServiceInstance")
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
    debug(s"Start to submit task${requestTask.getSourceID()} to engineConn($getServiceInstance)")
    requestTask.setLabels(ECMPUtils.filterJobStrategyLabel(requestTask.getLabels))
    requestTask.setLock(this.locker)
    getEngineConnSender.ask(requestTask) match {
      case submitResponse: SubmitResponse =>
        info(s"Succeed to submit task${requestTask.getSourceID()} to engineConn($getServiceInstance), Get asyncResponse execID is ${submitResponse}")
        getRunningTasks.put(submitResponse.taskId, requestTask)
        submitResponse
      case outPutResponse: OutputExecuteResponse =>
        info(s" engineConn($getServiceInstance) Succeed to execute task${requestTask.getSourceID()}, and get Res")
        outPutResponse
      case errorExecuteResponse: ErrorExecuteResponse =>
        error(s"engineConn($getServiceInstance) Failed to execute task${requestTask.getSourceID()} ,error msg ${errorExecuteResponse.message}", errorExecuteResponse.t)
        errorExecuteResponse
      case successExecuteResponse: SuccessExecuteResponse =>
        info(s" engineConn($getServiceInstance) Succeed to execute task${requestTask.getSourceID()}, no res")
        successExecuteResponse
      case _ =>
        throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE, s"engineConn($getServiceInstance) Failed to execute task${requestTask.getSourceID()}, get response error")
    }
  }

  override def killTask(execId: String): Boolean = {
    Utils.tryCatch {
      info(s"begin to send RequestTaskKill to engineConn($getServiceInstance), execID: $execId")
      getEngineConnSender.send(RequestTaskKill(execId))
      info(s"Finished to send RequestTaskKill to engineConn($getServiceInstance), execID: $execId")
      true
    } { t: Throwable =>
      error(s"Failed to kill task $execId engineConn($getServiceInstance)", t)
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
    true
  }

  override def pause(execId: String): Boolean = {
    //TODO
    true
  }

  override def pauseAll(): Boolean = {
    //TODO
    true
  }

  override def resume(execId: String): Boolean = {
    //TODO
    true
  }

  override def resumeAll(): Boolean = {
    //TODO
    true
  }

  override def status(execId: String): ExecutionNodeStatus = {
    getEngineConnSender.ask(RequestTaskStatus(execId)) match {
      case ResponseTaskStatus(execId, status) =>
        status
      case _ =>
        throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE, s"Failed to get engineConn($getServiceInstance) status ")
    }
  }

  private def getManagerSender: Sender = Sender.getSender(GovernanceCommonConf.MANAGER_SPRING_NAME.getValue)

}

class ComputationConcurrentEngineConnExecutor(engineNode: EngineNode, parallelism: Int) extends
  ComputationEngineConnExecutor(engineNode) {

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