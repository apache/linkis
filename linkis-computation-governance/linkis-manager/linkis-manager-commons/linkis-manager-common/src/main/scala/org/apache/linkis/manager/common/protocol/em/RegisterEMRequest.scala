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
 
package org.apache.linkis.manager.common.protocol.em

import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.protocol.message.RequestProtocol


class RegisterEMRequest extends EMRequest with RequestProtocol with Serializable {

  private var serviceInstance: ServiceInstance = null

  private var labels: util.Map[String, AnyRef] = null

  private var nodeResource: NodeResource = null

  private var user: String = null

  private var alias: String = null

  def getServiceInstance: ServiceInstance = serviceInstance

  def setServiceInstance(serviceInstance: ServiceInstance): Unit = {
    this.serviceInstance = serviceInstance
  }

  def getLabels: util.Map[String, AnyRef] = labels

  def setLabels(labels: util.Map[String, AnyRef]): Unit = {
    this.labels = labels
  }

  def getNodeResource: NodeResource = nodeResource

  def setNodeResource(nodeResource: NodeResource): Unit = {
    this.nodeResource = nodeResource
  }

  def setUser(user: String): Unit = {
    this.user = user
  }

  def getAlias: String = alias

  def setAlias(alias: String): Unit = {
    this.alias = alias
  }

  override def getUser: String = this.user
}
