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

package org.apache.linkis.engineplugin.loader.loaders.resource;

import org.apache.linkis.engineplugin.loader.loaders.EngineConnPluginsResourceLoader;
import org.apache.linkis.engineplugin.loader.utils.EngineConnPluginUtils;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalEngineConnPluginResourceLoader implements EngineConnPluginsResourceLoader {

  private static final Logger LOG =
      LoggerFactory.getLogger(LocalEngineConnPluginResourceLoader.class);

  @Override
  public PluginResource loadEngineConnPluginResource(
      EngineConnPluginInfo pluginInfo, String savePath) {
    File savePos = FileUtils.getFile(savePath);
    if (savePos.exists() && savePos.isDirectory()) {
      long modifyTime = savePos.lastModified();
      if (modifyTime > pluginInfo.resourceUpdateTime()) {
        EngineTypeLabel typeLabel = pluginInfo.typeLabel();
        List<URL> urls = EngineConnPluginUtils.getJarsUrlsOfPath(savePath);
        if (urls.size() > 0) {
          LOG.info(
              "Load local resource of engine conn plugin: [name: {}, version: {}] uri: [{}]",
              typeLabel.getEngineType(),
              typeLabel.getVersion(),
              savePath);
        }
        return new PluginResource(null, null, modifyTime, urls.toArray(new URL[0]));
      }
    }
    return null;
  }
}
