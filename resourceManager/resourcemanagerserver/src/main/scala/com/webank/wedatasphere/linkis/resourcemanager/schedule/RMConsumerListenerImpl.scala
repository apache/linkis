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
import com.webank.wedatasphere.linkis.resourcemanager.event.RMEvent

/**
  * Created by shanhuang on 2018/10/15.
  */
class RMConsumerListenerImpl extends  RMConsumerListener with  Logging{
  private var consumer: RMEventConsumer = _
  private var lastProcessTime : Long = _

  override def setConsumer(consumer: RMEventConsumer) = {
    this.consumer = consumer
  }

  override def beforeEventExecute(consumer: RMEventConsumer, event: RMEvent) = {
    this.consumer = consumer
    this.lastProcessTime = System.currentTimeMillis
  }

  override def afterEventExecute(consumer: RMEventConsumer, event: RMEvent) = {
    this.consumer = consumer
    this.lastProcessTime = 0
  }

  override def checkConsumerHealthy(timeLimit: Long) = {
    val waitTime = System.currentTimeMillis - lastProcessTime
    if(lastProcessTime != 0 && waitTime > timeLimit){
      error("checkConsumerHealthy is false "+ consumer.getGroup.getGroupName + "wait time"+ waitTime)
      false
    } else if(consumer.future.isCancelled || consumer.future.isDone){
      error("checkConsumerHealthy is false for future is completed "+ consumer.getGroup.getGroupName)
      false
    }else{
      true
    }
  }
}
