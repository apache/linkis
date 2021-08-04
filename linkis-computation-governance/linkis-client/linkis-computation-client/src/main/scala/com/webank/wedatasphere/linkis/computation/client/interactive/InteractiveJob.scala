package com.webank.wedatasphere.linkis.computation.client.interactive

import com.webank.wedatasphere.linkis.computation.client.{LinkisJobBuilder, ResultSetIterable}
import com.webank.wedatasphere.linkis.computation.client.job.{StorableExistingLinkisJob, StorableLinkisJob, StorableSubmittableLinkisJob}
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.request.JobSubmitAction
import com.webank.wedatasphere.linkis.ujes.client.response.{JobInfoResult, JobLogResult, JobProgressResult}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait InteractiveJob extends StorableLinkisJob {

  private var resultSetList: Array[String] = _
  private val logListeners: ArrayBuffer[LogListener] = new ArrayBuffer[LogListener]()
  private val progressListeners: ArrayBuffer[ProgressListener] = new ArrayBuffer[ProgressListener]()
  private var lastJobLogResult: JobLogResult = _
  private var lastProgress: JobProgressResult = _

  def existResultSets: Boolean = wrapperId {
    resultSetList = getJobInfoResult.getResultSetList(ujesClient)
    resultSetList != null && resultSetList.nonEmpty
  }

  def getResultSetIterables: Array[ResultSetIterable] = {
    if(!existResultSets) return Array.empty
    resultSetList.map(new ResultSetIterable(ujesClient, getJobSubmitResult.getUser, _, getJobMetrics))
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
    lastJobLogResult = if(lastJobLogResult == null) ujesClient.log(getJobSubmitResult, 0, 1) else ujesClient.log(getJobSubmitResult, lastJobLogResult)
    logListeners.foreach(_.onLogUpdate(lastJobLogResult.getLog))
    val progress = ujesClient.progress(getJobSubmitResult)
    if(lastProgress == null || progress.getProgress > lastProgress.getProgress) {
      lastProgress = progress
      progressListeners.foreach(_.onProgressUpdate(lastProgress.getProgress, lastProgress.getProgressInfo))
    }
    super.getJobInfoResult
  }
}

class SubmittableInteractiveJob(ujesClient: UJESClient,
                                jobSubmitAction: JobSubmitAction)
  extends StorableSubmittableLinkisJob(ujesClient, jobSubmitAction) with InteractiveJob

class ExistingInteractiveJob(ujesClient: UJESClient,
                             execId: String,
                             taskId: String, user: String)
  extends StorableExistingLinkisJob(ujesClient, execId, taskId, user) with InteractiveJob

object InteractiveJob {

  def builder(): InteractiveJobBuilder = new InteractiveJobBuilder

  /**
    * When use this method to create a InteractiveJob, ProgressListener and LogListener cannot be used, because execID is not exists.
    * @param taskId the id of InteractiveJob
    * @param user the execute user of InteractiveJob
    * @return
    */
  def build(taskId: String, user: String): InteractiveJob = new ExistingInteractiveJob(LinkisJobBuilder.getDefaultUJESClient, null, taskId, user)

  /**
    * Use this method to create a InteractiveJob, it is equivalent with [[builder]].
    * @param execId the execId of InteractiveJob
    * @param taskId the taskId of InteractiveJob
    * @param user the execute user of InteractiveJob
    * @return
    */
  def build(execId: String, taskId: String, user: String): InteractiveJob = new ExistingInteractiveJob(LinkisJobBuilder.getDefaultUJESClient, null, taskId, user)
}