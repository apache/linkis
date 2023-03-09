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

package org.apache.linkis.entrance.orchestrator.plugin

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.governance.common.protocol.conf.{
  RequestQueryEngineConfigWithGlobalConfig,
  ResponseQueryConfig
}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.orchestrator.plugin.UserParallelOrchestratorPlugin
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}

class EntranceUserParallelOrchestratorPlugin extends UserParallelOrchestratorPlugin with Logging {

  private val SPLIT = ","

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  private def getDefaultMaxRuningNum: Int = {
    EntranceConfiguration.WDS_LINKIS_INSTANCE.getHotValue()
  }

  private val sender: Sender =
    Sender.getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)

  private val configCache: LoadingCache[String, Integer] = CacheBuilder
    .newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .expireAfterWrite(EntranceConfiguration.USER_PARALLEL_REFLESH_TIME.getValue, TimeUnit.MINUTES)
    .build(new CacheLoader[String, Integer]() {

      override def load(key: String): Integer = {
        val (userCreatorLabel, engineTypeLabel) = fromKeyGetLabels(key)
        val keyAndValue = Utils.tryAndWarnMsg {
          sender
            .ask(RequestQueryEngineConfigWithGlobalConfig(userCreatorLabel, engineTypeLabel))
            .asInstanceOf[ResponseQueryConfig]
            .getKeyAndValue
        }(
          "Get user configurations from configuration server failed! Next use the default value to continue."
        )
        if (
            null == keyAndValue || !keyAndValue
              .containsKey(EntranceConfiguration.WDS_LINKIS_INSTANCE.key)
        ) {
          logger.error(
            s"cannot found user configuration key:${EntranceConfiguration.WDS_LINKIS_INSTANCE.key}," +
              s"will use default value ${EntranceConfiguration.WDS_LINKIS_INSTANCE.getHotValue()}。All config map: ${BDPJettyServerHelper.gson
                .toJson(keyAndValue)}"
          )
        }
        val maxRunningJobs = EntranceConfiguration.WDS_LINKIS_INSTANCE.getValue(keyAndValue, true)
        maxRunningJobs
      }

    })

  override def getUserMaxRunningJobs(user: String, labels: util.List[Label[_]]): Int = {

    if (null == labels || labels.isEmpty) {
      return getDefaultMaxRuningNum
    }
    var userCreatorLabel: UserCreatorLabel = null
    var engineTypeLabel: EngineTypeLabel = null
    labels.asScala.foreach {
      case label: UserCreatorLabel => userCreatorLabel = label
      case label: EngineTypeLabel => engineTypeLabel = label
      case _ =>
    }
    if (null == userCreatorLabel || null == engineTypeLabel) {
      return getDefaultMaxRuningNum
    }
    configCache.get(getKey(userCreatorLabel, engineTypeLabel))
  }

  private def getKey(
      userCreatorLabel: UserCreatorLabel,
      engineTypeLabel: EngineTypeLabel
  ): String = {
    userCreatorLabel.getStringValue + SPLIT + engineTypeLabel.getStringValue
  }

  private def fromKeyGetLabels(key: String): (UserCreatorLabel, EngineTypeLabel) = {
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

  override def isReady: Boolean = true

  override def start(): Unit = {}

  override def close(): Unit = {
    this.configCache.cleanUp()
  }

}
