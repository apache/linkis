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

package org.apache.linkis.entrance.utils

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.instance.label.client.InstanceLabelClient
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.{LabelKeyConstant, LabelValueConstant}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters.asScalaBufferConverter

object EntranceUtils extends Logging {

  private val SPLIT = ","

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  def getUserCreatorEcTypeKey(
      userCreatorLabel: UserCreatorLabel,
      engineTypeLabel: EngineTypeLabel
  ): String = {
    userCreatorLabel.getStringValue + SPLIT + engineTypeLabel.getStringValue
  }

  def fromKeyGetLabels(key: String): (UserCreatorLabel, EngineTypeLabel) = {
    if (StringUtils.isBlank(key)) (null, null)
    else {
      val labelStringValues = key.split(SPLIT)
      if (labelStringValues.length < 2) return (null, null)
      val userCreatorLabel = labelFactory
        .createLabel[UserCreatorLabel](LabelKeyConstant.USER_CREATOR_TYPE_KEY, labelStringValues(0))
      val engineTypeLabel = labelFactory
        .createLabel[EngineTypeLabel](LabelKeyConstant.ENGINE_TYPE_KEY, labelStringValues(1))
      (userCreatorLabel, engineTypeLabel)
    }
  }

  def getDefaultCreatorECTypeKey(creator: String, ecType: String): String = {
    val userCreatorLabel =
      labelFactory.createLabel[UserCreatorLabel](LabelKeyConstant.USER_CREATOR_TYPE_KEY)
    val ecTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(ecType)
    userCreatorLabel.setUser("*")
    userCreatorLabel.setCreator(creator)
    getUserCreatorEcTypeKey(userCreatorLabel, ecTypeLabel)
  }

  def getRunningEntranceNumber(): Int = {
    val entranceNum = Sender.getInstances(Sender.getThisServiceInstance.getApplicationName).length
    val labelList = new util.ArrayList[Label[_]]()
    val offlineRouteLabel = LabelBuilderFactoryContext.getLabelBuilderFactory
      .createLabel[RouteLabel](LabelKeyConstant.ROUTE_KEY, LabelValueConstant.OFFLINE_VALUE)
    labelList.add(offlineRouteLabel)
    var offlineIns: Array[ServiceInstance] = null
    Utils.tryAndWarn {
      offlineIns = InstanceLabelClient.getInstance
        .getInstanceFromLabel(labelList)
        .asScala
        .filter(l =>
          null != l && l.getApplicationName
            .equalsIgnoreCase(Sender.getThisServiceInstance.getApplicationName)
        )
        .toArray
    }
    val entranceRealNumber = if (null != offlineIns) {
      logger.info(s"There are ${offlineIns.length} offlining instance.")
      entranceNum - offlineIns.length
    } else {
      entranceNum
    }
    /*
    Sender.getInstances may get 0 instances due to cache in Sender. So this instance is the one instance.
     */
    if (entranceRealNumber <= 0) {
      logger.error(
        s"Got ${entranceRealNumber} ${Sender.getThisServiceInstance.getApplicationName} instances."
      )
      1
    } else {
      entranceRealNumber
    }
  }

}
