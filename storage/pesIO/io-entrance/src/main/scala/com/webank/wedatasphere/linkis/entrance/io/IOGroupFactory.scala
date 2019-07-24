/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.entrance.io


import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientGroupFactoryBeanAnnotation
import com.webank.wedatasphere.linkis.entranceclient.context.ClientTask
import com.webank.wedatasphere.linkis.entranceclient.scheduler.ClientGroupFactory
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelGroup
import com.webank.wedatasphere.linkis.scheduler.queue.{Group, SchedulerEvent}
import com.webank.wedatasphere.linkis.storage.domain.MethodEntitySerializer
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import javax.annotation.PostConstruct

/**
  * Created by johnnwang on 2018/11/6.
  */
@ClientGroupFactoryBeanAnnotation
class IOGroupFactory extends ClientGroupFactory with Logging {

  private val group: Array[ParallelGroup] = new Array[ParallelGroup](2)

  private var maxCapacity: Int = 1000
  private var maxRunningJobs: Int = 30
  private var maxAskExecutorTimes: Long = 5 * 60 * 1000


  override def setQueueSize(maxCapacity: Int) = this.maxCapacity = maxCapacity

  override def setMaxRunningJobs(maxRunningJobs: Int) = this.maxRunningJobs = maxRunningJobs

  override def setMaxAskExecutorTimes(maxAskExecutorTimes: Long) = this.maxAskExecutorTimes = maxAskExecutorTimes

  @PostConstruct
  def init(): Unit = {
    info("IOGroupFactory init ")
    group(0) = createGroup(StorageUtils.FILE)
    group(1) = createGroup(StorageUtils.HDFS)
  }

  private def createGroup(groupName: String):ParallelGroup ={
    val group = new ParallelGroup(groupName, 100, maxCapacity)
    group.setMaxAskExecutorTimes(maxAskExecutorTimes)
    group.setMaxRunningJobs(maxRunningJobs)
    info(s"This group:${group.getGroupName} " +
      s"maxRunningJobs:${group.getMaxRunningJobs} " +
      s"maxAskExecutorTimes:${group.getMaxAskExecutorTimes} maxCapacity:${group.getMaximumCapacity}")
    group
  }

  override def getOrCreateGroup(groupName: String): Group = {
      if (StorageUtils.HDFS == groupName) {
        group(1)
      }  else {
        group(0)
      }

  }

  override def getGroupNameByEvent(event: SchedulerEvent): String = event match {
    case entranceJob: EntranceJob => {
      val code = entranceJob.getTask match {
        case task: RequestPersistTask => task.getCode
        case task: ClientTask => task.getCode
        case _ => null
      }
      if (code != null) {
        val method = MethodEntitySerializer.deserializer(code)
        if (method.fsType == StorageUtils.HDFS)
          return StorageUtils.HDFS
      }
      StorageUtils.FILE
    }
  }
}
