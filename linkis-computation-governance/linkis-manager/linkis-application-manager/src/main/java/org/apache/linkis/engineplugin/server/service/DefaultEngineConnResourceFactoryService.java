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

import org.apache.linkis.engineplugin.server.loader.EngineConnPluginsLoaderFactory;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;
import org.apache.linkis.manager.engineplugin.common.resource.EngineResourceFactory;
import org.apache.linkis.manager.engineplugin.common.resource.EngineResourceRequest;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.stereotype.Component;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary.ETL_REQUESTED;

@Component
public class DefaultEngineConnResourceFactoryService implements EngineConnResourceFactoryService {

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnResourceFactoryService.class);

  @Override
  public EngineResourceFactory getResourceFactoryBy(EngineTypeLabel engineType) {
    final EngineConnPluginInstance engineConnPluginInstance;
    try {
      engineConnPluginInstance =
          EngineConnPluginsLoaderFactory.getEngineConnPluginsLoader()
              .getEngineConnPlugin(engineType);
    } catch (Exception e) {
      logger.warn("getResourceFactory failed engineType:{}", engineType, e);
      throw new AMErrorException(
          AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorCode(),
          AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorDesc());
    }
    return engineConnPluginInstance.plugin().getEngineResourceFactory();
  }

  @Override
  @Receiver
  public NodeResource createEngineResource(final EngineResourceRequest engineResourceRequest) {
    logger.info(String.format("To invoke createEngineResource %s", engineResourceRequest));
    final Optional<EngineTypeLabel> engineTypeOption =
        engineResourceRequest.labels().stream()
            .filter(label -> label instanceof EngineTypeLabel)
            .map(label -> (EngineTypeLabel) label)
            .findFirst();

    if (!engineTypeOption.isPresent()) {
      throw new EngineConnPluginErrorException(
          ETL_REQUESTED.getErrorCode(), ETL_REQUESTED.getErrorDesc());
    }

    final EngineTypeLabel engineTypeLabel = engineTypeOption.get();
    return getResourceFactoryBy(engineTypeLabel).createEngineResource(engineResourceRequest);
  }
}
