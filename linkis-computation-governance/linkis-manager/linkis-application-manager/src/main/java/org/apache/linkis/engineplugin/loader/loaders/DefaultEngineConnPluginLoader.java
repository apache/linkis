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

package org.apache.linkis.engineplugin.loader.loaders;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.engineplugin.loader.EngineConnPluginLoaderConf;
import org.apache.linkis.engineplugin.loader.classloader.EngineConnPluginClassLoader;
import org.apache.linkis.engineplugin.loader.loaders.resource.LocalEngineConnPluginResourceLoader;
import org.apache.linkis.engineplugin.loader.loaders.resource.PluginResource;
import org.apache.linkis.engineplugin.loader.utils.EngineConnPluginUtils;
import org.apache.linkis.engineplugin.loader.utils.ExceptionHelper;
import org.apache.linkis.manager.engineplugin.common.EngineConnPlugin;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginLoadException;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginNotFoundException;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEngineConnPluginLoader extends CacheablesEngineConnPluginLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultEngineConnPluginLoader.class);

  private final List<EngineConnPluginsResourceLoader> resourceLoaders = new ArrayList<>();

  private String rootStorePath;

  private String pluginPropsName;

  private static final String PLUGIN_DIR = "plugin";

  public DefaultEngineConnPluginLoader() throws ErrorException {
    // Check store path (is necessary)
    String storePath = EngineConnPluginLoaderConf.ENGINE_PLUGIN_STORE_PATH().getValue();
    if (StringUtils.isBlank(storePath)) {
      ExceptionHelper.dealErrorException(
          70061,
          "You should defined ["
              + EngineConnPluginLoaderConf.ENGINE_PLUGIN_STORE_PATH().key()
              + "] in properties file",
          null);
    }
    // The path can be uri
    try {
      URI storeUri = new URI(storePath);
      if (null != storeUri.getScheme()) {
        File storeDir = new File(storeUri);
        storePath = storeDir.getAbsolutePath();
      }
    } catch (URISyntaxException e) {
      // Ignore
    } catch (IllegalArgumentException e) {
      ExceptionHelper.dealErrorException(
          70061,
          "The value:["
              + storePath
              + "] of ["
              + EngineConnPluginLoaderConf.ENGINE_PLUGIN_STORE_PATH().key()
              + "] is incorrect",
          e);
    }
    this.rootStorePath = storePath;
    this.pluginPropsName = EngineConnPluginLoaderConf.ENGINE_PLUGIN_PROPERTIES_NAME().getValue();
    // Prepare inner loaders
    //        resourceLoaders.add(new BmlEngineConnPluginResourceLoader());
    resourceLoaders.add(new LocalEngineConnPluginResourceLoader());
  }

  @Override
  protected EngineConnPluginInstance loadEngineConnPluginInternal(
      EngineConnPluginInfo enginePluginInfo) throws Exception {
    // Build save path
    String savePath = rootStorePath;
    EngineTypeLabel typeLabel = enginePluginInfo.typeLabel();
    if (!savePath.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR))) {
      savePath += IOUtils.DIR_SEPARATOR;
    }
    savePath +=
        typeLabel.getEngineType() + IOUtils.DIR_SEPARATOR + PLUGIN_DIR + IOUtils.DIR_SEPARATOR;
    if (StringUtils.isNoneBlank(typeLabel.getVersion())) {
      savePath += typeLabel.getVersion() + IOUtils.DIR_SEPARATOR;
    }
    // Try to fetch resourceId from configuration
    /*  if (StringUtils.isBlank(enginePluginInfo.resourceId())) {
        String identify = EngineConnPluginLoaderConf.ENGINE_PLUGIN_RESOURCE_ID_NAME_PREFIX() + typeLabel.getEngineType() + "-" + typeLabel.getVersion();
        String resourceIdInConf = BDPConfiguration.get(identify);
        if (StringUtils.isNotBlank(resourceIdInConf)) {
            LOG.info("Fetch resourceId:[" + resourceIdInConf + "] from properties:[" + identify + "]");
            enginePluginInfo.resourceId_$eq(resourceIdInConf);
        }
    }*/
    EngineConnPlugin enginePlugin = null;
    // Load the resource of engine plugin
    PluginResource pluginResource = null;
    for (int i = 0; i < resourceLoaders.size(); i++) {
      PluginResource resource =
          resourceLoaders.get(i).loadEngineConnPluginResource(enginePluginInfo, savePath);
      if (null != resource) {
        if (null == pluginResource) {
          pluginResource = resource;
        } else {
          // Merge plugin resource
          pluginResource.merge(pluginResource);
        }
      }
    }
    if (null != pluginResource
        && null != pluginResource.getUrls()
        && pluginResource.getUrls().length > 0) {
      // Load engine plugin properties file
      Map<String, Object> props = readFromProperties(savePath + pluginPropsName);
      // Build engine classloader
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      EngineConnPluginClassLoader enginePluginClassLoader =
          new EngineConnPluginClassLoader(pluginResource.getUrls(), currentClassLoader);
      // Load the engine plugin object
      enginePlugin = loadEngineConnPlugin(enginePluginClassLoader, props);
      if (null != enginePlugin) {
        // Need to init engine plugin ?
        LOG.info(
            "Init engine conn plugin:[name: "
                + typeLabel.getEngineType()
                + ", version: "
                + typeLabel.getVersion()
                + "], invoke method init() ");
        initEngineConnPlugin(enginePlugin, props);
        // New another plugin info
        EngineConnPluginInfo newPluginInfo =
            new EngineConnPluginInfo(
                typeLabel,
                pluginResource.getUpdateTime(),
                pluginResource.getId(),
                pluginResource.getVersion(),
                enginePluginClassLoader);
        return new EngineConnPluginInstance(newPluginInfo, enginePlugin);
      }
    }
    throw new EngineConnPluginNotFoundException(
        "No plugin found "
            + enginePluginInfo.typeLabel().getStringValue()
            + ", please check your configuration",
        null);
  }

  /**
   * Init plugin
   *
   * @param plugin plugin instance
   * @param props parameters
   */
  private void initEngineConnPlugin(EngineConnPlugin plugin, Map<String, Object> props)
      throws EngineConnPluginLoadException {
    try {
      // Only one method
      plugin.init(props);
    } catch (Exception e) {
      throw new EngineConnPluginLoadException("Failed to init engine conn plugin instance", e);
    }
  }

  private EngineConnPlugin loadEngineConnPlugin(
      EngineConnPluginClassLoader enginePluginClassLoader, Map<String, Object> props)
      throws EngineConnPluginLoadException {
    // First try to load from spi
    EngineConnPlugin enginePlugin =
        EngineConnPluginUtils.loadSubEngineConnPluginInSpi(enginePluginClassLoader);
    if (null == enginePlugin) {
      // Second, try to find plugin class
      String pluginClass = EngineConnPluginUtils.getEngineConnPluginClass(enginePluginClassLoader);
      // Not contain plugin class
      if (StringUtils.isNotBlank(pluginClass)) {
        Class<? extends EngineConnPlugin> enginePluginClass =
            loadEngineConnPluginClass(pluginClass, enginePluginClassLoader);
        return loadEngineConnPlugin(enginePluginClass, enginePluginClassLoader, props);
      }
    }
    return null;
  }

  private EngineConnPlugin loadEngineConnPlugin(
      Class<? extends EngineConnPlugin> pluginClass,
      EngineConnPluginClassLoader enginePluginClassLoader,
      Map<String, Object> props)
      throws EngineConnPluginLoadException {
    ClassLoader storeClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(enginePluginClassLoader);
    try {
      final Constructor<?>[] constructors = pluginClass.getConstructors();
      if (constructors.length == 0) {
        throw new EngineConnPluginLoadException(
            "No public constructor in pluginClass [" + pluginClass.getName() + "]", null);
      }
      // Choose the first one
      Constructor<?> constructor = constructors[0];
      Class<?>[] parameters = constructor.getParameterTypes();
      try {
        if (constructor.getParameterCount() == 0) {
          return (EngineConnPlugin) constructor.newInstance();
        } else if (constructor.getParameterCount() == 1 && parameters[0] == Map.class) {
          return (EngineConnPlugin) constructor.newInstance(props);
        } else {
          throw new EngineConnPluginLoadException(
              "Illegal arguments in constructor of pluginClass [" + pluginClass.getName() + "]",
              null);
        }
      } catch (Exception e) {
        if (e instanceof EngineConnPluginLoadException) {
          throw (EngineConnPluginLoadException) e;
        }
        throw new EngineConnPluginLoadException(
            "Unable to construct pluginClass [" + pluginClass.getName() + "]", null);
      }
    } finally {
      Thread.currentThread().setContextClassLoader(storeClassLoader);
    }
  }

  private Class<? extends EngineConnPlugin> loadEngineConnPluginClass(
      String pluginClass, EngineConnPluginClassLoader classLoader)
      throws EngineConnPluginLoadException {
    try {
      return classLoader.loadClass(pluginClass).asSubclass(EngineConnPlugin.class);
    } catch (ClassNotFoundException e) {
      throw new EngineConnPluginLoadException("Unable to load class:[" + pluginClass + "]", e);
    }
  }

  @SuppressWarnings(value = {"unchecked", "rawtypes"})
  private Map<String, Object> readFromProperties(String propertiesFile) {
    Map<String, Object> map = new HashMap<>();
    Properties properties = new Properties();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(propertiesFile));
      properties.load(reader);
      map = new HashMap<String, Object>((Map) properties);
    } catch (IOException e) {
      // Just warn
    }
    return map;
  }
}
