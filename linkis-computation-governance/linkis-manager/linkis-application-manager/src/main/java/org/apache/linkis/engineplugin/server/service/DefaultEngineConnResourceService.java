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

package org.apache.linkis.engineplugin.server.service;

import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlUpdateResponse;
import org.apache.linkis.bml.protocol.BmlUploadResponse;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.engineplugin.server.conf.EngineConnPluginConfiguration;
import org.apache.linkis.engineplugin.server.dao.EngineConnBmlResourceDao;
import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.server.localize.EngineConnBmlResourceGenerator;
import org.apache.linkis.engineplugin.server.localize.EngineConnLocalizeResource;
import org.apache.linkis.manager.common.protocol.bml.BmlResource;
import org.apache.linkis.manager.common.protocol.bml.BmlResource.BmlResourceVisibility;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException;
import org.apache.linkis.manager.engineplugin.common.launch.process.EngineConnResource;
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants;
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary.EN_PLUGIN_MATERIAL_SOURCE_EXCEPTION;

@Component
public class DefaultEngineConnResourceService extends EngineConnResourceService {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnResourceService.class);

  @Autowired private EngineConnBmlResourceGenerator engineConnBmlResourceGenerator;

  @Autowired private EngineConnBmlResourceDao engineConnBmlResourceDao;

  private final BmlClient bmlClient = BmlClientFactory.createBmlClient();
  private boolean isRefreshing = false;

  @PostConstruct
  @Override
  public void init() {
    if ((boolean) EngineConnPluginConfiguration.ENGINE_CONN_DIST_LOAD_ENABLE.getValue()) {
      logger.info("Start to refresh all engineconn plugins when inited.");
      refreshAll(false, false);
    }
  }

  private BmlResource uploadToBml(final EngineConnLocalizeResource localizeResource) {
    final BmlUploadResponse response =
        bmlClient.uploadResource(
            Utils.getJvmUser(), localizeResource.fileName(), localizeResource.getFileInputStream());
    final BmlResource bmlResource = new BmlResource();
    bmlResource.setResourceId(response.resourceId());
    bmlResource.setVersion(response.version());
    return bmlResource;
  }

  private BmlResource uploadToBml(
      final EngineConnLocalizeResource localizeResource, final String resourceId) {
    final BmlUpdateResponse response =
        bmlClient.updateResource(
            Utils.getJvmUser(),
            resourceId,
            localizeResource.fileName(),
            localizeResource.getFileInputStream());
    final BmlResource bmlResource = new BmlResource();
    bmlResource.setResourceId(response.resourceId());
    bmlResource.setVersion(response.version());
    return bmlResource;
  }

  @Override
  public void refreshAll(boolean iswait, boolean force) {
    if (!isRefreshing) {
      synchronized (this) {
        if (!isRefreshing) {

          final Runnable refreshTask =
              new Runnable() {
                @Override
                public void run() {
                  isRefreshing = true;
                  logger.info("Try to initialize the dist resources of all EngineConns. ");
                  String[] engineConnTypeList =
                      engineConnBmlResourceGenerator.getEngineConnTypeListFromDisk();
                  for (String engineConnType : engineConnTypeList) {
                    try {
                      logger.info(
                          "Try to initialize all versions of {}EngineConn.", engineConnType);
                      Map<String, EngineConnLocalizeResource[]> version2Localize =
                          engineConnBmlResourceGenerator.generate(engineConnType);
                      for (Map.Entry<String, EngineConnLocalizeResource[]> entry :
                          version2Localize.entrySet()) {
                        logger.info(
                            "Try to initialize {}EngineConn-{}.", engineConnType, entry.getKey());
                        refresh(entry.getValue(), engineConnType, entry.getKey(), force);
                      }

                    } catch (Exception t) {
                      if (!iswait
                          && EngineConnPluginConfiguration.ENABLED_BML_UPLOAD_FAILED_EXIT
                              .getValue()) {
                        logger.error("Failed to upload engine conn to bml, now exit!", t);
                        System.exit(1);
                      }
                      logger.error("Failed to upload engine conn to bml", t);
                    }
                  }
                  isRefreshing = false;
                }
              };
          Future<?> future = Utils.defaultScheduler().submit(refreshTask);

          if (iswait) {
            try {
              future.get();
            } catch (InterruptedException | ExecutionException e) {
              logger.info("DefaultEngineConnResourceService refreshTask execution failed", e);
            }
          } else {
            logger.info("DefaultEngineConnResourceService IsRefreshing EngineConns...");
          }
        }
      }
    }
  }

  @Receiver
  public boolean refreshAll(final RefreshAllEngineConnResourceRequest engineConnRefreshAllRequest) {
    logger.info("Start to refresh all engineconn plugins.");
    refreshAll(true, false);
    return true;
  }

  @Receiver
  @Override
  public boolean refresh(
      final RefreshEngineConnResourceRequest engineConnRefreshRequest, final boolean force) {
    final String engineConnType = engineConnRefreshRequest.getEngineConnType();
    final String version = engineConnRefreshRequest.getVersion();
    if ("*".equals(version) || StringUtils.isEmpty(version)) {
      logger.info("Try to refresh all versions of {}EngineConn.", engineConnType);
      Map<String, EngineConnLocalizeResource[]> version2Localize =
          engineConnBmlResourceGenerator.generate(engineConnType);
      for (Map.Entry<String, EngineConnLocalizeResource[]> entry : version2Localize.entrySet()) {
        logger.info("Try to initialize {}EngineConn-{}.", engineConnType, entry.getKey());
        refresh(entry.getValue(), engineConnType, entry.getKey(), force);
      }

    } else {
      logger.info("Try to refresh {}EngineConn-{}.", engineConnType, version);
      EngineConnLocalizeResource[] localize =
          engineConnBmlResourceGenerator.generate(engineConnType, version);
      refresh(localize, engineConnType, version, force);
    }
    return true;
  }

  private void refresh(
      final EngineConnLocalizeResource[] localize,
      final String engineConnType,
      final String version,
      final boolean force) {
    final List<EngineConnBmlResource> engineConnBmlResources =
        engineConnBmlResourceDao.getAllEngineConnBmlResource(engineConnType, version);

    if (Stream.of(localize)
            .filter(
                localizeResource ->
                    StringUtils.equals(
                            LaunchConstants.ENGINE_CONN_CONF_DIR_NAME() + ".zip",
                            localizeResource.fileName())
                        || StringUtils.equals(
                            LaunchConstants.ENGINE_CONN_LIB_DIR_NAME() + ".zip",
                            localizeResource.fileName()))
            .count()
        < 2) {

      throw new EngineConnPluginErrorException(
          EngineconnCoreErrorCodeSummary.LIB_CONF_DIR_NECESSARY.getErrorCode(),
          MessageFormat.format(
              EngineconnCoreErrorCodeSummary.LIB_CONF_DIR_NECESSARY.getErrorDesc(),
              engineConnType));
    }

    for (EngineConnLocalizeResource localizeResource : localize) {

      Optional<EngineConnBmlResource> resource =
          engineConnBmlResources.stream()
              .filter(r -> r.getFileName().equals(localizeResource.fileName()))
              .findFirst();
      if (!resource.isPresent()) {
        logger.info(
            "Ready to upload a new bmlResource for {}EngineConn-{}. path: {}",
            engineConnType,
            version,
            localizeResource.fileName());
        final BmlResource bmlResource = uploadToBml(localizeResource);
        final EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
        engineConnBmlResource.setBmlResourceId(bmlResource.getResourceId());
        engineConnBmlResource.setBmlResourceVersion(bmlResource.getVersion());
        engineConnBmlResource.setCreateTime(new Date());
        engineConnBmlResource.setLastUpdateTime(new Date());
        engineConnBmlResource.setEngineConnType(engineConnType);
        engineConnBmlResource.setFileName(localizeResource.fileName());
        engineConnBmlResource.setFileSize(localizeResource.fileSize());
        engineConnBmlResource.setLastModified(localizeResource.lastModified());
        engineConnBmlResource.setVersion(version);
        engineConnBmlResourceDao.save(engineConnBmlResource);
      } else {
        boolean isChanged =
            resource.get().getFileSize() != localizeResource.fileSize()
                || resource.get().getLastModified() != localizeResource.lastModified();

        if (isChanged || (!isChanged && force)) {
          if (!isChanged && force) {
            logger.info(
                "The file has no change in {}EngineConn-{}, path: {}, but force to refresh",
                engineConnType,
                version,
                localizeResource.fileName());
          }
          logger.info(
              "Ready to upload a refreshed bmlResource for {}EngineConn-{}. path: {}",
              engineConnType,
              version,
              localizeResource.fileName());
          final EngineConnBmlResource engineConnBmlResource = resource.get();
          final BmlResource bmlResource =
              uploadToBml(localizeResource, engineConnBmlResource.getBmlResourceId());
          engineConnBmlResource.setBmlResourceVersion(bmlResource.getVersion());
          engineConnBmlResource.setLastUpdateTime(new Date());
          engineConnBmlResource.setFileSize(localizeResource.fileSize());
          engineConnBmlResource.setLastModified(localizeResource.lastModified());
          engineConnBmlResourceDao.update(engineConnBmlResource);
        } else {
          logger.info(
              "The file has no change in {}EngineConn-{}, path: {}",
              engineConnType,
              version,
              localizeResource.fileName());
        }
      }
    }
  }

  @Receiver
  @Override
  public EngineConnResource getEngineConnBMLResources(
      final GetEngineConnResourceRequest engineConnBMLResourceRequest) {
    final String engineConnType = engineConnBMLResourceRequest.getEngineConnType();
    final String version = engineConnBMLResourceRequest.getVersion();

    List<EngineConnBmlResource> engineConnBmlResources =
        engineConnBmlResourceDao.getAllEngineConnBmlResource(engineConnType, version);
    if (engineConnBmlResources.size() == 0
        && (boolean) EngineConnPluginConfiguration.EC_BML_VERSION_MAY_WITH_PREFIX_V.getValue()) {
      logger.info("Try to get engine conn bml resource with prefex v");
      engineConnBmlResourceDao.getAllEngineConnBmlResource(engineConnType, "v" + version);
    }

    Optional<BmlResource> confBmlResourceMap =
        engineConnBmlResources.stream()
            .filter(
                r -> r.getFileName().equals(LaunchConstants.ENGINE_CONN_CONF_DIR_NAME() + ".zip"))
            .map(this::parseToBmlResource)
            .findFirst();
    Optional<BmlResource> libBmlResourceMap =
        engineConnBmlResources.stream()
            .filter(
                r -> r.getFileName().equals(LaunchConstants.ENGINE_CONN_LIB_DIR_NAME() + ".zip"))
            .map(this::parseToBmlResource)
            .findFirst();

    if (!confBmlResourceMap.isPresent() || !libBmlResourceMap.isPresent()) {
      throw new EngineConnPluginErrorException(
          EN_PLUGIN_MATERIAL_SOURCE_EXCEPTION.getErrorCode(),
          EN_PLUGIN_MATERIAL_SOURCE_EXCEPTION.getErrorDesc());
    }
    final BmlResource confBmlResource = confBmlResourceMap.get();
    final BmlResource libBmlResource = libBmlResourceMap.get();
    BmlResource[] otherBmlResources =
        engineConnBmlResources.stream()
            .filter(
                r ->
                    !r.getFileName().equals(LaunchConstants.ENGINE_CONN_CONF_DIR_NAME() + ".zip")
                        || r.getFileName()
                            .equals(LaunchConstants.ENGINE_CONN_LIB_DIR_NAME() + ".zip"))
            .map(this::parseToBmlResource)
            .toArray(BmlResource[]::new);

    return new EngineConnResource() {
      @Override
      public BmlResource getConfBmlResource() {
        return confBmlResource;
      }

      @Override
      public BmlResource getLibBmlResource() {
        return libBmlResource;
      }

      @Override
      public BmlResource[] getOtherBmlResources() {
        return otherBmlResources;
      }
    };
  }

  private BmlResource parseToBmlResource(final EngineConnBmlResource engineConnBmlResource) {
    final BmlResource bmlResource = new BmlResource();
    bmlResource.setFileName(engineConnBmlResource.getFileName());
    bmlResource.setOwner(Utils.getJvmUser());
    bmlResource.setResourceId(engineConnBmlResource.getBmlResourceId());
    bmlResource.setVersion(engineConnBmlResource.getBmlResourceVersion());
    bmlResource.setVisibility(BmlResourceVisibility.Public);
    return bmlResource;
  }
}
