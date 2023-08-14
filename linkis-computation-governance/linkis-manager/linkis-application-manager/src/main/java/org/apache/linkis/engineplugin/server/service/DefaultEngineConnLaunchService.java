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
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException;
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest;
import org.apache.linkis.manager.engineplugin.common.launch.process.EngineConnResourceGenerator;
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultEngineConnLaunchService implements EngineConnLaunchService {

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnLaunchService.class);

  @Autowired private EngineConnResourceGenerator engineConnResourceGenerator;

  private EngineConnLaunchBuilder getEngineLaunchBuilder(
      EngineTypeLabel engineTypeLabel, EngineConnBuildRequest engineBuildRequest) {
    final EngineConnPluginInstance engineConnPluginInstance;
    try {
      engineConnPluginInstance =
          EngineConnPluginsLoaderFactory.getEngineConnPluginsLoader()
              .getEngineConnPlugin(engineTypeLabel);
    } catch (Exception e) {
      throw new EngineConnPluginErrorException(
          AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorCode(),
          AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorDesc());
    }
    final EngineConnLaunchBuilder builder =
        engineConnPluginInstance.plugin().getEngineConnLaunchBuilder();
    if (builder instanceof JavaProcessEngineConnLaunchBuilder) {
      ((JavaProcessEngineConnLaunchBuilder) builder)
          .setEngineConnResourceGenerator(engineConnResourceGenerator);
    }
    builder.setBuildRequest(engineBuildRequest);
    return builder;
  }

  @Override
  @Receiver
  public EngineConnLaunchRequest createEngineConnLaunchRequest(
      EngineConnBuildRequest engineBuildRequest) {
    final Optional<EngineTypeLabel> engineTypeOption =
        engineBuildRequest.labels().stream()
            .filter(label -> label instanceof EngineTypeLabel)
            .map(label -> (EngineTypeLabel) label)
            .findFirst();

    if (!engineTypeOption.isPresent()) {
      throw new EngineConnPluginErrorException(
          EngineconnCoreErrorCodeSummary.ETL_REQUESTED.getErrorCode(),
          EngineconnCoreErrorCodeSummary.ETL_REQUESTED.getErrorDesc());
    }

    final EngineTypeLabel engineTypeLabel = engineTypeOption.get();
    return LinkisUtils.tryCatch(
        () -> getEngineLaunchBuilder(engineTypeLabel, engineBuildRequest).buildEngineConn(),
        (Throwable t) -> {
          logger.error(
              String.format(
                  "Failed to createEngineConnLaunchRequest(%s)", engineBuildRequest.ticketId()),
              t);
          throw new EngineConnPluginErrorException(
              EngineconnCoreErrorCodeSummary.FAILED_CREATE_ELR.getErrorCode(),
              String.format(
                  "%s, %s",
                  EngineconnCoreErrorCodeSummary.FAILED_CREATE_ELR.getErrorDesc(),
                  ExceptionUtils.getRootCauseMessage(t)));
        });
  }
}
