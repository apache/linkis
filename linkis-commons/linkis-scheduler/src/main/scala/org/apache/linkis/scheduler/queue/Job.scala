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

package org.apache.linkis.scheduler.queue

import org.apache.linkis.common.exception.{ErrorException, LinkisRetryException}
import org.apache.linkis.common.listener.ListenerEventBus
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.event._
import org.apache.linkis.scheduler.exception.LinkisJobRetryException
import org.apache.linkis.scheduler.executer._
import org.apache.linkis.scheduler.future.BDPFuture
import org.apache.linkis.scheduler.listener._

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.io.Closeable
import java.util.concurrent.Future

abstract class Job extends Runnable with SchedulerEvent with Closeable with Logging {

  import SchedulerEventState._
  private[linkis] var future: Future[_] = _

  /**
   * the future of consumer
   */
  private[queue] var consumerFuture: BDPFuture = _

  protected var jobDaemon: Option[JobDaemon] = None
  private var eventListenerBus: ListenerEventBus[_ <: SchedulerListener, _ <: ScheduleEvent] = _
  private var executor: Executor = _
  private var jobListener: Option[JobListener] = None
  private var logListener: Option[LogListener] = None
  private var progressListener: Option[ProgressListener] = None
  private[linkis] var interrupt = false
  private var progress: Float = 0f
  private var retryNum = 0
  private[linkis] var errorExecuteResponse: ErrorExecuteResponse = _

  override def isWaiting: Boolean = super.isWaiting && !interrupt

  override def isCompleted: Boolean = super.isCompleted || interrupt

  def kill(): Unit = onFailure("Job is killed by user!", null)

  private[queue] def getJobDaemon = if (!existsJobDaemon) None
  else {
    if (jobDaemon.isEmpty) synchronized {
      if (jobDaemon.isEmpty) jobDaemon = Some(createJobDaemon)
    }
    jobDaemon
  }

  override def cancel(): Unit = kill()

  override def getId(): String = super.getId

  override def pause(): Unit = if (executor != null) executor match {
    case s: SingleTaskOperateSupport => s.pause()
    case c: ConcurrentTaskOperateSupport => c.pause(getId)
    case _ =>
  }

  override def resume(): Unit = if (executor != null) executor match {
    case s: SingleTaskOperateSupport => s.resume()
    case c: ConcurrentTaskOperateSupport => c.resume(getId)
    case _ =>
  }

  private def killByExecutor(): Unit = if (executor != null) executor match {
    case s: SingleTaskOperateSupport => s.kill()
    case c: ConcurrentTaskOperateSupport => c.kill(getId)
    case _ =>
  }

  def onFailure(errorMsg: String, t: Throwable): Unit =
    if (!SchedulerEventState.isCompleted(getState)) {
      logger.info(s"job $toString is onFailure on state $getState with errorMsg: $errorMsg.")
      Utils.tryAndWarn {
        logListener.foreach(_.onLogUpdate(this, LogUtils.generateERROR(errorMsg)))
        if (t != null) {
          logListener.foreach(
            _.onLogUpdate(this, LogUtils.generateERROR(ExceptionUtils.getStackTrace(t)))
          )
        }
      }
      errorExecuteResponse = ErrorExecuteResponse(errorMsg, t)
      jobDaemon.foreach(_.kill())
      interrupt = true
      if (future != null && !SchedulerEventState.isCompleted(getState)) {
        Utils.tryCatch(killByExecutor()) { t: Throwable =>
          logger.error(s"kill job $getName failed", t)
          val s = new ErrorException(23333, s"kill job $getName failed")
          s.initCause(t)
          forceCancel(s)
        }
        // Utils.tryAndWarnMsg(killByExecutor())(s"kill job $toString failed!")
        future.cancel(true)
      }
      if (consumerFuture != null && executor == null) {
        logger.warn(
          s"This executor of job($toString) in starting status,When kill job need to interrupter consumer Future"
        )
        this.consumerFuture.cancel()
        this.consumerFuture = null
      }
      if (super.isWaiting || super.isScheduled) transitionCompleted(errorExecuteResponse)
      logger.info(s"$toString execute failed. Reason: $errorMsg.", t)
    }

  /**
   * After some jobs call kill, they cannot be killed correctly, causing the state to not be
   * flipped.（一些job调用kill之后，不能被正确kill，导致状态不能翻转）
   */
  protected def forceCancel(t: Throwable): Unit = {
    logger.info(s"force to cancel job $getName")
    val executeCompleted = ErrorExecuteResponse("force to transition Failed", t)
    transitionCompleted(executeCompleted)
  }

  def setListenerEventBus(
      eventListenerBus: ListenerEventBus[_ <: SchedulerListener, _ <: ScheduleEvent]
  ): Unit = this.eventListenerBus = eventListenerBus

