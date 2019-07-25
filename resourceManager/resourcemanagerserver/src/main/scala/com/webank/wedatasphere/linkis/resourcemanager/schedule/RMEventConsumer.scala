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

package com.webank.wedatasphere.linkis.resourcemanager.schedule

import java.util.concurrent.{ExecutorService, Future}

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.resourcemanager.event.RMEvent
import com.webank.wedatasphere.linkis.resourcemanager.event.metric.{MetricRMEvent, MetricRMEventExecutor}
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{NotifyRMEvent, NotifyRMEventExecutor}
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.queue._

import scala.collection.mutable.ArrayBuffer

/**
  * Created by shanhuang on 2018/9/18.
  */
class RMEventConsumer(schedulerContext: SchedulerContext,
                      executeService: ExecutorService) extends Consumer(schedulerContext, executeService) {
  private var queue: ConsumeQueue = _
  private var group: Group = _
  private var maxRunningJobsNum = 1000
  //Not put(暂未放)
  private val runningJobs = new Array[SchedulerEvent](maxRunningJobsNum)
  private val executorManager = schedulerContext.getOrCreateExecutorManager
  private var rmConsumerListener : RMConsumerListener = _
  var future: Future[_] = _

  def this(schedulerContext: SchedulerContext, executeService: ExecutorService, group: Group) = {
    this(schedulerContext, executeService)
    this.group = group
    maxRunningJobsNum = group.getMaximumCapacity
  }

  def start():Unit = future = executeService.submit(this)

  def setRmConsumerListener(rmConsumerListener: RMConsumerListener): Unit ={
    this.rmConsumerListener = rmConsumerListener
  }

  override def setConsumeQueue(consumeQueue: ConsumeQueue) = {
    queue = consumeQueue
  }

  override def getConsumeQueue = queue

  override def getGroup = group

  override def setGroup(group: Group) = {
    this.group = group
  }

  override def getRunningEvents = getEvents(_.isRunning)

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
      Utils.tryQuietly(Thread.sleep(10))
    }
    info(s"$toString thread stopped!")
  }

  def loop(): Unit = {
    var event = queue.take()
    while (event.turnToScheduled() != true) {
      event = queue.take()
    }
    if(rmConsumerListener != null){rmConsumerListener.beforeEventExecute(this,event.asInstanceOf[RMEvent])}
    Utils.tryAndError({
      val executor = executorManager.askExecutor(event)
      if (executor.isDefined) {
        event match {
          case x: MetricRMEvent =>{
            Utils.tryQuietly(executor.get.asInstanceOf[MetricRMEventExecutor].execute(new EventJob(x)))
          }
          case y: NotifyRMEvent =>{
            Utils.tryQuietly(executor.get.asInstanceOf[NotifyRMEventExecutor].execute(new EventJob(y)))
          }
        }
      }
    })
    if(rmConsumerListener != null){rmConsumerListener.afterEventExecute(this,event.asInstanceOf[RMEvent])}
  }

  override def shutdown() = {
    future.cancel(true)
    super.shutdown()
  }
}
