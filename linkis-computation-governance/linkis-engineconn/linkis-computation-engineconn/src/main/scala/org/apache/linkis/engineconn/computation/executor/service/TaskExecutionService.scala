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

package org.apache.linkis.engineconn.computation.executor.service

import org.apache.linkis.governance.common.protocol.task._
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.ExecuteResponse

trait TaskExecutionService {

  def execute(requestTask: RequestTask, sender: Sender): ExecuteResponse

//  def taskStatus(taskID: String): ResponseTaskStatus

  def taskProgress(taskID: String): ResponseTaskProgress

  def taskLog(taskID: String): ResponseTaskLog

  def killTask(taskID: String): Unit

  /* def pauseTask(taskID: String): Unit

  def resumeTask(taskID: String): Unit */

  def dealRequestTaskStatus(requestTaskStatus: RequestTaskStatus): ResponseTaskStatus

  def dealRequestTaskPause(requestTaskPause: RequestTaskPause): Unit

  def dealRequestTaskKill(requestTaskKill: RequestTaskKill): Unit

  def dealRequestTaskResume(requestTaskResume: RequestTaskResume): Unit

  def clearCache(taskId: String): Unit

}
