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

package com.webank.wedatasphere.linkis.orchestrator.strategy

import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask

import scala.collection.mutable
/**
  *
  *
  */
trait  StatusInfoExecTask extends ExecTask {

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

}

object StatusInfoExecTask {

  val STATUS_INFO_MAP_KEY = "STATUS_INFO_MAP_KEY"

}