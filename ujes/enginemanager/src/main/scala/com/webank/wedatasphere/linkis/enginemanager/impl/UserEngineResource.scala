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

package com.webank.wedatasphere.linkis.enginemanager.impl

import com.webank.wedatasphere.linkis.enginemanager.EngineResource
import com.webank.wedatasphere.linkis.resourcemanager.Resource

/**
  * Created by johnnwang on 2018/10/10.
  */
class UserEngineResource extends EngineResource {

  private var user: String = _
  private var creator: String = _
  private var resource: Resource = _

  def getUser = user

  def setUser(user: String) = this.user = user

  def getCreator = creator

  def setCreator(creator: String) = this.creator = creator

  override def getResource: Resource = resource

  override def setResource(resource: Resource): Unit = this.resource = resource
}
