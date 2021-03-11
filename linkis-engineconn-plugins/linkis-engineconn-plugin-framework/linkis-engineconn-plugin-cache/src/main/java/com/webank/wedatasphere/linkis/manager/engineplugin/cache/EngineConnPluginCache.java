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

package com.webank.wedatasphere.linkis.manager.engineplugin.cache;

import com.webank.wedatasphere.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import com.webank.wedatasphere.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;


public interface EngineConnPluginCache {
    /**
     * Put into cache
     * @param pluginInfo hold the label and resource version
     * @param pluginInstance plugin instance
     */
    void put(EngineConnPluginInfo pluginInfo, EngineConnPluginInstance pluginInstance) throws Exception;

    /**
     * Get from the cache, if not exist invoke the getter
     * @param pluginInfo hold the label and resource version
     * @param getter getter
     */
    EngineConnPluginInstance get(EngineConnPluginInfo pluginInfo, PluginGetter getter) throws Exception;

    /**
     * Remove from the cache
     * @param pluginInfo info
     * @return the previous plugin instance
     */
    EngineConnPluginInstance remove(EngineConnPluginInfo pluginInfo) throws Exception;

    @FunctionalInterface
    interface PluginGetter {
        /**
         * Call method
         * @param pluginInfo plugin info
         * @return instance
         */
        EngineConnPluginInstance call(EngineConnPluginInfo pluginInfo) throws Exception;
    }
}
