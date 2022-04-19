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
 
package org.apache.linkis.orchestrator.execution.impl

import java.util
import java.util.concurrent.{ExecutorService, Future, TimeUnit}

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.exception.OrchestratorRetryException
import org.apache.linkis.orchestrator.execution.{ExecTaskRunner, TaskScheduler}
import org.apache.linkis.orchestrator.listener.OrchestratorListenerBusContext
import org.apache.linkis.orchestrator.listener.task.TaskReheaterEvent
import org.apache.linkis.orchestrator.plans.physical.ExecTask

import scala.collection.mutable.ArrayBuffer
import scala.collection.convert.wrapAsScala._

/**
  *
  *
  */
class BaseTaskScheduler(executeService: ExecutorService) extends TaskScheduler with Logging {

  private val taskFutureCache: util.Map[String, Future[_]] = new util.HashMap[String, Future[_]]()

  //private val taskIdTaskCache: util.Map[String, ExecTask] = new util.HashMap[String, ExecTask]()

  override def start(): Unit = {
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        val finishedTask = new ArrayBuffer[String]()
        val iterator = taskFutureCache.entrySet().iterator()
        while (iterator.hasNext) {
          val taskAndFuture = iterator.next()
          if (taskAndFuture.getValue.isDone || taskAndFuture.getValue.isCancelled) {
            finishedTask += taskAndFuture.getKey
          }
        }
        info(s"Clear finished task from  taskFutureCache size ${finishedTask.size}")
        finishedTask.foreach(taskFutureCache.remove(_))
      }
    }, 60000, OrchestratorConfiguration.TASK_SCHEDULER_CLEAR_TIME.getValue.toLong, TimeUnit.MILLISECONDS)
  }

  override def launchTask(task: ExecTaskRunner): Unit = {
    // TODO Should support to add task to ready queue, since a complex scheduler is needed,
    //  such as: fair, priority... we should schedule them by using some algorithms.
    // TODO Here, we should also remove the futures which is completed normally in taskFutureCache and taskIdTaskCache.
    debug(s"launch task Runner ${task.task.getIDInfo()}")
    val future = executeService.submit(task)
    if (! future.isDone) {
      taskFutureCache.put(task.task.getId, future)
      //taskIdTaskCache.put(task.task.getId, task.task)
    }
  }

  //TODO We should use this method to remove the futures in taskFutureCache,
  // when a event is sent to mark this task failed!
  override def cancelTask(task: ExecTaskRunner, interrupted: Boolean): Unit = {
    info(s"cancel task Runner ${task.task.getIDInfo}")
    task.interrupt()
    if (taskFutureCache.containsKey(task.task.getId)) {
      info(s"from taskFutureCache to kill task Runner ${task.task.getIDInfo}")
      val future = taskFutureCache.get(task.task.getId)
      if ( null != future && ! future.isDone) {
        future.cancel(interrupted)
      }
      taskFutureCache.remove(task.task.getId)
      //taskIdTaskCache.remove(task.task.getId)
    }
  }

  override def close(): Unit = {
    taskFutureCache.foreach{ case (_, future) =>
      if(future != null && !future.isDone) future.cancel(true)
    }
    taskFutureCache.clear()
   // taskIdTaskCache.clear()
    executeService.shutdownNow()
  }

}
