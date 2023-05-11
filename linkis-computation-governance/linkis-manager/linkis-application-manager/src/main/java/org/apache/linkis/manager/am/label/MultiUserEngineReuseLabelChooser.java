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

package org.apache.linkis.manager.am.label;

import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static scala.collection.JavaConverters.*;

@Component
public class MultiUserEngineReuseLabelChooser implements EngineReuseLabelChooser {
  private static final Logger logger =
      LoggerFactory.getLogger(MultiUserEngineReuseLabelChooser.class);

  private final String[] multiUserEngine =
      AMConfiguration.MULTI_USER_ENGINE_TYPES.getValue().split(",");
  private final Map<String, String> userMap = getMultiUserEngineUserMap();

  private Map<String, String> getMultiUserEngineUserMap() {
    String userJson = AMConfiguration.MULTI_USER_ENGINE_USER.getValue();
    if (StringUtils.isNotBlank(userJson)) {
      Map<String, String> userMap = BDPJettyServerHelper.gson().fromJson(userJson, Map.class);
      return userMap;
    } else {
      throw new AMErrorException(
          AMErrorCode.AM_CONF_ERROR.getErrorCode(),
          String.format(
              "Multi-user engine parameter configuration error, please check key %s",
              AMConfiguration.MULTI_USER_ENGINE_USER.key()));
    }
  }

  /**
   * Filter out UserCreator Label that supports multi-user engine
   *
   * @param labelList
   * @return
   */
  @Override
  public List<Label<?>> chooseLabels(List<Label<?>> labelList) {
    List<Label<?>> labels = new ArrayList<>(labelList);
    Optional<EngineTypeLabel> engineTypeLabelOption =
        labels.stream()
            .filter(label -> label instanceof EngineTypeLabel)
            .map(label -> (EngineTypeLabel) label)
            .findFirst();
    if (engineTypeLabelOption.isPresent()) {
      EngineTypeLabel engineTypeLabel = engineTypeLabelOption.get();
      Optional<String> maybeString =
          Stream.of(multiUserEngine)
              .filter(engineTypeLabel.getEngineType()::equalsIgnoreCase)
              .findFirst();
      Optional<UserCreatorLabel> userCreatorLabelOption =
          labels.stream()
              .filter(label -> label instanceof UserCreatorLabel)
              .map(label -> (UserCreatorLabel) label)
              .findFirst();
      if (maybeString.isPresent() && userCreatorLabelOption.isPresent()) {
        String userAdmin = userMap.get(engineTypeLabel.getEngineType());
        UserCreatorLabel userCreatorLabel = userCreatorLabelOption.get();
        logger.info(
            String.format(
                "For multi user engine to reset userCreatorLabel user %s to Admin %s",
                userCreatorLabel.getUser(), userAdmin));
        userCreatorLabel.setUser(userAdmin);
        return labels;
      }
    }
    return labelList;
  }
}
