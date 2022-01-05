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
 
package org.apache.linkis.entranceclient.execute

import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.governance.common.entity.task.RequestPersistTask
import org.apache.linkis.scheduler.executer.{ExecuteRequest, JobExecuteRequest}
import org.apache.linkis.scheduler.queue.SchedulerEventState.SchedulerEventState
import org.apache.linkis.scheduler.queue.{JobInfo, SchedulerEventState}

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
