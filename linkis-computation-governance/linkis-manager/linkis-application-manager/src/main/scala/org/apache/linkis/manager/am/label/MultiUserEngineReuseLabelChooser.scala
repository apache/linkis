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

package org.apache.linkis.manager.am.label

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.exception.{AMErrorCode, AMErrorException}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import org.springframework.stereotype.Component

import java.util

import scala.collection.JavaConverters._

@Component
class MultiUserEngineReuseLabelChooser extends EngineReuseLabelChooser with Logging {

  private val multiUserEngine = AMConfiguration.MULTI_USER_ENGINE_TYPES.getValue.split(",")

  private val userMap = getMultiUserEngineUserMap()

  def getMultiUserEngineUserMap(): util.Map[String, String] = {
    val userJson = AMConfiguration.MULTI_USER_ENGINE_USER.getValue
    if (StringUtils.isNotBlank(userJson)) {
      val userMap =
        BDPJettyServerHelper.gson.fromJson(userJson, classOf[java.util.Map[String, String]])
      userMap
    } else {
      throw new AMErrorException(
        AMErrorCode.AM_CONF_ERROR.getErrorCode,
        s"Multi-user engine parameter configuration error,please check key ${AMConfiguration.MULTI_USER_ENGINE_USER.key}"
      )
    }

  }

  /**
   * Filter out UserCreator Label that supports multi-user engine 过滤掉支持多用户引擎的UserCreator Label
   *
   * @param labelList
   * @return
   */
  override def chooseLabels(labelList: util.List[Label[_]]): util.List[Label[_]] = {
    val labels = labelList.asScala
    val engineTypeLabelOption = labels.find(_.isInstanceOf[EngineTypeLabel])
    if (engineTypeLabelOption.isDefined) {
      val engineTypeLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
      val maybeString = multiUserEngine.find(_.equalsIgnoreCase(engineTypeLabel.getEngineType))
      val userCreatorLabelOption = labels.find(_.isInstanceOf[UserCreatorLabel])
      if (maybeString.isDefined && userCreatorLabelOption.isDefined) {
        val userAdmin = userMap.get(engineTypeLabel.getEngineType)
        val userCreatorLabel = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
        logger.info(
          s"For multi user engine to reset userCreatorLabel user ${userCreatorLabel.getUser} to Admin $userAdmin "
        )
        userCreatorLabel.setUser(userAdmin)
        return labels.asJava
      }
    }
    labelList
  }

}
