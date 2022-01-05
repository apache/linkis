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

package org.apache.linkis.manager.engineplugin.common.resource

import java.util.Date

import org.apache.linkis.manager.common.entity.resource.{NodeResource, Resource, ResourceType}


class UserNodeResource extends NodeResource {

  private var id: Integer = _
  private var user: String = _
  private var resourceType: ResourceType = _
  private var minResource: Resource = _
  private var maxResource: Resource = _
  private var expectedResource: Resource = _
  private var usedResource: Resource = _
  private var lockedResource: Resource = _
  private var leftResource: Resource = _
  private var createTime: Date = _
  private var updateTime: Date = _


  def getUser = user

  def setUser(user: String) = this.user = user

  override def getResourceType: ResourceType = resourceType

  override def setResourceType(resourceType: ResourceType): Unit = this.resourceType = resourceType

  override def getMinResource: Resource = minResource

  override def setMinResource(resource: Resource): Unit = this.minResource = resource

  override def getMaxResource: Resource = maxResource

  override def setMaxResource(resource: Resource): Unit = this.maxResource = resource

  override def getExpectedResource: Resource = expectedResource

  override def setExpectedResource(resource: Resource): Unit = this.expectedResource = resource

  override def setUsedResource(usedResource: Resource): Unit = this.usedResource = usedResource

  override def getUsedResource: Resource = this.usedResource

  override def setLockedResource(lockedResource: Resource): Unit = this.lockedResource = lockedResource

  override def getLockedResource: Resource = this.lockedResource

  override def setLeftResource(leftResource: Resource): Unit = this.leftResource = leftResource

  override def getLeftResource: Resource = this.leftResource

  override def setCreateTime(createTime: Date): Unit = {
    this.createTime = createTime
  }

  override def getCreateTime: Date = {
    createTime
  }

  override def getUpdateTime: Date = {
    updateTime
  }

  override def setUpdateTime(updateTime: Date): Unit = {
    this.updateTime = updateTime
  }

  override def getId: Integer = id

  override def setId(id: Integer): Unit = this.id = id
}
