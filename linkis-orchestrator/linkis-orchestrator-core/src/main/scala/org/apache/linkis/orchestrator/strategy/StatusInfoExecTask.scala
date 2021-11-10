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
 
package org.apache.linkis.orchestrator.strategy

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.exception.OrchestratorErrorCodeSummary
import org.apache.linkis.orchestrator.execution.FailedTaskResponse
import org.apache.linkis.orchestrator.execution.impl.DefaultFailedTaskResponse
import org.apache.linkis.orchestrator.plans.physical.ExecTask

import scala.collection.mutable
/**
  *
  *
  */
trait  StatusInfoExecTask extends ExecTask with Logging{

  /**
   * This method is called by ExecTaskRunner, ExecTask cannot be called directly, which will cause repeated placement of status information
   * 该方法由ExecTaskRunner进行调用，ExecTask不能直接调用，会导致重复放置状态信息
   * @param execTaskStatusInfo
   */
   def addExecTaskStatusInfo(execTaskStatusInfo: ExecTaskStatusInfo): Unit = {
    val statusInfoMap = if(null != getPhysicalContext.get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)) {
      getPhysicalContext.get(StatusInfoExecTask.STATUS_INFO_MAP_KEY).asInstanceOf[mutable.Map[String, ExecTaskStatusInfo]]
    } else {
      new mutable.HashMap[String, ExecTaskStatusInfo]()
    }
    statusInfoMap.put(getId, execTaskStatusInfo)
    getPhysicalContext.set(StatusInfoExecTask.STATUS_INFO_MAP_KEY, statusInfoMap)
  }

   def getChildrenExecTaskStatusInfo(): Map[String, ExecTaskStatusInfo] = {
    val map = getPhysicalContext.get(StatusInfoExecTask.STATUS_INFO_MAP_KEY)
    if (null != map) {
      val statusMap = map.asInstanceOf[mutable.Map[String, ExecTaskStatusInfo]]
      val childrenStatus = getChildren.map(execTask => execTask.getId -> statusMap.get(execTask.getId)).filter(_._2.isDefined).map { tuple =>
        tuple._1 -> tuple._2.get
      }.toMap
      return childrenStatus
    }
    null
  }


  def parseChildrenErrorInfo(errorExecTasks: Map[String, ExecTaskStatusInfo]): String = {
     if (null != errorExecTasks && errorExecTasks.nonEmpty) {
        val errorReason = errorExecTasks.map { entry =>
          val execTaskId = entry._1
          val statusInfo = entry._2
          val errorMsg = statusInfo.taskResponse match {
            case failedTaskResponse: FailedTaskResponse =>
              s"Task is Failed,errorMsg: ${failedTaskResponse.getErrorMsg}"
            case _ => s"Task($execTaskId) status not succeed,is ${statusInfo.nodeStatus}"
          }
          errorMsg
        }.mkString(";")
        error(s"There are Tasks execution failure of stage ${getIDInfo()}, now mark ExecutionTask as failed")
        return  errorReason
      }
    "error info is null"
  }

  def getErrorChildrenExecTasks: Option[Map[String, ExecTaskStatusInfo]] = {
    val statusInfos = getChildrenExecTaskStatusInfo()
    if (null != statusInfos) {
      val errorExecTask = statusInfos.filter { entry =>
        !ExecutionNodeStatus.isSucceed(entry._2.nodeStatus) && ExecutionNodeStatus.isCompleted(entry._2.nodeStatus)
      }
      if (null != errorExecTask && errorExecTask.nonEmpty) {
        return  Some(errorExecTask)
      }
    }
    None
  }

}

object StatusInfoExecTask {

  val STATUS_INFO_MAP_KEY = "STATUS_INFO_MAP_KEY"

}