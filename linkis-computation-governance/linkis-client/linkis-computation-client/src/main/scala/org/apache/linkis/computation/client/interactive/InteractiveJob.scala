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

package org.apache.linkis.computation.client.interactive

import org.apache.linkis.computation.client.{LinkisJobBuilder, ResultSetIterable}
import org.apache.linkis.computation.client.job.{
  StorableExistingLinkisJob,
  StorableLinkisJob,
  StorableSubmittableLinkisJob
}
import org.apache.linkis.ujes.client.UJESClient
import org.apache.linkis.ujes.client.request.JobSubmitAction
import org.apache.linkis.ujes.client.response.{JobInfoResult, JobLogResult, JobProgressResult}

import java.util

import scala.collection.mutable.ArrayBuffer

trait InteractiveJob extends StorableLinkisJob {

  private var resultSetList: Array[String] = _
  private val logListeners: ArrayBuffer[LogListener] = new ArrayBuffer[LogListener]()

  private val progressListeners: ArrayBuffer[ProgressListener] =
    new ArrayBuffer[ProgressListener]()

  protected var lastJobLogResult: JobLogResult = _
  protected var lastProgress: JobProgressResult = _

  def existResultSets: Boolean = wrapperId {
    resultSetList = getJobInfoResult.getResultSetList(ujesClient)
    resultSetList != null && resultSetList.nonEmpty
  }

  def getResultSetIterables: Array[ResultSetIterable] = {
    if (!existResultSets) return Array.empty
    resultSetList.map(
      new ResultSetIterable(ujesClient, getJobSubmitResult.getUser, _, getJobMetrics)
    )
  }

  def addLogListener(logListener: LogListener): Unit = {
    logListeners += logListener
    initJobDaemon()
  }

  def addProgressListener(progressListener: ProgressListener): Unit = {
    progressListeners += progressListener
    initJobDaemon()
  }

  override protected def getJobInfoResult: JobInfoResult = {
    lastJobLogResult =
      if (lastJobLogResult == null) ujesClient.log(getJobSubmitResult, 0, 1)
      else ujesClient.log(getJobSubmitResult, lastJobLogResult)
    logListeners.foreach(_.onLogUpdate(lastJobLogResult.getLog))
    val progress = ujesClient.progress(getJobSubmitResult)
    if (lastProgress == null || progress.getProgress > lastProgress.getProgress) {
      lastProgress = progress
      progressListeners.foreach(
        _.onProgressUpdate(lastProgress.getProgress, lastProgress.getProgressInfo)
      )
    }
    super.getJobInfoResult
  }

  def getLogListeners: ArrayBuffer[LogListener] = logListeners

  def getProgressListener: ArrayBuffer[ProgressListener] = progressListeners

}

class SubmittableInteractiveJob(ujesClient: UJESClient, jobSubmitAction: JobSubmitAction)
    extends StorableSubmittableLinkisJob(ujesClient, jobSubmitAction)
    with InteractiveJob {

  private var maxRetry = 0
  private var retryNumber = 0

  private var finishedJobInfoResult: JobInfoResult = _

  override protected def getJobInfoResult: JobInfoResult = {
    lastJobLogResult =
      if (lastJobLogResult == null) ujesClient.log(getJobSubmitResult, 0, 1)
      else ujesClient.log(getJobSubmitResult, lastJobLogResult)
    getLogListeners.foreach(_.onLogUpdate(lastJobLogResult.getLog))
    val progress = ujesClient.progress(getJobSubmitResult)
    if (lastProgress == null || progress.getProgress > lastProgress.getProgress) {
      lastProgress = progress
      getProgressListener.foreach(
        _.onProgressUpdate(lastProgress.getProgress, lastProgress.getProgressInfo)
      )
    }
    if (finishedJobInfoResult != null) return finishedJobInfoResult
    val startTime = System.currentTimeMillis
    val oldJobSubmitResult = getJobSubmitResult
    val jobInfoResult = wrapperId(ujesClient.getJobInfo(getJobSubmitResult))
    getJobMetrics.addClientGetJobInfoTime(System.currentTimeMillis - startTime)
    if (jobInfoResult.isCompleted) {
      if (jobInfoResult.isFailed && jobInfoResult.canRetry && getRetryNumber < getMaxRetry) {
        val retryMsg =
          s"Task ${oldJobSubmitResult.taskID} failed to run, retrying automatically, retry number: ${getRetryNumber}"
        logger.info(retryMsg)
        this.submit()
        addRetryNumber
        val logs = new util.ArrayList[String]()
        val retrySucceedMsg =
          s"The task has been retried, and the new task id is: ${getJobSubmitResult.taskID}"
        logs.add(retryMsg)
        logs.add(retrySucceedMsg)
        getLogListeners.foreach(_.onLogUpdate(logs))
        logger.info(retrySucceedMsg)
        return getJobInfoResult
      }
      getJobMetrics.setClientFinishedTime(System.currentTimeMillis)
      finishedJobInfoResult = jobInfoResult
      logger.info(s"Job-$getId is completed with status " + finishedJobInfoResult.getJobStatus)
      getJobListeners.foreach(_.onJobFinished(this))
    } else if (jobInfoResult.isRunning) getJobListeners.foreach(_.onJobRunning(this))
    jobInfoResult
  }

  def getMaxRetry: Int = maxRetry

  def setMaxRetry(maxRetry: Int): Unit = this.maxRetry = maxRetry

  def addRetryNumber: Unit = retryNumber = retryNumber + 1

  def getRetryNumber: Int = retryNumber

}

class ExistingInteractiveJob(ujesClient: UJESClient, execId: String, taskId: String, user: String)
    extends StorableExistingLinkisJob(ujesClient, execId, taskId, user)
    with InteractiveJob

object InteractiveJob {

  def builder(): InteractiveJobBuilder = new InteractiveJobBuilder

  /**
   * When use this method to create a InteractiveJob, ProgressListener and LogListener cannot be
   * used, because execID does not exist.
   *
   * @param taskId
   *   the id of InteractiveJob
   * @param user
   *   the execute user of InteractiveJob
   * @return
   */
  def build(taskId: String, user: String): InteractiveJob =
    new ExistingInteractiveJob(LinkisJobBuilder.getDefaultUJESClient, null, taskId, user)

  /**
   * Use this method to create a InteractiveJob, it is equivalent with [[builder]].
   *
   * @param execId
   *   the execId of InteractiveJob
   * @param taskId
   *   the taskId of InteractiveJob
   * @param user
   *   the execute user of InteractiveJob
   * @return
   */
  def build(execId: String, taskId: String, user: String): InteractiveJob =
    new ExistingInteractiveJob(LinkisJobBuilder.getDefaultUJESClient, null, taskId, user)

}
