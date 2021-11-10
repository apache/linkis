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
 
package org.apache.linkis.orchestrator.plans.logical

import java.util

import org.apache.linkis.common.listener.Event
import org.apache.linkis.orchestrator.listener.task.{TaskLogEvent, TaskProgressEvent}

import scala.collection.mutable.ArrayBuffer

/**
  *
  *
  */
class LogicalContextImpl extends LogicalContext{

  private val jobTasks = new ArrayBuffer[JobTask]()

  private val stageTasks = new ArrayBuffer[StageTask]()
  private val context: java.util.Map[String, Any] = new util.HashMap[String, Any]()
  private var resolved = false

  override def getJobTasks: Array[JobTask] = jobTasks.toArray

  override def addJobTask(jobTask: JobTask): Unit = {
    if (! jobTasks.exists(_.getId == jobTask.getId)) {
      jobTasks += jobTask
    }
  }

  override def getStageTasks: Array[StageTask] = {
    stageTasks.toArray
  }

  override def addStageTask(stageTask: StageTask): Unit = {
    if (! stageTasks.exists(_.getId == stageTask.getId)) {
      stageTasks += stageTask
    }
  }

  override def isResolved: Boolean = resolved

  override def get(key: String): Any = {
    context.get(key)
  }

  override def getOption(key: String): Option[Any] = {
    Some(context.get(key))
  }

  override def orElse(key: String, defaultValue: Any): Option[Any] = {
    Some(getOrElse(key, defaultValue))
  }

  override def getOrElse(key: String, defaultValue: Any): Any = {
    context.getOrDefault(key, defaultValue)
  }

  override def orElsePut(key: String, defaultValue: Any): Option[Any] = {
    Some(getOrElsePut(key, defaultValue))
  }

  override def getOrElsePut(key: String, defaultValue: Any): Any = synchronized {
    if (exists(key)) {
      context.get(key)
    } else {
      context.put(key, defaultValue)
      defaultValue
    }
  }

  override def exists(key: String): Boolean = {
    context.containsKey(key)
  }

  override def set(key: String, value: Any): Unit = {
    context.put(key, value)
  }

  override def pushLog(taskLogEvent: TaskLogEvent): Unit = {}

  override def pushProgress(taskProgressEvent: TaskProgressEvent): Unit = {}

  override def broadcastAsyncEvent(event: Event): Unit = {}

  override def broadcastSyncEvent(event: Event): Unit = {}
}
