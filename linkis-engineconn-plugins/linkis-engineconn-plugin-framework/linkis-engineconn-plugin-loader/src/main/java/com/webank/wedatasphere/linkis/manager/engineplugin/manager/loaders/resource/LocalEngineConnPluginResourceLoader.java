/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.engineplugin.manager.loaders.resource;

import com.webank.wedatasphere.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import com.webank.wedatasphere.linkis.manager.engineplugin.manager.loaders.EngineConnPluginsResourceLoader;
import com.webank.wedatasphere.linkis.manager.engineplugin.manager.utils.EngineConnPluginUtils;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;


public class LocalEngineConnPluginResourceLoader implements EngineConnPluginsResourceLoader {

    private static final Logger LOG = LoggerFactory.getLogger(LocalEngineConnPluginResourceLoader.class);

    @Override
    public PluginResource loadEngineConnPluginResource(EngineConnPluginInfo pluginInfo, String savePath) {
        File savePos = FileUtils.getFile(savePath);
        if(savePos.exists() && savePos.isDirectory()){
            long modifyTime = savePos.lastModified();
            if(modifyTime > pluginInfo.resourceUpdateTime()){
                EngineTypeLabel typeLabel = pluginInfo.typeLabel();
                List<URL> urls = EngineConnPluginUtils.getJarsUrlsOfPath(savePath);
                if(urls.size() > 0){
                    LOG.info("Load local resource of engine conn plugin: [name: " + typeLabel.getEngineType() +
                            ", version: " + typeLabel.getVersion() + "] uri: [" + savePath + "]");
                }
                return new PluginResource(null, null, modifyTime, urls.toArray(new URL[0]));
            }
        }
        return null;
    }

}
