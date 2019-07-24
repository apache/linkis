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

import com.webank.wedatasphere.linkis.enginemanager.{EngineResource, ResourceRequester}
import com.webank.wedatasphere.linkis.resourcemanager.ResultResource
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient

/**
  * Created by johnnwang on 2018/10/10.
  */
class ResourceRequesterImpl(rmClient: ResourceManagerClient) extends ResourceRequester {

  override def request(resourceRequest: EngineResource): ResultResource = resourceRequest match {
    case time: UserTimeoutEngineResource =>
      //Only allow up to 30 seconds to request resources(只允许使用最多30秒的时间，用于请求资源)
      val timeout = math.max(5000, math.min(time.getTimeout / 5, 30000))  //TODO Whether it takes 30 seconds to make parameters(30秒是否需要做成参数)
      rmClient.requestResource(time.getUser, time.getCreator, resourceRequest.getResource, timeout)
    case user: UserEngineResource =>
      rmClient.requestResource(user.getUser, user.getCreator, resourceRequest.getResource)
  }
}