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

package org.apache.linkis.manager.rm.utils

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.protocol.conf.{
  RequestQueryEngineConfigWithGlobalConfig,
  RequestQueryGlobalConfig,
  ResponseQueryConfig
}
import org.apache.linkis.manager.common.conf.RMConfiguration._
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary._
import org.apache.linkis.manager.common.exception.RMWarnException
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.{EngineTypeLabelCreator, LabelUtils}
import org.apache.linkis.protocol.CacheableProtocol
import org.apache.linkis.rpc.RPCMapCache

import java.util

object UserConfiguration extends Logging {

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  val globalMapCache = new RPCMapCache[String, String, String](
    Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue
  ) {

    override protected def createRequest(user: String): CacheableProtocol =
      RequestQueryGlobalConfig(user)

    override protected def createMap(any: Any): util.Map[String, String] = any match {
      case response: ResponseQueryConfig => response.getKeyAndValue
    }

  }

  val engineMapCache = new RPCMapCache[(UserCreatorLabel, EngineTypeLabel), String, String](
    Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue
  ) {

    override protected def createRequest(
        labelTuple: (UserCreatorLabel, EngineTypeLabel)
    ): CacheableProtocol = {
      RequestQueryEngineConfigWithGlobalConfig(labelTuple._1, labelTuple._2)
    }

    override protected def createMap(any: Any): util.Map[String, String] = any match {
      case response: ResponseQueryConfig => response.getKeyAndValue
    }

  }

  def getGlobalConfig(user: String): util.Map[String, String] = {
    globalMapCache.getCacheMap(user)
  }

  def getUserGlobalInstanceLimit(user: String): Int = {
    USER_AVAILABLE_INSTANCE.getValue(getGlobalConfig(user))
  }

  private def buildRequestLabel(
      user: String,
      creator: String = LabelUtils.COMMON_VALUE,
      engineType: String = LabelUtils.COMMON_VALUE
  ): (UserCreatorLabel, EngineTypeLabel) = {
    val userCreateLabel = labelFactory.createLabel(classOf[UserCreatorLabel])
    userCreateLabel.setUser(user)
    userCreateLabel.setCreator(LabelUtils.COMMON_VALUE)
    val engineType = EngineTypeLabelCreator.createEngineTypeLabel(LabelUtils.COMMON_VALUE)
    (userCreateLabel, engineType)
  }

  def getUserConfiguredResource(
      resourceType: ResourceType,
      userCreatorLabel: UserCreatorLabel,
      engineTypeLabel: EngineTypeLabel
  ): Resource = {
    Utils.tryAndWarn {
      val userCreatorAvailableResource = generateResource(
        resourceType,
        engineMapCache.getCacheMap(userCreatorLabel, engineTypeLabel)
      )
      logger.info(
        s"${userCreatorLabel.getUser} on creator ${userCreatorLabel.getCreator} available engine ${engineTypeLabel.getEngineType} resource:$userCreatorAvailableResource"
      )
      userCreatorAvailableResource
    }
  }

  def getUserConfiguredResource(
      resourceType: ResourceType,
      engineType: String,
      user: String,
      creator: String
  ): Resource = {
    Utils.tryAndWarn {
      val userCreatorAvailableResource = generateResource(
        resourceType,
        engineMapCache.getCacheMap(buildRequestLabel(user, creator, engineType = engineType))
      )
      logger.info(s"$user on creator available resource:$userCreatorAvailableResource")
      userCreatorAvailableResource
    }
  }

  def generateResource(
      policy: ResourceType,
      userConfiguration: util.Map[String, String]
  ): Resource = policy match {
    case ResourceType.CPU => new CPUResource(USER_AVAILABLE_CPU.getValue(userConfiguration))
    case ResourceType.Memory =>
      new MemoryResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong)
    case ResourceType.Load =>
      new LoadResource(
        USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong,
        USER_AVAILABLE_CPU.getValue(userConfiguration)
      )
    case ResourceType.Instance =>
      new InstanceResource(USER_AVAILABLE_INSTANCE.getValue(userConfiguration))
    case ResourceType.LoadInstance =>
      new LoadInstanceResource(
        USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong,
        USER_AVAILABLE_CPU.getValue(userConfiguration),
        USER_AVAILABLE_INSTANCE.getValue(userConfiguration)
      )
    case ResourceType.Yarn =>
      new YarnResource(
        USER_AVAILABLE_YARN_INSTANCE_MEMORY.getValue(userConfiguration).toLong,
        USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
        USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration),
        USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration)
      )
    case ResourceType.DriverAndYarn =>
      new DriverAndYarnResource(
        new LoadInstanceResource(
          USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong,
          USER_AVAILABLE_CPU.getValue(userConfiguration),
          USER_AVAILABLE_INSTANCE.getValue(userConfiguration)
        ),
        new YarnResource(
          USER_AVAILABLE_YARN_INSTANCE_MEMORY.getValue(userConfiguration).toLong,
          USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
          USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration),
          USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration)
        )
      )
    case ResourceType.Special => new SpecialResource(new java.util.HashMap[String, AnyVal]())
    case _ =>
      throw new RMWarnException(
        NOT_RESOURCE_RESULT_TYPE.getErrorCode,
        NOT_RESOURCE_RESULT_TYPE.getErrorDesc
      )
  }

}
