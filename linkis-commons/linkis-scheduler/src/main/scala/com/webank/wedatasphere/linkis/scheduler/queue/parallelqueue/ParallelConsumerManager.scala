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

package com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue

import java.util.concurrent.{ExecutorService, TimeUnit}

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.scheduler.conf.SchedulerConfiguration
import com.webank.wedatasphere.linkis.scheduler.listener.ConsumerListener
import com.webank.wedatasphere.linkis.scheduler.queue._
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer

import scala.collection.mutable


class ParallelConsumerManager(maxParallelismUsers: Int) extends  ConsumerManager with Logging{

  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  private val CONSUMER_LOCK = new Object()

  private var consumerListener: Option[ConsumerListener] = None

  private var executorService: ExecutorService = _

  private val consumerGroupMap = new mutable.HashMap[String, FIFOUserConsumer]()

  /**
    * Clean up idle consumers regularly
    */
  if (SchedulerConfiguration.FIFO_CONSUMER_AUTO_CLEAR_ENABLED.getValue) {
    info("The feature that auto  Clean up idle consumers is enabled ")
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = CONSUMER_LOCK.synchronized {
        info("Start to Clean up idle consumers ")
        val nowTime = System.currentTimeMillis()
        consumerGroupMap.values.filter(_.isIdle)
          .filter(consumer => nowTime - consumer.getLastTime > SchedulerConfiguration.FIFO_CONSUMER_MAX_IDLE_TIME)
          .foreach(consumer => destroyConsumer(consumer.getGroup.getGroupName))
        info(s"Finished to Clean up idle consumers,cost ${System.currentTimeMillis() - nowTime} ms ")
      }
    },
      SchedulerConfiguration.FIFO_CONSUMER_IDLE_SCAN_INIT_TIME.getValue.toLong,
      SchedulerConfiguration.FIFO_CONSUMER_IDLE_SCAN_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  }

  override def setConsumerListener(consumerListener: ConsumerListener) = {
    this.consumerListener = Some(consumerListener)
  }

  override def getOrCreateExecutorService = if(executorService != null) executorService
    else UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (executorService == null) {
        executorService = Utils.newCachedThreadPool(5 * maxParallelismUsers + 1, "Engine-Scheduler-ThreadPool-", true)
      }
      executorService
  }

  override def getOrCreateConsumer(groupName: String) = CONSUMER_LOCK.synchronized {
    val consumer = if (consumerGroupMap.contains(groupName)) consumerGroupMap(groupName)
    else UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      consumerGroupMap.getOrElse(groupName, {
        val newConsumer = createConsumer(groupName)
        val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
        newConsumer.setGroup(group)
        newConsumer.setConsumeQueue(new LoopArrayQueue(group))
        consumerGroupMap.put(groupName, newConsumer)
        consumerListener.foreach(_.onConsumerCreated(newConsumer))
        newConsumer.start()
        newConsumer
      })
    }
    consumer.setLastTime(System.currentTimeMillis())
    consumer
  }

  override protected def createConsumer(groupName: String) = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
    new FIFOUserConsumer(getSchedulerContext, getOrCreateExecutorService, group)
  }

  override def destroyConsumer(groupName: String) =
    consumerGroupMap.get(groupName).foreach { tmpConsumer =>
      tmpConsumer.shutdown()
      consumerGroupMap.remove(groupName)
      consumerListener.foreach(_.onConsumerDestroyed(tmpConsumer))
      warn(s"Consumer of  group ($groupName) is destroyed")
    }

  override def shutdown() = {
    consumerGroupMap.iterator.foreach(x => x._2.shutdown())
  }

  override def listConsumers() = consumerGroupMap.values.toArray
}
