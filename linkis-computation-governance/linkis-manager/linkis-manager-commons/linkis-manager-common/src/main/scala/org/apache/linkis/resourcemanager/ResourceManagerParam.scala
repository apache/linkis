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

import java.util

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.entity.node.RMNode
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.label.entity.Label



case class RequestResource(labels: util.List[Label[_]], resource: NodeResource)

case class RequestResourceAndWait(labels: util.List[Label[_]], resource: NodeResource, waitTime: Long)

case class RequestExpectedResource(labels: util.List[Label[_]], resource: NodeResource)

case class RequestExpectedResourceAndWait(labels: util.List[Label[_]], resource: NodeResource, waitTime: Long)

case class ResourceInited(labels: util.List[Label[_]], engineResource: NodeResource)

case class ResourceUpdated(labels: util.List[Label[_]], engineResource: NodeResource)

case class ResourceReleased(labels: util.List[Label[_]])

case class RequestResourceInfo(serviceInstances: Array[ServiceInstance])

case class ResourceInfo(resourceInfo: util.List[RMNode])



