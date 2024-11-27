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

package org.apache.linkis.ecm.core.conf

import org.apache.linkis.common.conf.CommonVars

object ContainerizationConf {

  val ENGINE_CONN_CONTAINERIZATION_MAPPING_STATTIC_PORT_RANGE =
    CommonVars("linkis.engine.containerization.static.port.range", "1-65535")

  val ENGINE_CONN_CONTAINERIZATION_ENABLE =
    CommonVars("linkis.engine.containerization.enable", false).getValue

  val ENGINE_CONN_CONTAINERIZATION_MAPPING_HOST =
    CommonVars("linkis.engine.containerization.mapping.host", "")

  val ENGINE_CONN_CONTAINERIZATION_MAPPING_PORTS =
    CommonVars("linkis.engine.containerization.mapping.ports", "")

  val ENGINE_CONN_CONTAINERIZATION_MAPPING_STRATEGY =
    CommonVars("linkis.engine.containerization.mapping.strategy", "static")

  // 引擎类型-需要开启的端口数量
  // Engine Type - Number of Ports Required to Be Opened
  val ENGINE_CONN_CONTAINERIZATION_ENGINE_LIST =
    CommonVars("linkis.engine.containerization.engine.list", "spark-2,")

}
