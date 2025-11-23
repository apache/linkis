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

package org.apache.linkis.entrance.persistence

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.governance.common.entity.job.{JobRequest, SubJobDetail, SubJobInfo}
import org.apache.linkis.protocol.task.Task

import java.io.{Closeable, Flushable}

trait PersistenceEngine extends Closeable with Flushable {

  /**
   * 持久化JobRequest
   * @param jobReq
   * @throws
   */
  @throws[ErrorException]
  def persist(jobReq: JobRequest): Unit

  @deprecated
  @throws[ErrorException]
  def persist(subjobInfo: SubJobInfo): Unit = {}

  /**
   * If a task's progress, status, logs, and result set are updated, this method is updated <br>
   * 如果一个任务的进度、状态、日志和结果集发生更新，由此方法进行更新<br>
   * @param jobReq
   */
  @throws[ErrorException]
  def updateIfNeeded(jobReq: JobRequest): Unit

  @deprecated
  @throws[ErrorException]
  def updateIfNeeded(subJobInfo: SubJobInfo): Unit = {}

  /**
   * Used to hang up a unified import task through this method, and continue to do the processing.
   * 用于如果某一个统一入口挂掉了，通过这个方法，将挂掉的统一入口tasks读取过来，继续做处理
   * @param instance
   * @return
   */
  @throws[ErrorException]
  def readAll(instance: String): Array[Task]

  /**
   * Return a task information in the database through a taskID, such as query log storage address,
   * etc. 通过一个taskID，返回数据库中的一条task信息，这种情况包括如 查询日志存放地址等
   * @param taskID
   * @return
   */
  @throws[ErrorException]
  def retrieveJobReq(jobGroupId: java.lang.Long): JobRequest

  @deprecated
  @throws[ErrorException]
  def retrieveJobDetailReq(jobDetailId: java.lang.Long): SubJobDetail = {
    null
  }

}
