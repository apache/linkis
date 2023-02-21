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

package org.apache.linkis.scheduler.queue.parallelqueue

import org.apache.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import org.apache.linkis.scheduler.conf.SchedulerConfiguration
import org.apache.linkis.scheduler.listener.ConsumerListener
import org.apache.linkis.scheduler.queue._
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer

import java.util.concurrent.{ExecutorService, TimeUnit}

import scala.collection.mutable

class ParallelConsumerManager(maxParallelismUsers: Int, schedulerName: String)
    extends ConsumerManager
    with Logging {

  def this(maxParallelismUsers: Int) = this(maxParallelismUsers, "DefaultScheduler")

  private val executorServiceLock = new Array[Byte](0)

  private val CONSUMER_LOCK = new Array[Byte](0)

  private var consumerListener: Option[ConsumerListener] = None

  private var executorService: ExecutorService = _

  private val consumerGroupMap = new mutable.HashMap[String, FIFOUserConsumer]()

  /**
   * Clean up idle consumers regularly
   */
  if (SchedulerConfiguration.FIFO_CONSUMER_AUTO_CLEAR_ENABLED.getValue) {
    logger.info(s"The feature that auto clean up idle consumers for $schedulerName is enabled.")
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = CONSUMER_LOCK.synchronized {
          logger.info("Start to Clean up idle consumers ")
          val nowTime = System.currentTimeMillis()
          Utils.tryAndWarn {
            consumerGroupMap.values
              .filter(_.isIdle)
              .filter(consumer =>
                nowTime - consumer.getLastTime > SchedulerConfiguration.FIFO_CONSUMER_MAX_IDLE_TIME
              )
              .foreach(consumer => destroyConsumer(consumer.getGroup.getGroupName))
          }
          logger.info(
            s"Finished to clean up idle consumers for $schedulerName, cost " +
              s"${ByteTimeUtils.msDurationToString(System.currentTimeMillis - nowTime)}."
          )
        }
      },
      SchedulerConfiguration.FIFO_CONSUMER_IDLE_SCAN_INIT_TIME.getValue.toLong,
      SchedulerConfiguration.FIFO_CONSUMER_IDLE_SCAN_INTERVAL.getValue.toLong,
      TimeUnit.MILLISECONDS
    )
  }

  override def setConsumerListener(consumerListener: ConsumerListener): Unit = {
    this.consumerListener = Some(consumerListener)
  }

  override def getOrCreateExecutorService: ExecutorService = if (executorService != null) {
    executorService
  } else {
    executorServiceLock.synchronized {
      if (executorService == null) {
        executorService = Utils.newCachedThreadPool(
          5 * maxParallelismUsers + 1,
          schedulerName + "-ThreadPool-",
          true
        )
      }
      executorService
    }
  }

  override def getOrCreateConsumer(groupName: String): Consumer = {
    val consumer =
      if (consumerGroupMap.contains(groupName)) {
        consumerGroupMap(groupName)
      } else {
        CONSUMER_LOCK.synchronized {
          if (consumerGroupMap.contains(groupName)) {
            consumerGroupMap(groupName)
          } else {
            consumerGroupMap.getOrElseUpdate(
              groupName, {
                val newConsumer = createConsumer(groupName)
                val group = getSchedulerContext.getOrCreateGroupFactory.getGroup(groupName)
                newConsumer.setGroup(group)
                newConsumer.setConsumeQueue(new LoopArrayQueue(group))
                consumerListener.foreach(_.onConsumerCreated(newConsumer))
                newConsumer.start()
                newConsumer
              }
            )
          }
        }
      }
    consumer.setLastTime(System.currentTimeMillis())
    consumer
  }

  override protected def createConsumer(groupName: String): FIFOUserConsumer = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getGroup(groupName)
    new FIFOUserConsumer(getSchedulerContext, getOrCreateExecutorService, group)
  }

  override def destroyConsumer(groupName: String): Unit =
    consumerGroupMap.get(groupName).foreach { tmpConsumer =>
      tmpConsumer.shutdown()
      consumerGroupMap.remove(groupName)
      consumerListener.foreach(_.onConsumerDestroyed(tmpConsumer))
      logger.warn(s"Consumer of group ($groupName) in $schedulerName is destroyed.")
    }

  override def shutdown(): Unit = {
    consumerGroupMap.iterator.foreach(_._2.shutdown())
  }

  override def listConsumers(): Array[Consumer] = consumerGroupMap.values.toArray
}
