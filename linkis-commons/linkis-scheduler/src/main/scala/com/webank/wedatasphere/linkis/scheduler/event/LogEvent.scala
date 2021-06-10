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

/**
  * author: enjoyyin
  * date: 2018/9/5
  * time: 17:29
  * Description:
  */
package com.webank.wedatasphere.linkis.scheduler.event

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.scheduler.queue.Job


class LogEvent(source:Job,
               t:Int) extends Event{
  def getT:Int = t
}

object LogEvent{
  val read:Int = 1
  val write:Int = 2

  def apply(source: Job,
            t: Int): LogEvent = new LogEvent(source, t)
}