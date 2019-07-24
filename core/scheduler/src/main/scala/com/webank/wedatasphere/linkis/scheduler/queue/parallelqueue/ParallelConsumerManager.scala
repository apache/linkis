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

import java.util.concurrent.ExecutorService

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.scheduler.listener.ConsumerListener
import com.webank.wedatasphere.linkis.scheduler.queue._
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer

import scala.collection.mutable

/**
  * Created by enjoyyin on 2018/9/11.
  */
class ParallelConsumerManager(maxParallelismUsers: Int)extends  ConsumerManager{

  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()
  private var consumerListener: Option[ConsumerListener] = None

  private var executorService: ExecutorService = _

  private val consumerGroupMap = new mutable.HashMap[String, FIFOUserConsumer]()

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

  override def getOrCreateConsumer(groupName: String) = if(consumerGroupMap.contains(groupName)) consumerGroupMap(groupName)
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

  override protected def createConsumer(groupName: String) = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
    new FIFOUserConsumer(getSchedulerContext, getOrCreateExecutorService, group)
  }

  override def destroyConsumer(groupName: String) =
    consumerGroupMap.get(groupName).foreach { tmpConsumer =>
      tmpConsumer.shutdown()
      consumerGroupMap.remove(groupName)
      consumerListener.foreach(_.onConsumerDestroyed(tmpConsumer))
    }

  override def shutdown() = {
    consumerGroupMap.iterator.foreach(x => x._2.shutdown())
  }

  override def listConsumers() = consumerGroupMap.values.toArray
}
