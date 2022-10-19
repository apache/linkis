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

package org.apache.linkis.manager.am.service.engine

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.am.recycle.RecyclingRuleExecutor
import org.apache.linkis.manager.common.protocol.engine.{EngineRecyclingRequest, EngineStopRequest}
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util

import scala.collection.JavaConverters._

@Service
class DefaultEngineRecycleService
    extends AbstractEngineService
    with EngineRecycleService
    with Logging {

  @Autowired
  private var ruleExecutorList: util.List[RecyclingRuleExecutor] = _

  @Autowired
  private var engineStopService: EngineStopService = _

  @Receiver
  override def recycleEngine(
      engineRecyclingRequest: EngineRecyclingRequest
  ): Array[ServiceInstance] = {
    if (null == ruleExecutorList) {
      logger.error("has not recycling rule")
      return null
    }
    logger.info(s"start to recycle engine by ${engineRecyclingRequest.getUser}")
    // 1. 规则解析
    val ruleList = engineRecyclingRequest.getRecyclingRuleList
    // 2. 返回一系列待回收Engine，
    val recyclingNodeSet = ruleList.asScala
      .flatMap { rule =>
        val ruleExecutorOption = ruleExecutorList.asScala.find(_.ifAccept(rule))
        if (ruleExecutorOption.isDefined) {
          ruleExecutorOption.get.executeRule(rule)
        } else {
          Nil
        }
      }
      .filter(null != _)
      .toSet
    if (null == recyclingNodeSet) {
      return null
    }
    logger.info(s"The list of engines recycled this time is as follows:${recyclingNodeSet}")
    // 3. 调用EMService stopEngine
    recyclingNodeSet.foreach { serviceInstance =>
      val stopEngineRequest =
        new EngineStopRequest(serviceInstance, engineRecyclingRequest.getUser)
      engineStopService.asyncStopEngine(stopEngineRequest)
    }
    logger.info(s"Finished to recycle engine ,num ${recyclingNodeSet.size}")
    recyclingNodeSet.toArray
  }

}
