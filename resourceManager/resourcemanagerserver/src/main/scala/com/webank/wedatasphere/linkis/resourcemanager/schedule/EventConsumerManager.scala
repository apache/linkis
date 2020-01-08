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

import java.util.concurrent.ExecutorService

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.resourcemanager.event.RMEvent
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.listener.ConsumerListener
import com.webank.wedatasphere.linkis.scheduler.queue.{ConsumerManager, LoopArrayQueue}

import scala.collection.mutable

/**
  * Created by shanhuang on 9/11/18.
  */

trait RMEventExecuteRequest {
  val event: RMEvent
}

class EventConsumerManager(schedulerContext: SchedulerContext,
                           val maxParallelismUsers: Int) extends ConsumerManager() with Logging {
  private val RM_CONTEXT_CONSTRUCTOR_LOCK = new Object()
  private val consumerGroupMap = new mutable.HashMap[String, RMEventConsumer]()
  private val consumerListenerMap = new mutable.HashMap[String, RMConsumerListener]()

  private var consumerListener: ConsumerListener = _

  private var executorService: ExecutorService = _


  private val monitorThread = new Thread("monitor-thread-EventConsumerManager") {
    setDaemon(true)

    override def run(): Unit = {
      info("Monitor consumer thread is running")
      while (true) {
        Utils.tryAndWarn(checkAllConsumerHealthy())
        Utils.tryQuietly(Thread.sleep(5000))
      }
    }
  }

  monitorThread.start()

  def checkAllConsumerHealthy(): Unit = {
    consumerListenerMap.foreach(x => {
      if (!x._2.checkConsumerHealthy(10000)) {
        val oldConsumer = consumerGroupMap.get(x._1).getOrElse(null);
        val newConsumer = createConsumerFromConsumer(oldConsumer)
        consumerGroupMap.update(x._1, newConsumer)
        if (oldConsumer != null) oldConsumer.shutdown()
      }
    })
  }

  override def setConsumerListener(consumerListener: ConsumerListener) = {
    this.consumerListener = consumerListener
  }

  //override def createExecutorService =

  /*override def getEvent(eventId: String) = {
    val eventList = listConsumers.map(x => {
      x.getConsumeQueue.get(eventId)
    }).filter(x => x.isDefined).toList
    if (eventList.size > 0) eventList(0) else None
  }*/

  override def getOrCreateConsumer(groupName: String) = {
    RM_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      var tmpConsumer = consumerGroupMap.get(groupName).getOrElse(null)
      if (tmpConsumer == null) {
        tmpConsumer = createConsumer(groupName)
      }
      tmpConsumer
    }
  }

  override protected def createConsumer(groupName: String) = {
    val group = schedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
    val consumer = new RMEventConsumer(schedulerContext, getOrCreateExecutorService, group)
    consumer.start()
    val listener = new RMConsumerListenerImpl
    listener.setConsumer(consumer)
    consumer.setConsumeQueue(new LoopArrayQueue(group))
    consumer.setRmConsumerListener(listener)
    consumerGroupMap.put(groupName, consumer)
    consumerListenerMap.put(groupName, listener)
    if (consumerListener != null) consumerListener.onConsumerCreated(consumer)

    consumer
  }

  protected def createConsumerFromConsumer(oldConsumer: RMEventConsumer) = {
    var newConsumer: RMEventConsumer = null
    if (oldConsumer != null) {
      info("Create new consumer from old consumer " + oldConsumer.getGroup.getGroupName)
      val groupName = oldConsumer.getGroup.getGroupName
      val group = schedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
      newConsumer = new RMEventConsumer(schedulerContext, getOrCreateExecutorService, group)
      newConsumer.start()
      val listener = new RMConsumerListenerImpl
      listener.setConsumer(newConsumer)
      newConsumer.setConsumeQueue(oldConsumer.getConsumeQueue)
      newConsumer.setRmConsumerListener(listener)
      consumerListenerMap.update(groupName, listener)
      if (consumerListener != null) consumerListener.onConsumerCreated(newConsumer)

    }

    newConsumer
  }

  override def destroyConsumer(groupName: String) = {
    val tmpConsumer = consumerGroupMap.get(groupName).getOrElse(null)
    if (tmpConsumer != null) {
      tmpConsumer.shutdown()
      consumerGroupMap.remove(groupName)
      if (consumerListener != null) consumerListener.onConsumerDestroyed(tmpConsumer)
    }
  }

  override def shutdown() = {
    Utils.tryThrow({
      consumerGroupMap.values.toArray.foreach(x => x.shutdown())
      executorService.shutdown()
    })(t => new Exception("ConsumerManager shutdown exception", t))
  }

  override def listConsumers() = consumerGroupMap.values.toArray

  override def getOrCreateExecutorService: ExecutorService = {
    RM_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (executorService == null) {
        executorService = Utils.newCachedThreadPool(3 * maxParallelismUsers + 1, "Engine-Scheduler-ThreadPool-", true)
        executorService
      } else {
        executorService
      }
    }
  }
}
