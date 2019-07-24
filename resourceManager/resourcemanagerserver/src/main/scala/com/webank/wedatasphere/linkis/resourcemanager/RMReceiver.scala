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

package com.webank.wedatasphere.linkis.resourcemanager

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.rpc.utils.SenderUtils
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration

/**
  * Created by shanhuang on 9/11/18.
  */

@Component
class RMReceiver extends Receiver with Logging {

  @Autowired
  var rm: ResourceManager = _

  override def receive(message: Any, sender: Sender): Unit = message match {
    case ModuleInfo(moduleInstance, totalResource, protectedResource, resourceRequestPolicy) => rm.register(ModuleInfo(moduleInstance, totalResource, protectedResource, resourceRequestPolicy))
    case moduleInstance: ServiceInstance => rm.unregister(moduleInstance)
    case ResourceInited(resource, moduleInstance, realUsed, engineInstance) => rm.resourceInited(resource, moduleInstance, realUsed, SenderUtils.getSenderServiceInstance(sender))
    case ResourceReleased(resultResource, moduleInstance) => rm.resourceReleased(resultResource, moduleInstance)
  }

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case RequestResource(moduleInstance, user, creator, resource) =>
      rm.requestResource(moduleInstance, user, creator, resource)
    case RequestResourceAndWait(moduleInstance, user, creator, resource, waitTime) =>
      rm.requestResource(moduleInstance, user, creator, resource, waitTime)
    case moduleInstance: ServiceInstance => ResourceInfo(rm.getModuleResourceInfo(moduleInstance))
    case ResourceOverload(moduleInstance) => rm.instanceCanService(moduleInstance)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = null
}
