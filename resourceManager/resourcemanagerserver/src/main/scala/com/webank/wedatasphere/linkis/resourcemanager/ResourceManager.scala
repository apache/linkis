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

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.resourcemanager.domain.{ModuleInfo, ModuleResourceInfo}
import com.webank.wedatasphere.linkis.resourcemanager.service.rm.DefaultResourceManager

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class ResourceManager {

  protected val rmContext: RMContext

  /**
    * The registration method is mainly used to notify all RM nodes (including the node)
    * 该注册方法，主要是用于通知所有的RM节点（包括本节点）
    *
    * @param moduleInfo
    */
  def register(moduleInfo: ModuleInfo): Unit

  /**
    * The registration method is mainly used to notify all RM nodes (including the node), and the instance is offline.
    * 该注册方法，主要是用于通知所有的RM节点（包括本节点），下线该实例
    */
  def unregister(moduleInstance: ServiceInstance): Unit

  /**
    * Whether the instance of the module has been overloaded
    * 是否该module的实例，已经使用超载了
    *
    * @param moduleInstance
    * @return
    */
  def instanceCanService(moduleInstance: ServiceInstance): Boolean

  /**
    * Request resources, if not successful, return directly
    * 请求资源，如果不成功，直接返回
    *
    * @param user
    * @param resource
    * @return
    */
  def requestResource(moduleInstance: ServiceInstance, user: String, creator: String, resource: Resource): ResultResource

  /**
    * Request resources and wait for a certain amount of time until the requested resource is met
    * 请求资源，并等待一定的时间，直到满足请求的资源
    *
    * @param user
    * @param resource
    * @param wait
    * @return
    */
  def requestResource(moduleInstance: ServiceInstance, user: String, creator: String, resource: Resource, wait: Long): ResultResource

  /**
    * When the resource is instantiated, the total amount of resources actually occupied is returned.
    * 当资源被实例化后，返回实际占用的资源总量
    *
    * @param resource
    *                       In general, resourceReleased will release the resources occupied by the user, but if the process that uses the resource does not have time to call the resourceReleased method to die, you need to unregister to release the resource.
    * @param moduleInstance 一般情况下，会由resourceReleased释放用户占用的资源，但是如果该使用资源的进程没来得及调用resourceReleased方法就死掉了，就需要unregister来释放了
    * @param realUsed
    */
  def resourceInited(resource: ResultResource, moduleInstance: ServiceInstance, realUsed: Resource, engineInstance: ServiceInstance): Unit

  /**
    * Method called when the resource usage is released
    * 当资源使用完成释放后，调用的方法
    *
    * @param resultResource
    */
  def resourceReleased(resultResource: ResultResource, moduleInstance: ServiceInstance): Unit

  /**
    * If the IP and port are empty, return the resource status of all modules of a module
    *   * Return the use of this instance resource if there is an IP and port
    *
    * @param moduleInstance
    * @return
    */
  def getModuleResourceInfo(moduleInstance: ServiceInstance): Array[ModuleResourceInfo]

}

object ResourceManager {
  val resourceManager = new DefaultResourceManager

  def getResourceManager = resourceManager
}