  def setExecutor(executor: Executor): Unit = this.executor = executor
  protected def getExecutor = executor

  def setJobListener(jobListener: JobListener): Unit = this.jobListener = Some(jobListener)

  def getJobListener: Option[JobListener] = jobListener

  def setLogListener(logListener: LogListener): Unit = this.logListener = Some(logListener)

  def getLogListener: Option[LogListener] = logListener

  def setProgressListener(progressListener: ProgressListener): Unit = this.progressListener = Some(
    progressListener
  )

  def getProgressListener: Option[ProgressListener] = progressListener

  def getProgress: Float = progress

  def setProgress(progress: Float): Unit = this.progress = progress

  @throws[Exception]
  def init(): Unit

  @throws[Exception]
  protected def jobToExecuteRequest: ExecuteRequest

  def getName: String

  def getJobInfo: JobInfo

  def getErrorResponse: ErrorExecuteResponse = errorExecuteResponse

  protected def existsJobDaemon: Boolean = false

  protected def createJobDaemon: JobDaemon =
    new JobDaemon(this, jobDaemonUpdateInterval, executor)

  protected def jobDaemonUpdateInterval: Long = 1000L

  override def beforeStateChanged(
      fromState: SchedulerEventState,
      toState: SchedulerEventState
  ): Unit = toState match {
    case Succeed | Failed | Cancelled | Timeout =>
      jobListener.foreach(_.onJobCompleted(this))
    case _ =>
  }

  override def afterStateChanged(
      fromState: SchedulerEventState,
      toState: SchedulerEventState
  ): Unit = toState match {
    case Inited =>
      jobListener.foreach(_.onJobInited(this))
    // TODO Add event（加事件）
    case Scheduled =>
      jobListener.foreach(_.onJobScheduled(this))
      logListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo("job is scheduled.")))
    // TODO Add event（加事件）
    case Running =>
      jobListener.foreach(_.onJobRunning(this))
      logListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo("job is running.")))
    // TODO job start event
    case WaitForRetry =>
      jobListener.foreach(_.onJobWaitForRetry(this))
    case _ =>
      jobDaemon.foreach(_.kill())
      jobListener.foreach(_.onJobCompleted(this))
//      if(getJobInfo != null) logListener.foreach(_.onLogUpdate(this, getJobInfo.getMetric))
      logListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo("job is completed.")))
    // TODO job end event
  }

  protected def transitionCompleted(executeCompleted: CompletedExecuteResponse): Unit = {
    val state = getState
    executeCompleted match {
      case _: SuccessExecuteResponse =>
        if (!interrupt) {
          Utils.tryAndWarnMsg(transition(Succeed))(
            s"update Job $toString from $state to Succeed failed."
          )
        } else transitionCompleted(errorExecuteResponse)
      case e: ErrorExecuteResponse =>
        val canRetry = Utils.tryCatch(isJobShouldRetry(e)) { t =>
          logger.error(s"Job $toString failed to get the retry information!", t)
          Utils.tryAndWarn(
            logListener.foreach(
              _.onLogUpdate(
                this,
                LogUtils.generateERROR(
                  "failed to get the retry information! " + ExceptionUtils.getStackTrace(t)
                )
              )
            )
          )
          if (e.t == null) errorExecuteResponse = ErrorExecuteResponse(e.message, t)
          false
        }
        if (canRetry) {
          progress = 0
          Utils.tryAndWarnMsg(transition(WaitForRetry))(
            s"update Job $toString from $state to WaitForRetry failed."
          )
          return
        } else {
          errorExecuteResponse = e
          Utils.tryAndWarnMsg(
            transition(if (interrupt && errorExecuteResponse.t == null) Cancelled else Failed)
          )(s"update Job $toString from $state to Failed failed.")
        }
    }
    endTime = System.currentTimeMillis
    IOUtils.closeQuietly(this)
  }

  def isJobSupportRetry: Boolean = true
  def getRetryNum: Int = retryNum
  protected def getMaxRetryNum: Int = 2

  protected def isJobShouldRetry(errorExecuteResponse: ErrorExecuteResponse): Boolean =
    isJobSupportRetry && errorExecuteResponse != null && (errorExecuteResponse.t match {
      case t: LinkisRetryException =>
        logger.warn(s"Job $toString is desired to retry.", t)
        t.getErrCode == LinkisJobRetryException.JOB_RETRY_ERROR_CODE
      case _ => false
    })

  final def isJobCanRetry: Boolean = if (!isJobSupportRetry || getState != WaitForRetry) {
    false
  } else {
    synchronized {
      if (getState == WaitForRetry && (getMaxRetryNum < 1 || retryNum < getMaxRetryNum)) true
      else if (WaitForRetry == getState && getMaxRetryNum > 0 && retryNum >= getMaxRetryNum) {
        logListener.foreach(
          _.onLogUpdate(
            this,
            LogUtils.generateInfo(s"Job cancelled since reached maxRetryNum $getMaxRetryNum.")
          )
        )
        transition(Failed)
        false
      } else false
    }
  }

  final def turnToRetry(): Boolean = if (!isJobSupportRetry || getState != WaitForRetry) {
    false
  } else {
    synchronized(Utils.tryThrow {
      if (isJobCanRetry) {
        transition(Scheduled)
        retryNum += 1
        true
      } else false
    } { t =>
      retryNum += 1
      t
    })
  }

  override def run(): Unit = {
    if (!isScheduled || interrupt) return
    startTime = System.currentTimeMillis
    Utils.tryAndWarn(transition(Running))
    if (interrupt) {
      endTime = System.currentTimeMillis
      transition(Cancelled)
      close()
      return
    }
    val rs = Utils.tryCatch(executor.execute(jobToExecuteRequest)) {
      case t: InterruptedException =>
        logger.warn(s"job $toString is interrupted by user!", t)
        ErrorExecuteResponse("job is interrupted by user!", t)
      case t =>
        logger.warn(s"execute job $toString failed!", t)
        ErrorExecuteResponse("execute job failed!", t)
    }
    rs match {
      case r: CompletedExecuteResponse =>
        transitionCompleted(r)
      case r: IncompleteExecuteResponse =>
        transitionCompleted(
          ErrorExecuteResponse(
            if (StringUtils.isNotEmpty(r.message)) r.message else "incomplete code.",
            null
          )
        )
      case r: AsynReturnExecuteResponse =>
        r.notify(r1 => {
          val realRS =
            if (interrupt) {
              errorExecuteResponse
            } else {
              r1 match {
                case r: IncompleteExecuteResponse =>
                  ErrorExecuteResponse(
                    if (StringUtils.isNotEmpty(r.message)) r.message else "incomplete code.",
                    null
                  )
                case r: CompletedExecuteResponse => r
              }
            }
          transitionCompleted(realRS)
        })
    }
  }

  override def toString: String = if (StringUtils.isNotBlank(getName)) getName else getId
}

