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

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.node.RMNode
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.label.entity.Label



case class RequestResource(labels: util.List[Label[_]], resource: NodeResource)

case class RequestResourceAndWait(labels: util.List[Label[_]], resource: NodeResource, waitTime: Long)

case class RequestExpectedResource(labels: util.List[Label[_]], resource: NodeResource)

case class RequestExpectedResourceAndWait(labels: util.List[Label[_]], resource: NodeResource, waitTime: Long)

case class ResourceInited(labels: util.List[Label[_]], engineResource: NodeResource)

case class ResourceUpdated(labels: util.List[Label[_]], engineResource: NodeResource)

case class ResourceReleased(labels: util.List[Label[_]])

case class RequestResourceInfo(serviceInstances: Array[ServiceInstance])

case class ResourceInfo(resourceInfo: util.List[RMNode])



