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
package com.webank.wedatasphere.linkis.configuration.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType

import scala.collection.JavaConverters.asScalaBufferConverter


object Configuration {

  val ENGINE_TYPE = CommonVars.apply("wds.linkis.configuration.engine.type", EngineType.getAllEngineTypes.asScala.mkString(","))

  val MANAGER_SPRING_NAME = CommonVars("wds.linkis.engineconn.manager.name", "linkis-cg-linkismanager")
}
