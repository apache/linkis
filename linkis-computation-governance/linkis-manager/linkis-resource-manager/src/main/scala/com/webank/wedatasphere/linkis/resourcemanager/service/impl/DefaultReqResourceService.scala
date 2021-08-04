/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.resourcemanager.service.impl

import com.webank.wedatasphere.linkis.manager.common.entity.resource.{NodeResource, ResourceSerializer, ResourceType}
import com.webank.wedatasphere.linkis.resourcemanager.domain.RMLabelContainer
import com.webank.wedatasphere.linkis.resourcemanager.service.{LabelResourceService, RequestResourceService}
import org.json4s.DefaultFormats

class DefaultReqResourceService(labelResourceService: LabelResourceService) extends RequestResourceService(labelResourceService){

  implicit val formats = DefaultFormats + ResourceSerializer

  override val resourceType: ResourceType = ResourceType.Default

  override def canRequest(labelContainer: RMLabelContainer, resource: NodeResource): Boolean = {
    super.canRequest(labelContainer, resource)
  }
}
