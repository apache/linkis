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

package com.webank.wedatasphere.linkis.entranceclient.execute

import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteRequest, JobExecuteRequest}
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState.SchedulerEventState
import com.webank.wedatasphere.linkis.scheduler.queue.{JobInfo, SchedulerEventState}

import scala.collection.mutable.ArrayBuffer

class ClientJob extends EntranceJob {

  private val resultSets = ArrayBuffer[String]()

  def addResultSet(resultSet: String): Unit = resultSets += resultSet

  def getResultSets: Array[String] = resultSets.toArray

  def waitForComplete(): Unit = {
    if (SchedulerEventState.isCompleted(this.getState)) return
    resultSets synchronized {
      while (!SchedulerEventState.isCompleted(this.getState)) resultSets.wait()
    }
  }

  override protected def isWaitForPersistedTimeout(startWaitForPersistedTime: Long): Boolean = System.currentTimeMillis() - startWaitForPersistedTime >= 3000

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {
    super.afterStateChanged(fromState, toState)
    if(SchedulerEventState.isCompleted(this.getState)) resultSets synchronized resultSets.notify()
  }

  override def init(): Unit = {}

  override protected def jobToExecuteRequest: ExecuteRequest = new ExecuteRequest with JobExecuteRequest {
    override val code: String = getTask.asInstanceOf[RequestPersistTask].getCode
    override val jobId: String = getId
  }

  override def getName: String = getTask.asInstanceOf[RequestPersistTask].getRequestApplicationName + "_" + getTask.asInstanceOf[RequestPersistTask].getUmUser + "_" + getId

  override def getJobInfo: JobInfo = null

  override def close(): Unit = {}
}
