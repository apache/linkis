/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.resourcemanager

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.node.EMNode
import org.apache.linkis.resourcemanager.service.ResourceManager
import org.apache.linkis.rpc.{Receiver, Sender}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration

class RMReceiver extends Receiver with Logging {

  @Autowired
  var rm: ResourceManager = _

  override def receive(message: Any, sender: Sender): Unit = message match {
    case eMNode: EMNode => rm.register(eMNode.getServiceInstance, eMNode.getNodeResource)
    case serviceInstance: ServiceInstance => rm.unregister(serviceInstance)
    case ResourceInited(labels, engineResource) => rm.resourceUsed(labels, engineResource)
    case ResourceReleased(labels) => rm.resourceReleased(labels)
  }

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case RequestResource(labels, resource) => rm.requestResource(labels, resource)
    case RequestResourceAndWait(labels, resource, waitTime) => rm.requestResource(labels, resource, waitTime)
    case RequestResourceInfo(serviceInstances) => rm.getResourceInfo(serviceInstances)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = null
}
