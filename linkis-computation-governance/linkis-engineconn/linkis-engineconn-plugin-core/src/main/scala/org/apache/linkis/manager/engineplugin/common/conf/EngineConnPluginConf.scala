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

package org.apache.linkis.manager.engineplugin.common.conf

import org.apache.linkis.common.conf.{ByteType, CommonVars}

object EngineConnPluginConf {

  val JAVA_ENGINE_REQUEST_MEMORY =
    CommonVars[ByteType]("wds.linkis.engineconn.java.driver.memory", new ByteType("1g"))

  val JAVA_ENGINE_REQUEST_CORES = CommonVars[Int]("wds.linkis.engineconn.java.driver.cores", 1)

  val JAVA_ENGINE_REQUEST_INSTANCE = 1

  val ENGINECONN_TYPE_NAME = CommonVars[String]("wds.linkis.engineconn.type.name", "python")

  val ENGINECONN_MAIN_CLASS = CommonVars[String](
    "wds.linkis.engineconn.main.class",
    "org.apache.linkis.engineconn.launch.EngineConnServer"
  )

}
