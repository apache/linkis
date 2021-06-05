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

package com.webank.wedatasphere.linkis.resourcemanager.utils

import java.util

import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.governance.common.protocol.conf.{RequestQueryEngineConfig, RequestQueryGlobalConfig, ResponseQueryConfig}
import com.webank.wedatasphere.linkis.manager.common.entity.resource._
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.label.utils.{EngineTypeLabelCreator, LabelUtils}
import com.webank.wedatasphere.linkis.protocol.CacheableProtocol
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMWarnException
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMConfiguration._
import com.webank.wedatasphere.linkis.rpc.RPCMapCache

object UserConfiguration extends Logging {

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  val globalMapCache = new RPCMapCache[String, String, String](
    Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue) {
    override protected def createRequest(user: String): CacheableProtocol = RequestQueryGlobalConfig(user)

    override protected def createMap(any: Any): util.Map[String, String] = any match {
      case response: ResponseQueryConfig => response.getKeyAndValue
    }
  }

  val engineMapCache = new RPCMapCache[(UserCreatorLabel, EngineTypeLabel), String, String](
    Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue) {
    override protected def createRequest(labelTuple: (UserCreatorLabel, EngineTypeLabel)): CacheableProtocol =
      RequestQueryEngineConfig(labelTuple._1, labelTuple._2, GovernanceCommonConf.CONF_FILTER_RM)

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

  private def buildRequestLabel(user: String, creator: String = LabelUtils.COMMON_VALUE, engineType: String = LabelUtils.COMMON_VALUE): (UserCreatorLabel, EngineTypeLabel) = {
    val userCreateLabel = labelFactory.createLabel(classOf[UserCreatorLabel])
    userCreateLabel.setUser(user)
    userCreateLabel.setCreator(LabelUtils.COMMON_VALUE)
    val engineType = EngineTypeLabelCreator.createEngineTypeLabel(LabelUtils.COMMON_VALUE)
    (userCreateLabel, engineType)
  }

  def getUserConfiguredResource(resourceType: ResourceType, userCreatorLabel: UserCreatorLabel,  engineTypeLabel: EngineTypeLabel) = {
    Utils.tryAndWarn{
      val userCreatorAvailableResource = generateResource(resourceType, engineMapCache.getCacheMap(userCreatorLabel, engineTypeLabel))
      info(s"${userCreatorLabel.getUser} on creator ${userCreatorLabel.getCreator} available engine ${engineTypeLabel.getEngineType} resource:$userCreatorAvailableResource")
      userCreatorAvailableResource
    }
  }

  def getUserConfiguredResource(resourceType: ResourceType, engineType: String, user: String, creator: String): Resource = {
    Utils.tryAndWarn{
      val userCreatorAvailableResource = generateResource(resourceType, engineMapCache.getCacheMap(buildRequestLabel(user, creator, engineType = engineType)))
      info(s"$user on creator available resource:$userCreatorAvailableResource")
      userCreatorAvailableResource
    }
  }

  def generateResource(policy: ResourceType, userConfiguration: util.Map[String, String]): Resource = policy match {
    case ResourceType.CPU => new CPUResource(USER_AVAILABLE_CPU.getValue(userConfiguration))
    case ResourceType.Memory => new MemoryResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong)
    case ResourceType.Load => new LoadResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong, USER_AVAILABLE_CPU.getValue(userConfiguration))
    case ResourceType.Instance => new InstanceResource(USER_AVAILABLE_INSTANCE.getValue(userConfiguration))
    case ResourceType.LoadInstance => new LoadInstanceResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong, USER_AVAILABLE_CPU.getValue(userConfiguration), USER_AVAILABLE_INSTANCE.getValue(userConfiguration))
    case ResourceType.Yarn => new YarnResource(USER_AVAILABLE_YARN_INSTANCE_MEMORY.getValue(userConfiguration).toLong,
      USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
      USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration), USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration))
    case ResourceType.DriverAndYarn => new DriverAndYarnResource(new LoadInstanceResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong, USER_AVAILABLE_CPU.getValue(userConfiguration), USER_AVAILABLE_INSTANCE.getValue(userConfiguration)),
      new YarnResource(USER_AVAILABLE_YARN_INSTANCE_MEMORY.getValue(userConfiguration).toLong,
        USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
        USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration), USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration)))
    case ResourceType.Special => new SpecialResource(new java.util.HashMap[String, AnyVal]())
    case _ => throw new RMWarnException(11003, "not supported resource result type ")
  }

}
