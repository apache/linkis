/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.manager.am.service.engine

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.am.recycle.RecyclingRuleExecutor
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineRecyclingRequest, EngineStopRequest}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.publisher.MessagePublisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._



@Service
class DefaultEngineRecycleService extends AbstractEngineService with EngineRecycleService with Logging {

  @Autowired
  private var ruleExecutorList: util.List[RecyclingRuleExecutor] = _

  @Autowired
  private var publisher: MessagePublisher = _

  @Receiver
  override def recycleEngine(engineRecyclingRequest: EngineRecyclingRequest): Array[ServiceInstance] = {
    if (null == ruleExecutorList) {
      error("has not recycling rule")
      return null
    }
    info(s"start to recycle engine by ${engineRecyclingRequest.getUser}")
    //1. 规则解析
    val ruleList = engineRecyclingRequest.getRecyclingRuleList
    //2. 返回一系列待回收Engine，
    val recyclingNodeSet = ruleList.flatMap { rule =>
      val ruleExecutorOption = ruleExecutorList.find(_.ifAccept(rule))
      if (ruleExecutorOption.isDefined) {
        ruleExecutorOption.get.executeRule(rule)
      } else {
        Nil
      }
    }.filter(null != _).toSet
    if (null == recyclingNodeSet) {
      return null
    }
    info(s"The list of engines recycled this time is as follows:${recyclingNodeSet}")
    //3. 调用EMService stopEngine
    recyclingNodeSet.foreach { serviceInstance =>
      val stopEngineRequest = new EngineStopRequest(serviceInstance, engineRecyclingRequest.getUser)
      publisher.publish(stopEngineRequest)
    }
    info(s"Finished to recycle engine ,num ${recyclingNodeSet.size}")
    recyclingNodeSet.toArray
  }

}