/**
 * Mainly used to get the status and log. If the Executor can't directly notify the ProgressListener
 * and LogListener, then the Consumer is submitting a Job. A JobDaemon must be submitted at the same
 * time to ensure that progress and logs are notified in a timely manner.
 * 主要用于获取状态和日志，如果Executor做不到直接通知ProgressListener和LogListener，则Consumer在提交一个Job时，
 * 必须同时提交一个JobDaemon，确保进度和日志能及时通知出去
 * @param job
 *   Job to be monitored（需要监听的Job）
 * @param listenerUpdateIntervalMs
 *   Interval of listening（监听的间隔）
 * @param executor
 *   Corresponding actuator（对应的执行器）
 */
class JobDaemon(job: Job, listenerUpdateIntervalMs: Long, executor: Executor)
    extends Runnable
    with Logging {
  private var terminate = false
  private[queue] var future: Future[_] = _
  private var lastProgress = 0f

  protected def getProgress: (Float, Array[JobProgressInfo]) = executor match {
    case s: SingleTaskInfoSupport => (s.progress(), s.getProgressInfo)
    case c: ConcurrentTaskInfoSupport => (c.progress(job.getId), c.getProgressInfo(job.getId))
    case _ => (0, null)
  }

  protected def getLog: String = executor match {
    case s: SingleTaskInfoSupport => s.log()
    case c: ConcurrentTaskInfoSupport => c.log(job.getId)
    case _ => ""
  }

  override def run(): Unit = {
    if (listenerUpdateIntervalMs < 10) return
    executor match {
      case _: SingleTaskInfoSupport =>
      case _: ConcurrentTaskInfoSupport =>
      case _ => return
    }
    while (!SchedulerEventState.isCompleted(job.getState) && !terminate) {
      val (progress, progressInfo) = Utils.tryAndWarnMsg(getProgress)(
        s"Can not get progress information from $executor for job $job."
      )
      if (progress != lastProgress) {
        job.setProgress(progress)
        Utils.tryAndWarnMsg(
          job.getProgressListener.foreach(_.onProgressUpdate(job, progress, progressInfo))
        )(s"Can not update progress for job $job.")
        lastProgress = progress
      }
      val log = Utils.tryAndWarnMsg(getLog)(s"Can not get logs from $executor for job $job.")
      if (StringUtils.isNotEmpty(log)) {
        Utils.tryAndWarnMsg(job.getLogListener.foreach(_.onLogUpdate(job, log)))(
          s"Can not update logs for job $job."
        )
      }
      Utils.tryQuietly(Thread.sleep(listenerUpdateIntervalMs))
    }
  }

  def kill(): AnyVal = {
    terminate = true
    if (future != null && !future.isDone) future.cancel(true)
  }

}
