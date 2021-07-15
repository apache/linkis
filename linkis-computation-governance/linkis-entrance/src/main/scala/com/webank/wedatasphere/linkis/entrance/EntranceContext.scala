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

package com.webank.wedatasphere.linkis.entrance

import com.webank.wedatasphere.linkis.entrance.event._
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.entrance.log.LogManager
import com.webank.wedatasphere.linkis.entrance.persistence.PersistenceManager
import com.webank.wedatasphere.linkis.scheduler.Scheduler


abstract class EntranceContext {

  def getOrCreateScheduler(): Scheduler

  def getOrCreateEntranceParser(): EntranceParser

  def getOrCreateEntranceInterceptors(): Array[EntranceInterceptor]

  def getOrCreateLogManager(): LogManager

  def getOrCreatePersistenceManager(): PersistenceManager

  /**
    * Please note: it can be empty(请注意：可以为空)
    *
    * @return
    */
  def getOrCreateEventListenerBus: EntranceEventListenerBus[EntranceEventListener, EntranceEvent]

  def getOrCreateLogListenerBus: EntranceLogListenerBus[EntranceLogListener, EntranceLogEvent]

}