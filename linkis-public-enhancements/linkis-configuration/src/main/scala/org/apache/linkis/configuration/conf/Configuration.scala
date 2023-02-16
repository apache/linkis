/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.configuration.conf

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.manager.label.entity.engine.EngineType

import scala.collection.JavaConverters.asScalaBufferConverter

object Configuration {

  val ENGINE_TYPE = CommonVars.apply(
    "wds.linkis.configuration.engine.type",
    EngineType.getAllEngineTypes.asScala.mkString(",")
  )

  val MANAGER_SPRING_NAME =
    CommonVars("wds.linkis.engineconn.manager.name", "linkis-cg-linkismanager")

  val GLOBAL_CONF_CHN_NAME = "全局设置"

  val GLOBAL_CONF_CHN_OLDNAME = "通用设置"

  val GLOBAL_CONF_CHN_EN_NAME = "GlobalSettings"

  val GLOBAL_CONF_LABEL = "*-*,*-*"

  val USE_CREATOR_DEFAULE_VALUE =
    CommonVars.apply("wds.linkis.configuration.use.creator.default.value", true).getValue

}
