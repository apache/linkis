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
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._

/**
  * Created by shanhuang on 9/11/18.
  */
case class ModuleInstanceRecord(moduleName: String, start: Int, locked: Int)

object ModuleInstanceSerializer extends CustomSerializer[ServiceInstance](implicit formats => ( {
  case JObject(List(("moduleName", moduleName), ("instance", instance))) =>
    ServiceInstance(moduleName.extract[String], instance.extract[String])
}, {
  case i: ServiceInstance =>
    ("moduleName", i.getApplicationName) ~ ("instance", i.getInstance)
})
)