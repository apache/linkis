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

import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlDownloadResponse;
import org.apache.linkis.bml.protocol.Version;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.engineplugin.loader.EngineConnPluginLoaderConf;
import org.apache.linkis.engineplugin.loader.loaders.EngineConnPluginsResourceLoader;
import org.apache.linkis.engineplugin.loader.utils.EngineConnPluginUtils;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginLoadResourceException;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BmlEngineConnPluginResourceLoader implements EngineConnPluginsResourceLoader {

  private static final Logger LOG =
      LoggerFactory.getLogger(BmlEngineConnPluginResourceLoader.class);

  private static final String LOAD_LOCK_FILE = ".lock-dir";
  /** BML client user */
  private String clientUser;

  /** BML client entity */
  private BmlClient bmlClient;

  private String downloadTmpDir;

  public BmlEngineConnPluginResourceLoader() {
    this(EngineConnPluginLoaderConf.ENGINE_PLUGIN_LOADER_DEFAULT_USER().getValue(), null);
  }

  public BmlEngineConnPluginResourceLoader(String clientUser, Map<String, Object> clientProps) {
    this.clientUser = clientUser;
    this.bmlClient = BmlClientFactory.createBmlClient(clientUser, clientProps);
    this.downloadTmpDir = EngineConnPluginLoaderConf.DOWNLOAD_TEMP_DIR_PREFIX().getValue();
  }

  @Override
  public PluginResource loadEngineConnPluginResource(
      EngineConnPluginInfo pluginInfo, String savePath)
      throws EngineConnPluginLoadResourceException {
    String resourceId = getResourceIdFromStorage(pluginInfo.typeLabel());
    resourceId = StringUtils.isBlank(resourceId) ? pluginInfo.resourceId() : resourceId;
    EngineTypeLabel typeLabel = pluginInfo.typeLabel();
    if (StringUtils.isNotBlank(resourceId)) {
      String resourceVersion = pluginInfo.resourceVersion();
      String maxVersion = "";
      if (StringUtils.isNotBlank(resourceVersion)) {
        List<Version> versions =
            this.bmlClient.getVersions(this.clientUser, resourceId).resourceVersions().versions();
        maxVersion =
            versions.stream().map(Version::version).max(Comparator.naturalOrder()).orElse(null);
        if (null == maxVersion) {
          LOG.trace(
              "Unable to find the versions of resourceId:[{}"
                  + "] for engine conn plugin:[name: {}, version: {}] in BML",
              resourceId,
              typeLabel.getEngineType(),
              typeLabel.getVersion());
          return null;
        }
        // Has the same version
        if (maxVersion.equals(resourceVersion)) {
          LOG.trace(
              "The version:[{}] of resourceId:[{}] for engine conn plugin:[name: {}, version: {}"
                  + "] must be latest",
              versions,
              resourceId,
              typeLabel.getEngineType(),
              typeLabel.getVersion());
          return null;
        }
        LOG.trace(
            "Start to download resource of engine conn plugin:[name: {}, version: {}]",
            typeLabel.getEngineType(),
            typeLabel.getVersion());
        // Try to download
        downloadResource(typeLabel, resourceId, maxVersion, savePath);
        // Bml doesn't need to provide urls
        return new PluginResource(resourceId, maxVersion, -1L, null);
      }
    }
    // Don't need to load resource
    return null;
  }

  private void downloadResource(
      EngineTypeLabel typeLabel, String resourceId, String version, String savePath)
      throws EngineConnPluginLoadResourceException {
    // Use FsPath instead ?
    File savePos = FileUtils.getFile(savePath);
    if (!savePos.exists() || savePos.isDirectory()) {
      File parentFile = savePos.getParentFile();
      if (null == parentFile) {
        throw new EngineConnPluginLoadResourceException(
            "Unable to build temp directory for downloading,"
                + " reason:[The parent directory of savePath doesn't exist], savePath:["
                + savePath
                + "]",
            null);
      }
      if (!parentFile.exists() || !parentFile.canWrite()) {
        throw new EngineConnPluginLoadResourceException(
            "Have no write permission to directory:[" + parentFile.getAbsolutePath() + "]", null);
      }
      String tmpPath = parentFile.getAbsolutePath();
      if (!tmpPath.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR))) {
        tmpPath += IOUtils.DIR_SEPARATOR;
      }
      // TODO maybe has the same name directory?
      tmpPath +=
          (downloadTmpDir + System.currentTimeMillis() + "-" + Thread.currentThread().getId());
      File tmpFile = FileUtils.getFile(tmpPath);
      try {
        BmlDownloadResponse downloadResponse =
            this.bmlClient.downloadResource(
                this.clientUser,
                resourceId,
                version,
                EngineConnPluginUtils.FILE_SCHEMA
                    + (FsPath.WINDOWS
                        ? IOUtils.DIR_SEPARATOR_UNIX + FilenameUtils.normalize(tmpPath, true)
                        : FilenameUtils.normalize(tmpPath, true)),
                true);
        if (null == downloadResponse || !downloadResponse.isSuccess()) {
          throw new EngineConnPluginLoadResourceException(
              "Fail to download resources of engine conn plugin:[name: "
                  + typeLabel.getEngineType()
                  + ", version: "
                  + typeLabel.getVersion()
                  + "]",
              null);
        } else {
          LOG.info(
              "Success to download resource of plugin:[name: {}, version: {}"
                  + "], start to load temp resource directory",
              typeLabel.getEngineType(),
              typeLabel.getVersion());
          // Load tmp directory
          loadTempResourceFile(tmpFile, savePos);
        }
      } finally {
        // Ensure that the temporary directory will be deleted completely
        if (tmpFile.exists()) {
          try {
            FileUtils.forceDelete(tmpFile);
          } catch (IOException e) {
            // Ignore
            LOG.warn(
                "Delete temp file fail:["
                    + tmpFile.getAbsolutePath()
                    + "], message:["
                    + e.getMessage()
                    + "]",
                e);
          }
        }
      }
    }
    LOG.error("Unable to download from BML to savePath:[" + savePath + "], is not a directory");
  }

  private void loadTempResourceFile(File tempFile, File saveDir)
      throws EngineConnPluginLoadResourceException {
    if (saveDir.exists()) {
      LOG.info("Clean out-of-date files in saving directory:[{}]", saveDir.getAbsolutePath());
      try {
        FileUtils.cleanDirectory(saveDir);
      } catch (IOException e) {
        throw new EngineConnPluginLoadResourceException(
            "Fail to clean out-of-date save directory:["
                + saveDir.getAbsolutePath()
                + "], please check if the directory has been broken, message:["
                + e.getMessage()
                + "]",
            e);
      }
    } else {
      saveDir.mkdir(); // Ignore
    }
    // TODO has a problem, we don't know the source file name or suffix name of tmp file
    // downloaded from BML
    LOG.info("Move tempFile:[{}] to saveDir:[{}]", tempFile.getPath(), saveDir.getPath());
    try {
      FileUtils.moveDirectory(tempFile, saveDir);
    } catch (IOException e) {
      throw new EngineConnPluginLoadResourceException(
          "Fail to move tempFile:["
              + tempFile.getPath()
              + "] to saveDir:["
              + saveDir.getPath()
              + "], message:["
              + e.getMessage()
              + "]",
          e);
    }
  }

  private String getResourceIdFromStorage(EngineTypeLabel typeLabel) {
    // TODO get resourceId from database by engineTypeLabel?
    return null;
  }
}
