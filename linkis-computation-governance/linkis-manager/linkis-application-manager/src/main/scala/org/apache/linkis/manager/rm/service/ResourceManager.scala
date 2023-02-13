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

package org.apache.linkis.manager.rm.service

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.rm.{ResourceInfo, ResultResource}

import java.util

abstract class ResourceManager {

  /**
   * The registration method is mainly used to notify all RM nodes (including the node)
   * 该注册方法，主要是用于通知所有的RM节点（包括本节点）
   */
  def register(serviceInstance: ServiceInstance, resource: NodeResource): Unit

  /**
   * The registration method is mainly used to notify all RM nodes (including the node), and the
   * instance is offline. 该注册方法，主要是用于通知所有的RM节点（包括本节点），下线该实例
   */
  def unregister(serviceInstance: ServiceInstance): Unit

  /**
   * Request resources, if not successful, return directly 请求资源，如果不成功，直接返回
   *
   * @param labels
   * @param resource
   * @return
   */
  def requestResource(labels: util.List[Label[_]], resource: NodeResource): ResultResource

  /**
   * Request resources and wait for a certain amount of time until the requested resource is met
   * 请求资源，并等待一定的时间，直到满足请求的资源
   *
   * @param labels
   * @param resource
   * @param wait
   * @return
   */
  def requestResource(
      labels: util.List[Label[_]],
      resource: NodeResource,
      wait: Long
  ): ResultResource

  /**
   * When the resource is instantiated, the total amount of resources actually occupied is returned.
   * 当资源被实例化后，返回实际占用的资源总量
   *
   * @param labels
   * @param usedResource
   */
  def resourceUsed(labels: util.List[Label[_]], usedResource: NodeResource): Unit

  /**
   * 处理ECM和EC的资源上报请求，会对资源进行加减
   *
   * @param labels
   * @param reportResource
   */
  def resourceReport(labels: util.List[Label[_]], reportResource: NodeResource): Unit

  /**
   * Method called when the resource usage is released 当资源使用完成释放后，调用的方法
   *
   * @param ecNode
   */
  def resourceReleased(ecNode: EngineNode): Unit

  /**
   * If the IP and port are empty, return the resource status of all modules of a module   * Return
   * the use of this instance resource if there is an IP and port
   *
   * @param serviceInstances
   * @return
   */
  def getResourceInfo(serviceInstances: Array[ServiceInstance]): ResourceInfo

}
