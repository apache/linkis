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

package org.apache.linkis.engineplugin.loader

import org.apache.linkis.common.conf.{CommonVars, Configuration}

object EngineConnPluginLoaderConf {

  val ENGINE_PLUGIN_RESOURCE_ID_NAME_PREFIX: String =
    "wds.linkis.engineConn.plugin.loader.resource-id."

  val CLASS_LOADER_CLASS_NAME: CommonVars[String] =
    CommonVars("wds.linkis.engineconn.plugin.loader.classname", "")

  val ENGINE_PLUGIN_LOADER_DEFAULT_USER: CommonVars[String] =
    CommonVars("wds.linkis.engineconn.plugin.loader.defaultUser", "hadoop")

  val ENGINE_PLUGIN_STORE_PATH: CommonVars[String] = CommonVars(
    "wds.linkis.engineconn.plugin.loader.store.path",
    CommonVars[String](
      "ENGINE_CONN_HOME",
      Configuration.getLinkisHome() + "/lib/linkis-engineconn-plugins"
    ).getValue
  )

  val ENGINE_PLUGIN_PROPERTIES_NAME: CommonVars[String] =
    CommonVars("wds.linkis.engineconn.plugin.loader.properties.name", "plugins.properties")

  val ENGINE_PLUGIN_LOADER_CACHE_REFRESH_INTERVAL: CommonVars[String] =
    CommonVars("wds.linkis.engineconn.plugin.loader.cache.refresh-interval", "300")

  val DOWNLOAD_TEMP_DIR_PREFIX: CommonVars[String] =
    CommonVars("wds.linkis.engineconn.plugin.loader.download.tmpdir.prefix", ".BML_TMP_")

}
