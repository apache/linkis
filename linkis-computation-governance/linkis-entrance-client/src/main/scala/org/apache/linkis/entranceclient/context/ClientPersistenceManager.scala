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
 
package org.apache.linkis.entranceclient.context

import org.apache.linkis.entrance.EntranceContext
import org.apache.linkis.entrance.persistence.{PersistenceEngine, PersistenceManager, ResultSetEngine}
import org.apache.linkis.entranceclient.execute
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.OutputExecuteResponse
import org.apache.linkis.scheduler.queue.Job

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
