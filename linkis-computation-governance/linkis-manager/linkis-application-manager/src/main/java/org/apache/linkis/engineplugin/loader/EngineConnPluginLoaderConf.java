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

package org.apache.linkis.engineplugin.loader;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;

public class EngineConnPluginLoaderConf {

  public static final CommonVars<String> ENGINE_PLUGIN_LOADER_DEFAULT_USER =
      CommonVars.apply("wds.linkis.engineconn.plugin.loader.defaultUser", "hadoop");

  public static final CommonVars<String> ENGINE_PLUGIN_STORE_PATH =
      CommonVars.apply(
          "wds.linkis.engineconn.plugin.loader.store.path",
          CommonVars.apply(
                  "ENGINE_CONN_HOME",
                  Configuration.getLinkisHome() + "/lib/linkis-engineconn-plugins")
              .getValue());

  public static final CommonVars<String> ENGINE_PLUGIN_PROPERTIES_NAME =
      CommonVars.apply("wds.linkis.engineconn.plugin.loader.properties.name", "plugins.properties");

  public static final CommonVars<String> ENGINE_PLUGIN_LOADER_CACHE_REFRESH_INTERVAL =
      CommonVars.apply("wds.linkis.engineconn.plugin.loader.cache.refresh-interval", "300");

  public static final CommonVars<String> DOWNLOAD_TEMP_DIR_PREFIX =
      CommonVars.apply("wds.linkis.engineconn.plugin.loader.download.tmpdir.prefix", ".BML_TMP_");
}
