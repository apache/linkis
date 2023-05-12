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

package org.apache.linkis.manager.am.conf;

import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import scala.Tuple2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultEngineConnConfigurationService implements EngineConnConfigurationService {

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnConfigurationService.class);

  @Override
  public Map<String, String> getConsoleConfiguration(List<? extends Label<?>> labelList) {
    Map<String, String> properties = new HashMap<>();

    Optional<UserCreatorLabel> userCreatorLabelOption =
        labelList.stream()
            .filter(l -> l instanceof UserCreatorLabel)
            .map(l -> (UserCreatorLabel) l)
            .findFirst();

    Optional<EngineTypeLabel> engineTypeLabelOption =
        labelList.stream()
            .filter(l -> l instanceof EngineTypeLabel)
            .map(l -> (EngineTypeLabel) l)
            .findFirst();
    userCreatorLabelOption.ifPresent(
        userCreatorLabel -> {
          engineTypeLabelOption.ifPresent(
              engineTypeLabel -> {
                Map<String, String> engineConfig =
                    LinkisUtils.tryAndWarn(
                        () ->
                            ConfigurationMapCache.engineMapCache.getCacheMap(
                                new Tuple2(userCreatorLabel, engineTypeLabel)),
                        logger);
                if (engineConfig != null) {
                  properties.putAll(engineConfig);
                }
              });
        });
    return properties;
  }
}
