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

package com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOSchedulerContextImpl
import com.webank.wedatasphere.linkis.scheduler.queue.{ConsumerManager, GroupFactory}



class ParallelSchedulerContextImpl(override val maxParallelismUsers: Int)
  extends FIFOSchedulerContextImpl(maxParallelismUsers) with Logging {

  override protected def createGroupFactory(): GroupFactory = {
    val groupFactory = new ParallelGroupFactory
    groupFactory.setParallelism(maxParallelismUsers)
    groupFactory
  }

  override protected def createConsumerManager(): ConsumerManager =
    new ParallelConsumerManager(maxParallelismUsers)

}
