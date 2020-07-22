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

package com.webank.wedatasphere.linkis.resourcemanager.service.metadata

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.protocol.config.RequestQueryAppConfigWithGlobal
import com.webank.wedatasphere.linkis.protocol.utils.ProtocolUtils
import com.webank.wedatasphere.linkis.resourcemanager.ResourceRequestPolicy._
import com.webank.wedatasphere.linkis.resourcemanager._
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMWarnException
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMConfiguration._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable

/**
  * Created by shanhuang on 9/11/18.
  */
@Component
class DefaultUserMetaData extends UserMetaData with Logging {

  @Autowired
  var moduleResourceRecordService: ModuleResourceRecordService = _

  override def getUserAvailableResource(moduleName: String, user: String, creator: String): (UserAvailableResource, UserAvailableResource) = {
    val policy = moduleResourceRecordService.getModulePolicy(moduleName)
    val appName = ProtocolUtils.getAppName(moduleName).getOrElse(moduleName)
    val userModuleAvailableResource = UserAvailableResource(moduleName, generateResource(policy, UserConfiguration.getCacheMap(RequestQueryAppConfigWithGlobal(user, null, appName, true))))
    val userCreatorAvailableResource = UserAvailableResource(moduleName, generateResource(policy, UserConfiguration.getCacheMap(RequestQueryAppConfigWithGlobal(user, creator, appName, true))))
    info(s"$user available resource of module:$userModuleAvailableResource,on creator available resource:$userCreatorAvailableResource")
    (userModuleAvailableResource, userCreatorAvailableResource)
  }

  override def getUserGlobalInstanceLimit(user: String): Int = {
    val userConfiguration = UserConfiguration.getCacheMap(RequestQueryAppConfigWithGlobal(user, null, null, true))
    USER_AVAILABLE_INSTANCE.getValue(userConfiguration)
  }

  def generateResource(policy: ResourceRequestPolicy, userConfiguration: util.Map[String, String]): Resource = policy match {
    case CPU => new CPUResource(USER_AVAILABLE_CPU.getValue(userConfiguration))
    case Memory => new MemoryResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong)
    case Load => new LoadResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong, USER_AVAILABLE_CPU.getValue(userConfiguration))
    case Instance => new InstanceResource(USER_AVAILABLE_INSTANCE.getValue(userConfiguration))
    case LoadInstance => new LoadInstanceResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong, USER_AVAILABLE_CPU.getValue(userConfiguration), USER_AVAILABLE_INSTANCE.getValue(userConfiguration))
    case Yarn => new YarnResource(USER_AVAILABLE_YARN_INSTANCE_MEMORY.getValue(userConfiguration).toLong,
      USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
      USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration), USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration))
    case DriverAndYarn => new DriverAndYarnResource(new LoadInstanceResource(USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong, USER_AVAILABLE_CPU.getValue(userConfiguration), USER_AVAILABLE_INSTANCE.getValue(userConfiguration)),
      new YarnResource(USER_AVAILABLE_YARN_INSTANCE_MEMORY.getValue(userConfiguration).toLong,
        USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
        USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration), USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration)))
    case Special => new SpecialResource(new java.util.HashMap[String, AnyVal]())
    case _ => throw new RMWarnException(111003, "not supported resource result policy ")
  }

  override def getUserModuleInfo(moduleName: String, user: String): Map[String, Any] = {
    val appName = ProtocolUtils.getAppName(moduleName).getOrElse(moduleName)
    val userConfiguration = UserConfiguration.getCacheMap(RequestQueryAppConfigWithGlobal(user, null, appName, true))
    val userModuleInfo = new mutable.HashMap[String, Any]()
    userModuleInfo.put("waitUsed", USER_MODULE_WAIT_USED.getValue(userConfiguration))
    userModuleInfo.put("waitReleased", USER_MODULE_WAIT_RELEASE.getValue(userConfiguration))
    userModuleInfo.toMap
  }
}

