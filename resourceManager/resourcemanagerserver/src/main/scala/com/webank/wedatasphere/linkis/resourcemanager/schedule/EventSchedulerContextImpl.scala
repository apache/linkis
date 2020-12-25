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

package com.webank.wedatasphere.linkis.resourcemanager.schedule

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory

/**
  * Created by shanhuang on 2018/9/25.
  */
class EventSchedulerContextImpl(maxParallelismUsers: Int) extends EventSchedulerContext with Logging{
  private val RM_CONTEXT_CONSTRUCTOR_LOCK = new Object()
  private var consumerManager: EventConsumerManager = _
  private var executorManager: RMEventExecutorManager = _
  private var groupFactory: GroupFactory = _

  override def getOrCreateGroupFactory = {
    RM_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (groupFactory == null) {
        groupFactory = new EventGroupFactoryImpl()
      }
    }
    groupFactory

  }

  override def getOrCreateConsumerManager = {
    RM_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (consumerManager == null) {
        consumerManager = new EventConsumerManager(this, maxParallelismUsers)
      }
      consumerManager
    }

  }

  override def getOrCreateExecutorManager = {
    RM_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (executorManager == null) {
        executorManager = new RMEventExecutorManager()
      }
      executorManager
    }
  }

  override def getOrCreateSchedulerListenerBus = null
}
