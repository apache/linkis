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

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.am.conf.{AMConfiguration, EngineConnConfigurationService}
import org.apache.linkis.manager.common.entity.enumeration.{MaintainType, NodeStatus}
import org.apache.linkis.manager.common.protocol.engine.{ECCanKillRequest, ECCanKillResponse}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.{DayOfWeek, LocalDateTime}
import java.util

import scala.collection.JavaConverters._

@Service
class DefaultEngineConnCanKillService
    extends AbstractEngineService
    with EngineConnCanKillService
    with Logging {

  @Autowired
  private var engineConnConfigurationService: EngineConnConfigurationService = _

  @Autowired
  private var labelService: NodeLabelService = _

  @Receiver
  override def canKillEngineConn(ecCanKillRequest: ECCanKillRequest): ECCanKillResponse = {
    val ecCanKillResponse = new ECCanKillResponse()
    if (
        null == ecCanKillRequest || StringUtils.isBlank(
          ecCanKillRequest.getUser
        ) || null == ecCanKillRequest.getEngineTypeLabel
    ) {
      ecCanKillResponse.setFlag(true)
      return ecCanKillResponse
    }

    val labels = new util.ArrayList[Label[_]]()
    labels.add(ecCanKillRequest.getEngineTypeLabel)
    labels.add(ecCanKillRequest.getUserCreatorLabel)
    val maintainType: MaintainType = Utils.tryCatch {
      val configProps = engineConnConfigurationService.getConsoleConfiguration(labels)
      AMConfiguration.EC_MAINTAIN_TIME_STR.getValue(configProps) match {
        case "周一到周五工作时间保持一个引擎" =>
          MaintainType.day
        case "周一到周五保持一个引擎" =>
          MaintainType.week
        case _ =>
          MaintainType.Default
      }
    } { case e: Exception =>
      logger.warn("failed to get engine maintain time from publicservice", e)
      MaintainType.Default
    }
    maintainType match {
      case MaintainType.day =>
        canKillInWorkTime(ecCanKillRequest)
      case MaintainType.week =>
        canKillInWorkDay(ecCanKillRequest)
      case _ =>
        ecCanKillResponse.setFlag(true)
        ecCanKillResponse.setReason("default MaintainType")
        ecCanKillResponse
    }
  }

  private def canKillInWorkTime(ecCanKillRequest: ECCanKillRequest): ECCanKillResponse = {
    if (isInWorkTime()) {
      canKill(ecCanKillRequest)
    } else {
      val ecCanKillResponse = new ECCanKillResponse
      ecCanKillResponse.setFlag(true)
      ecCanKillResponse.setReason("not workTime")
      ecCanKillResponse
    }
  }

  private def canKill(ecCanKillRequest: ECCanKillRequest): ECCanKillResponse = {
    val ecCanKillResponse = new ECCanKillResponse
    val ecNodes = getEngineNodeManager
      .listEngines(ecCanKillRequest.getUser)
      .asScala
      .filter(!_.getServiceInstance.equals(ecCanKillRequest.getEngineConnInstance))
    if (null == ecNodes || ecNodes.isEmpty) {
      ecCanKillResponse.setFlag(false)
      logger.info(s"There are no other engines, $ecCanKillRequest can not be killed")
      ecCanKillResponse.setReason("There are no other engines")
      return ecCanKillResponse
    }
    val engineConnNodes = ecNodes.filter(node => NodeStatus.isAvailable(node.getNodeStatus))
    if (engineConnNodes.isEmpty) {
      ecCanKillResponse.setFlag(false)
      logger.info(s"There are no other engines available, $ecCanKillRequest can not be killed")
      ecCanKillResponse.setReason("There are no other engines available")
      return ecCanKillResponse
    }

    val ecWithLabels = engineConnNodes.filter { node =>
      val labels = labelService.getNodeLabels(node.getServiceInstance)
      val userCreatorLabel = LabelUtil.getUserCreatorLabel(labels)
      val engineTypeLabel = LabelUtil.getEngineTypeLabel(labels)
      ecCanKillRequest.getUserCreatorLabel.getStringValue.equals(
        userCreatorLabel.getStringValue
      ) && ecCanKillRequest.getEngineTypeLabel.getStringValue.equals(engineTypeLabel.getStringValue)
    }

    if (ecWithLabels.nonEmpty) {
      ecCanKillResponse.setFlag(true)
      logger.info(s"There are engines available, $ecCanKillRequest can be killed")
      ecCanKillResponse.setReason("There are  engines available")
    } else {
      ecCanKillResponse.setFlag(false)
      logger.info(
        s"There are no label equal engines available, $ecCanKillRequest can not be killed"
      )
      ecCanKillResponse.setReason("There are no label equal engines available")
    }
    ecCanKillResponse
  }

  private def canKillInWorkDay(ecCanKillRequest: ECCanKillRequest): ECCanKillResponse = {
    if (isWeekend()) {
      val ecCanKillResponse = new ECCanKillResponse
      ecCanKillResponse.setFlag(true)
      ecCanKillResponse.setReason("is weekend")
      ecCanKillResponse
    } else {
      canKill(ecCanKillRequest)
    }
  }

  private def isWeekend(): Boolean = {
    if (Configuration.IS_TEST_MODE.getValue) {
      return true
    }
    val date = LocalDateTime.now()
    val dayOfWeek = date.getDayOfWeek
    (dayOfWeek == DayOfWeek.SUNDAY) || (dayOfWeek == DayOfWeek.SATURDAY)
  }

  private def isInWorkTime(): Boolean = {
    if (isWeekend()) return false
    val date = LocalDateTime.now()
    date.getHour > AMConfiguration.EC_MAINTAIN_WORK_START_TIME && date.getHour < AMConfiguration.EC_MAINTAIN_WORK_END_TIME
  }

}
