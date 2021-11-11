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
 
package org.apache.linkis.scheduler.queue.fifoqueue



import java.util.concurrent.{ExecutorService, Future}

import org.apache.linkis.common.exception.{ErrorException, WarnException}
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.scheduler.SchedulerContext
import org.apache.linkis.scheduler.exception.SchedulerErrorException
import org.apache.linkis.scheduler.executer.Executor
import org.apache.linkis.scheduler.future.{BDPFuture, BDPFutureTask}
import org.apache.linkis.scheduler.queue._

import scala.beans.BeanProperty
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.TimeoutException

class FIFOUserConsumer(schedulerContext: SchedulerContext,
                       executeService: ExecutorService, private var group: Group) extends Consumer(schedulerContext, executeService) {
  private var fifoGroup = group.asInstanceOf[FIFOGroup]
  private var queue: ConsumeQueue = _
  private val maxRunningJobsNum = fifoGroup.getMaxRunningJobs
  private val runningJobs = new Array[Job](maxRunningJobsNum)
  private var future: Future[_] = _

  private var bdpFutureTask: BDPFuture = _

  @BeanProperty
  var lastTime: Long = _

  def this(schedulerContext: SchedulerContext,executeService: ExecutorService) = {
    this(schedulerContext,executeService, null)
  }

  def start(): Unit = {
    future = executeService.submit(this)
    bdpFutureTask = new BDPFutureTask(this.future)
  }

  override def setConsumeQueue(consumeQueue: ConsumeQueue) = {
    queue = consumeQueue
  }

  override def getConsumeQueue = queue

  override def getGroup = fifoGroup

  override def setGroup(group: Group) = {
    this.fifoGroup = group.asInstanceOf[FIFOGroup]
  }

  override def getRunningEvents = getEvents(e => e.isRunning || e.isWaitForRetry)

  private def getEvents(op: SchedulerEvent => Boolean): Array[SchedulerEvent] = {
    val result = ArrayBuffer[SchedulerEvent]()
    runningJobs.filter(_ != null).filter(x => op(x)).foreach(result += _)
    result.toArray
  }

  override def run() = {
    Thread.currentThread().setName(s"${toString}Thread")
    info(s"$toString thread started!")
    while (!terminate) {
      Utils.tryAndError(loop())
      Utils.tryAndError(Thread.sleep(10))
    }
    info(s"$toString thread stopped!")
  }

  protected def askExecutorGap(): Unit = {}

  protected def loop(): Unit = {
    var isRetryJob = false
    def getWaitForRetryEvent: Option[SchedulerEvent] = {
      val waitForRetryJobs = runningJobs.filter(job => job != null && job.isJobCanRetry)
      waitForRetryJobs.find{job =>
        isRetryJob = Utils.tryCatch(job.turnToRetry()){ t =>
          job.onFailure("Job state flipped to Scheduled failed in Retry(Retry时，job状态翻转为Scheduled失败)！", t)
          false
        }
        isRetryJob
      }
    }
    var event: Option[SchedulerEvent] = getWaitForRetryEvent
    if(event.isEmpty) {
      val completedNums = runningJobs.filter(e => e == null || e.isCompleted)
      if (completedNums.length < 1) {
        Utils.tryQuietly(Thread.sleep(1000))  //TODO 还可以优化，通过实现JobListener进行优化
        return
      }
      while(event.isEmpty) {
        val takeEvent = if(getRunningEvents.isEmpty) Option(queue.take()) else queue.take(3000)
        event = if(takeEvent.exists(e => Utils.tryCatch(e.turnToScheduled()) {t =>
          takeEvent.get.asInstanceOf[Job].onFailure("Job状态翻转为Scheduled失败！", t)
          false
        })) takeEvent else getWaitForRetryEvent
      }
    }
    event.foreach { case job: Job =>
      Utils.tryCatch {
        val (totalDuration, askDuration) = (fifoGroup.getMaxAskExecutorDuration, fifoGroup.getAskExecutorInterval)
        var executor: Option[Executor] = None
        job.consumerFuture = bdpFutureTask
        Utils.waitUntil(() => {
          executor = Utils.tryCatch(schedulerContext.getOrCreateExecutorManager.askExecutor(job, askDuration)) {
            case warn: WarnException =>
              job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateWarn(warn.getDesc)))
              None
            case e: ErrorException =>
              job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateERROR(e.getMessage)))
              throw e
            case error: Throwable =>
              job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateERROR(error.getMessage)))
              throw error
          }
          Utils.tryQuietly(askExecutorGap())
          executor.isDefined
        }, totalDuration)
        job.consumerFuture = null
        executor.foreach { executor =>
          job.setExecutor(executor)
          job.future = executeService.submit(job)
          job.getJobDaemon.foreach(jobDaemon => jobDaemon.future = executeService.submit(jobDaemon))
          if(!isRetryJob) putToRunningJobs(job)
        }
      }{
        case _: TimeoutException =>
          warn(s"Ask executor for Job $job timeout!")
          job.onFailure("The request engine times out (请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以去查看看EngineConnManager的linkis.out和linkis.log日志).",
            new SchedulerErrorException(11055, "The request engine times out (请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以去观看EngineConnManager的linkis.out和linkis.log日志)."))
        case error: Throwable =>
          job.onFailure("请求引擎失败，可能是由于后台进程错误!请联系管理员", error)
          if(job.isWaitForRetry) {
            warn(s"Ask executor for Job $job failed, wait for the next retry!", error)
            if(!isRetryJob)  putToRunningJobs(job)
          } else warn(s"Ask executor for Job $job failed!", error)
      }
    }
  }

  private def putToRunningJobs(job: Job): Unit = {
    val index = runningJobs.indexWhere(f => f == null || f.isCompleted)
    runningJobs(index) = job
  }

  override def shutdown() = {
    future.cancel(true)
    super.shutdown()
  }

  /**
    * Determine whether the consumer is idle, by determining whether the queue and running job are empty
    * @return
    */
  def isIdle: Boolean = {
    info(s"${getGroup.getGroupName} queue isEmpty:${queue.isEmpty},size ${queue.size}")
    info(s"${getGroup.getGroupName} running jobs is not empty:${this.runningJobs.exists(job => job !=null && ! job.isCompleted)}")
    this.queue.peek.isEmpty && ! this.runningJobs.exists(job => job !=null && ! job.isCompleted)
  }
}