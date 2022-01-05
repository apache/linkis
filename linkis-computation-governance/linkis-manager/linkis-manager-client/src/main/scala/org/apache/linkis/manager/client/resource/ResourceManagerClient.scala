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
 
package org.apache.linkis.manager.client.resource

import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.node.EMNode
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.resourcemanager._
import org.apache.linkis.resourcemanager.exception.RMErrorException
import org.apache.linkis.resourcemanager.utils.RMConfiguration
import org.apache.linkis.rpc.Sender
import javax.annotation.PostConstruct
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._



@Component
class ResourceManagerClient(private var serviceInstance: ServiceInstance) extends Logging {

  private val sender = Sender.getSender(RMConfiguration.RM_APPLICATION_NAME.getValue)

  def this() = this(null)

  @PostConstruct
  def init(): Unit = {
    info("ResourceManagerClient init")
    if (serviceInstance == null) serviceInstance = Sender.getThisServiceInstance
  }

  def register(eMNode: EMNode): Unit = sender.send(eMNode)

  def unregister(eMNode: EMNode): Unit = sender.send(eMNode.getServiceInstance)

  def requestResource(labels: util.List[Label[_]], resource: NodeResource): ResultResource = sender.ask(RequestResource(labels, resource)).asInstanceOf[ResultResource]

  def requestResource(labels: util.List[Label[_]], resource: NodeResource, wait: Long) = sender.ask(RequestResourceAndWait(labels, resource, wait)).asInstanceOf[ResultResource]

  def requestExpectedResource(labels: util.List[Label[_]], resource: NodeResource): ResultResource = sender.ask(RequestExpectedResource(labels, resource)).asInstanceOf[ResultResource]

  def requestExpectedResource(labels: util.List[Label[_]], resource: NodeResource, wait: Long) = sender.ask(RequestExpectedResourceAndWait(labels, resource, wait)).asInstanceOf[ResultResource]

  def resourceInited(labels: util.List[Label[_]], engineResource: NodeResource): Unit = {
    labels.find(_.isInstanceOf[EngineInstanceLabel]) match {
      case Some(l) => sender.send(ResourceInited(labels, engineResource))
      case None => throw new RMErrorException(110020, "EngineInstanceLabel is required to use resource")
    }
  }

  def resourceUpdated(labels: util.List[Label[_]], engineResource: NodeResource): Unit = {
    labels.find(_.isInstanceOf[EngineInstanceLabel]) match {
      case Some(l) => sender.send(ResourceUpdated(labels, engineResource))
      case None => throw new RMErrorException(110020, "EngineInstanceLabel is required to use resource")
    }
  }

  def resourceReleased(labels: util.List[Label[_]]): Unit = {
    labels.find(_.isInstanceOf[EngineInstanceLabel]) match {
      case Some(l) => sender.send(ResourceReleased(labels))
      case None => throw new RMErrorException(110020, "EngineInstanceLabel is required to release resource")
    }
  }

  def requestResourceInfo(serviceInstances: Array[ServiceInstance]): ResourceInfo = sender.ask(RequestResourceInfo(serviceInstances)).asInstanceOf[ResourceInfo]

}