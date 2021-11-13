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
 
package org.apache.linkis.entranceclient.context

import java.lang

import org.apache.linkis.entrance.persistence.PersistenceEngine
import org.apache.linkis.protocol.task.Task

class ClientPersistenceEngine extends PersistenceEngine {
  override def persist(task: Task): Unit = {}

  /**
    * If a task's progress, status, logs, and result set are updated, this method is updated <br>
    * 如果一个任务的进度、状态、日志和结果集发生更新，由此方法进行更新<br>
    *
    * @param task
    */
  override def updateIfNeeded(task: Task): Unit = {}

  /**
    * Used to hang up a unified import task through this method, and continue to do the processing.
    * 用于如果某一个统一入口挂掉了，通过这个方法，将挂掉的统一入口tasks读取过来，继续做处理
    *
    * @param instance
    * @return
    */
override def readAll(instance: String): Array[Task] = Array.empty

  /**
    * Return a task information in the database through a taskID, such as query log storage address, etc.
    * 通过一个taskID，返回数据库中的一条task信息，这种情况包括如 查询日志存放地址等
    *
    * @param taskID
    * @return
    */
  override def retrieve(taskID: lang.Long): Task = null

  override def close(): Unit = {}

  override def flush(): Unit = {}
}
