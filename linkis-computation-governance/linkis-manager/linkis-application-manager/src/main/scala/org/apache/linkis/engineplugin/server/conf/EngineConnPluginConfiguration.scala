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

package org.apache.linkis.engineplugin.server.conf

import org.apache.linkis.common.conf.{CommonVars, Configuration}

object EngineConnPluginConfiguration {

  val ENGINE_CONN_HOME: CommonVars[String] = CommonVars(
    "wds.linkis.engineconn.home",
    CommonVars[String](
      "ENGINE_CONN_HOME",
      Configuration.getLinkisHome() + "/lib/linkis-engineconn-plugins"
    ).getValue
  )

  val ENGINE_CONN_DIST_LOAD_ENABLE: CommonVars[Boolean] =
    CommonVars("wds.linkis.engineconn.dist.load.enable", true)

  val ENABLED_BML_UPLOAD_FAILED_EXIT: CommonVars[Boolean] =
    CommonVars("wds.linkis.engineconn.bml.upload.failed.enable", true)

}
