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

package org.apache.linkis.scheduler.queue.fifoqueue

import org.apache.linkis.common.exception.{ErrorException, WarnException}
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.scheduler.SchedulerContext
import org.apache.linkis.scheduler.errorcode.LinkisSchedulerErrorCodeSummary._
import org.apache.linkis.scheduler.exception.SchedulerErrorException
import org.apache.linkis.scheduler.executer.Executor
import org.apache.linkis.scheduler.future.{BDPFuture, BDPFutureTask}
import org.apache.linkis.scheduler.queue._

import java.util
import java.util.concurrent.{ExecutorService, Future}

import scala.beans.BeanProperty
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.TimeoutException

class FIFOUserConsumer(
    schedulerContext: SchedulerContext,
    executeService: ExecutorService,
    private var group: Group
) extends Consumer(schedulerContext, executeService) {
  private var fifoGroup = group.asInstanceOf[FIFOGroup]
  private var queue: ConsumeQueue = _
  private val maxRunningJobsNum = fifoGroup.getMaxRunningJobs
  private val runningJobs = new Array[Job](maxRunningJobsNum)
  private var future: Future[_] = _

  private var bdpFutureTask: BDPFuture = _

  @BeanProperty
  var lastTime: Long = _

  def this(schedulerContext: SchedulerContext, executeService: ExecutorService) = {
    this(schedulerContext, executeService, null)
  }

  def start(): Unit = {
    future = executeService.submit(this)
    bdpFutureTask = new BDPFutureTask(this.future)
  }

  override def setConsumeQueue(consumeQueue: ConsumeQueue): Unit = {
    queue = consumeQueue
  }

  override def getConsumeQueue: ConsumeQueue = queue

  override def getGroup: FIFOGroup = fifoGroup

  override def setGroup(group: Group): Unit = {
    this.fifoGroup = group.asInstanceOf[FIFOGroup]
  }

  override def getRunningEvents: Array[SchedulerEvent] =
    getEvents(e => e.isRunning || e.isWaitForRetry)

  protected def getSchedulerContext: SchedulerContext = schedulerContext

  private def getEvents(op: SchedulerEvent => Boolean): Array[SchedulerEvent] = {
    val result = ArrayBuffer[SchedulerEvent]()
    runningJobs.filter(_ != null).filter(x => op(x)).foreach(result += _)
    result.toArray
  }

  override def run(): Unit = {
    Thread.currentThread().setName(s"${toString}Thread")
    logger.info(s"$toString thread started!")
    while (!terminate) Utils.tryAndError {
      loop()
      Thread.sleep(10)
    }
    logger.info(s"$toString thread stopped!")
  }

  protected def askExecutorGap(): Unit = {}

  /**
   * Task scheduling interception is used to judge the rules of task operation, and to judge other
   * task rules based on Group. For example, Entrance makes Creator-level task judgment.
   */
  protected def runScheduleIntercept(): Boolean = {
    true
  }

  protected def loop(): Unit = {
    if (!runScheduleIntercept()) {
      Utils.tryQuietly(Thread.sleep(1000))
      return
    }
    var isRetryJob = false
    def getWaitForRetryEvent: Option[SchedulerEvent] = {
      val waitForRetryJobs = runningJobs.filter(job => job != null && job.isJobCanRetry)
      waitForRetryJobs.find { job =>
        isRetryJob = Utils.tryCatch(job.turnToRetry()) { t =>
          job.onFailure(
            "Job state flipped to Scheduled failed in Retry(Retry时，job状态翻转为Scheduled失败)！",
            t
          )
          false
        }
        isRetryJob
      }
    }
    var event: Option[SchedulerEvent] = getWaitForRetryEvent
    if (event.isEmpty) {
      val maxAllowRunningJobs = fifoGroup.getMaxAllowRunningJobs
      val currentRunningJobs = runningJobs.count(e => e != null && !e.isCompleted)
      if (maxAllowRunningJobs <= currentRunningJobs) {
        Utils.tryQuietly(Thread.sleep(1000)) // TODO 还可以优化，通过实现JobListener进行优化
        return
      }
      while (event.isEmpty) {
        val takeEvent = if (getRunningEvents.isEmpty) Option(queue.take()) else queue.take(3000)
        event =
          if (
              takeEvent.exists(e =>
                Utils.tryCatch(e.turnToScheduled()) { t =>
                  takeEvent.get
                    .asInstanceOf[Job]
                    .onFailure(
                      "Failed to change the job status to Scheduled(Job状态翻转为Scheduled失败)",
                      t
                    )
                  false
                }
              )
          ) {
            takeEvent
          } else getWaitForRetryEvent
      }
    }
    event.foreach { case job: Job =>
      Utils.tryCatch {
        val (totalDuration, askDuration) =
          (fifoGroup.getMaxAskExecutorDuration, fifoGroup.getAskExecutorInterval)
        var executor: Option[Executor] = None
        job.consumerFuture = bdpFutureTask
        Utils.waitUntil(
          () => {
            executor = Utils.tryCatch(
              schedulerContext.getOrCreateExecutorManager.askExecutor(job, askDuration)
            ) {
              case warn: WarnException =>
                job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateWarn(warn.getDesc)))
                None
              case e: ErrorException =>
                job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateERROR(e.getMessage)))
                throw e
              case error: Throwable =>
                job.getLogListener.foreach(
                  _.onLogUpdate(job, LogUtils.generateERROR(error.getMessage))
                )
                throw error
            }
            Utils.tryQuietly(askExecutorGap())
            executor.isDefined
          },
          totalDuration
        )
        job.consumerFuture = null
        executor.foreach { executor =>
          job.setExecutor(executor)
          job.future = executeService.submit(job)
          job.getJobDaemon.foreach(jobDaemon => jobDaemon.future = executeService.submit(jobDaemon))
          if (!isRetryJob) putToRunningJobs(job)
        }
      } {
        case _: TimeoutException =>
          logger.warn(s"Ask executor for Job $job timeout!")
          job.onFailure(
            "The request engine times out (请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以去查看看EngineConnManager的linkis.out和linkis.log日志).",
            new SchedulerErrorException(
              REQUEST_ENGINE_TIME_OUT.getErrorCode,
              REQUEST_ENGINE_TIME_OUT.getErrorDesc
            )
          )
        case error: Throwable =>
          job.onFailure("Failed to request EngineConn", error)
          if (job.isWaitForRetry) {
            logger.warn(s"Ask executor for Job $job failed, wait for the next retry!", error)
            if (!isRetryJob) putToRunningJobs(job)
          } else logger.warn(s"Ask executor for Job $job failed!", error)
      }
    }
  }

  private def putToRunningJobs(job: Job): Unit = {
    val index = runningJobs.indexWhere(f => f == null || f.isCompleted)
    runningJobs(index) = job
  }

  protected def scanAllRetryJobsAndRemove(): util.List[Job] = {
    val jobs = new util.ArrayList[Job]()
    for (index <- runningJobs.indices) {
      val job = runningJobs(index)
      if (job != null && job.isJobCanRetry) {
        jobs.add(job)
        runningJobs(index) = null
        logger.info(s"Job $job can retry, remove from runningJobs")
      }
    }
    jobs
  }

  override def shutdown(): Unit = {
    future.cancel(true)
    val waitEvents = queue.getWaitingEvents
    if (waitEvents.nonEmpty) {
      waitEvents.foreach {
        case job: Job =>
          job.onFailure("Your job will be marked as canceled because the consumer be killed", null)
        case _ =>
      }
    }

    this.runningJobs.foreach { job =>
      if (job != null && !job.isCompleted) {
        job.onFailure("Your job will be marked as canceled because the consumer be killed", null)
      }
    }
    super.shutdown()
  }

  /**
   * Determine whether the consumer is idle, by determining whether the queue and running job are
   * empty
   * @return
   */
  def isIdle: Boolean = {
    logger.info(s"${getGroup.getGroupName} queue isEmpty:${queue.isEmpty},size ${queue.size}")
    logger.info(s"${getGroup.getGroupName} running jobs is not empty:${this.runningJobs
      .exists(job => job != null && !job.isCompleted)}")
    this.queue.peek.isEmpty && !this.runningJobs.exists(job => job != null && !job.isCompleted)
  }

}
