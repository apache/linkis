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

package com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext

/**
  * Created by enjoyyin on 2018/9/26.
  */
class FIFOSchedulerContextImpl(val maxParallelismUsers: Int) extends  SchedulerContext with Logging{
  private var consumerManager: FIFOConsumerManager = _
  private var groupFactory: FIFOGroupFactory = _
  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  override def getOrCreateGroupFactory = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (groupFactory == null) {
        groupFactory = new FIFOGroupFactory()
      }
      groupFactory
    }
  }

  override def getOrCreateConsumerManager = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (consumerManager == null) {
        consumerManager = new FIFOConsumerManager
        consumerManager.setSchedulerContext(this)
      }
      consumerManager
    }
  }

  override def getOrCreateExecutorManager = null

  override def getOrCreateSchedulerListenerBus = null
}
