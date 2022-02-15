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
 
package org.apache.linkis.entrance.log

import org.apache.linkis.entrance.EntranceParser
import org.apache.linkis.entrance.persistence.PersistenceManager
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.entity.task.RequestPersistTask
import org.apache.linkis.protocol.task.Task
import org.apache.linkis.scheduler.queue.Job
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}


trait ErrorCodeListener {

  def onErrorCodeCreated(job: Job, errorCode: String, detailErrorMsg: String)
}

class PersistenceErrorCodeListener extends ErrorCodeListener{

  private var persistenceManager: PersistenceManager = _
  private var entranceParser: EntranceParser = _

  private val logger:Logger = LoggerFactory.getLogger(classOf[PersistenceErrorCodeListener])

  def setPersistenceManager(persistenceManager: PersistenceManager): Unit = this.persistenceManager = persistenceManager
  def getPersistenceManager = persistenceManager
  def setEntranceParser(entranceParser: EntranceParser): Unit = this.entranceParser = entranceParser
  def getEntranceParser = entranceParser

  /**
    * onErrorCodeCreated: When a job is running, it terminates unexpectedly or generates an error, and the error code and error information need to be persisted to the database.
  * The subsequent front end will get this error code when querying a specific task as a key error message.
    * onErrorCodeCreated: 当一个job在运行的时候，意外终止或者产生错误，需要将错误码和错误信息持久化到数据库中
    * 后续前端在查询一个具体任务的时候，会得到这个错误码，作为关键错误信息
    * @param job Wrong job(出错的job)
    * @param errorCode error code(错误码)
    * @param detailErrorMsg wrong description（错误描述）
    */
  override def onErrorCodeCreated(job: Job, errorCode: String, detailErrorMsg: String): Unit = {
    val jobRequest: JobRequest = this.entranceParser.parseToJobRequest(job)
    if (StringUtils.isEmpty(jobRequest.getErrorDesc) || "50032".equals(errorCode)) {
      jobRequest.setErrorCode(Integer.parseInt(errorCode))
      val realErrorMsg = if (detailErrorMsg.length <= 255) detailErrorMsg else detailErrorMsg.substring(0, 255)
      jobRequest.setErrorDesc(realErrorMsg)
      persistenceManager.createPersistenceEngine().updateIfNeeded(jobRequest)
    }
  }
}
