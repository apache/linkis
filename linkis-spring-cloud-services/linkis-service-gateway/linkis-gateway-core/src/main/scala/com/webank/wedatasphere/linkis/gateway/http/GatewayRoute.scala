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

package com.webank.wedatasphere.linkis.gateway.http

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.server.JMap

/**
  * created by cooperyang on 2019/1/9.
  */
class GatewayRoute {

  private var requestURI: String = _
  private var serviceInstance: ServiceInstance = _
  private var params: JMap[String, String] = new JMap[String, String]
  private var routeLabels: util.List[_ <:Label[_]] = _

  def setRequestURI(requestURI: String):Unit = this.requestURI = requestURI
  def getRequestURI:String = requestURI
  def setServiceInstance(serviceInstance: ServiceInstance):Unit = this.serviceInstance = serviceInstance
  def getServiceInstance:ServiceInstance = serviceInstance
  def setParams(params: JMap[String, String]):Unit = this.params = params
  def getParams:JMap[String, String] = params
  def getLabels:util.List[_ <:Label[_]] = routeLabels
  def setLabels(labels: util.List[_ <:Label[_]]):Unit = this.routeLabels = labels
}
