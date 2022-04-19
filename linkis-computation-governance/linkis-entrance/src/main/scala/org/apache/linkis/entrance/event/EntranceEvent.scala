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
 
package org.apache.linkis.entrance.event

import org.apache.linkis.common.listener.Event
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.Executor
import org.apache.linkis.scheduler.queue.Job

/**
  * desctiption: entrance The type of event in the event bus in the module(模块中的事件总线中的事件类型)
  */
trait EntranceEvent extends Event{

}

//TODO Subsequent optimization, temporarily use this(后续优化，暂时这么用)
case class EntranceJobEvent(jobId: String) extends EntranceEvent

case class EntranceProgressEvent(job: Job, progress: Float, progressInfo: Array[JobProgressInfo]) extends EntranceEvent

case class MissingEngineNotifyEvent(job: Job, t: Throwable, executor: Executor) extends EntranceEvent


