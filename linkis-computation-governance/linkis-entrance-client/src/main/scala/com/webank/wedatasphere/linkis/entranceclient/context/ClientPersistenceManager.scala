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

package com.webank.wedatasphere.linkis.entranceclient.context

import com.webank.wedatasphere.linkis.entrance.EntranceContext
import com.webank.wedatasphere.linkis.entrance.persistence.{PersistenceEngine, PersistenceManager, ResultSetEngine}
import com.webank.wedatasphere.linkis.entranceclient.execute
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.scheduler.executer.OutputExecuteResponse
import com.webank.wedatasphere.linkis.scheduler.queue.Job

class ClientPersistenceManager extends PersistenceManager {
  private val persistenceEngine = new ClientPersistenceEngine
  override def getEntranceContext: EntranceContext = null

  override def setEntranceContext(entranceContext: EntranceContext): Unit = null

  override def createPersistenceEngine(): PersistenceEngine = persistenceEngine

  override def createResultSetEngine(): ResultSetEngine = null

  override def onProgressUpdate(job: Job, progress: Float, progressInfo: Array[JobProgressInfo]): Unit = {}

  override def onJobScheduled(job: Job): Unit = {}

  override def onJobInited(job: Job): Unit = {}

  override def onJobRunning(job: Job): Unit = {}

  override def onJobCompleted(job: Job): Unit = {

  }

  override def onResultSetCreated(job: Job, response: OutputExecuteResponse): Unit = job match {
    case j: execute.ClientJob =>
      j.addResultSet(response.getOutput)
      j.incrementResultSetPersisted()
  }


  override def onResultSizeCreated(job: Job, resultSize: Int): Unit = job match {
    case j: execute.ClientJob => j.setResultSize(resultSize)
  }

  override def onJobWaitForRetry(job: Job): Unit = {}
}
