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

package com.webank.wedatasphere.linkis.resourcemanager.domain

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.resourcemanager.Resource

/**
  * Created by shanhuang on 9/11/18.
  */
/**
  *
  * @param moduleInstance :EM ID
  * @param engineInstance :Engine ID
  * @param creator
  * @param preUsed
  * @param used
  */
case class UserModuleRecord(moduleInstance: ServiceInstance, engineInstance: ServiceInstance, creator: String, preUsed: Resource, used: Resource)

//user ==> tickID -->UserModuleRecord
case class UserResourceRecord(user: String, resourceRecord: scala.collection.mutable.Map[String, UserModuleRecord])
