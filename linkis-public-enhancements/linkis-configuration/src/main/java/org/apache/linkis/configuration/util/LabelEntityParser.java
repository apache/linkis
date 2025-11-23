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

package org.apache.linkis.configuration.util;

import org.apache.linkis.configuration.entity.ConfigLabel;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.CombinedLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class LabelEntityParser {

  static LabelBuilderFactory labelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  public static ConfigLabel parseToConfigLabel(CombinedLabel combinedLabel) {
    ConfigLabel label = new ConfigLabel();
    label.setLabelKey(combinedLabel.getLabelKey());
    label.setStringValue(combinedLabel.getStringValue());
    label.setFeature(combinedLabel.getFeature());
    label.setLabelValueSize(combinedLabel.getValue().size());
    return label;
  }

  public static ArrayList<Label> generateUserCreatorEngineTypeLabelList(
      String username, String creator, String engineType, String version) {
    if (StringUtils.isEmpty(username)) {
      username = "*";
    }
    if (StringUtils.isEmpty(creator)) {
      creator = "*";
    }
    if (StringUtils.isEmpty(engineType)) {
      engineType = "*";
    }
    UserCreatorLabel userCreatorLabel = labelBuilderFactory.createLabel(UserCreatorLabel.class);
    userCreatorLabel.setUser(username);
    userCreatorLabel.setCreator(creator);
    EngineTypeLabel engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(engineType);
    if (!StringUtils.isEmpty(version)) {
      engineTypeLabel.setVersion(version);
    }
    ArrayList<Label> labelList = new ArrayList<>();
    labelList.add(userCreatorLabel);
    labelList.add(engineTypeLabel);
    return labelList;
  }

  public static ArrayList<Label> labelDecompile(String labelKey, String stringValue) {
    // NOTICE: a simple Decompile, too bad! don't use in other places
    String[] labelKeyList = labelKey.split("_");
    String[] stringValueList = stringValue.split(",");
    ArrayList<Label> labelList = new ArrayList<>();
    for (int i = 1, j = 1; i < labelKeyList.length; i++, j--) {
      String innerKey = labelKeyList[i];
      if (innerKey.equalsIgnoreCase("usercreator")) {
        UserCreatorLabel userCreatorLabel = labelBuilderFactory.createLabel(UserCreatorLabel.class);
        String[] innerString = stringValueList[j].split("-");
        userCreatorLabel.setUser(innerString[0]);
        userCreatorLabel.setCreator(innerString[1]);
        labelList.add(userCreatorLabel);
      } else if (innerKey.equalsIgnoreCase("enginetype")) {
        EngineTypeLabel engineTypeLabel = labelBuilderFactory.createLabel(EngineTypeLabel.class);
        String[] innerString = stringValueList[j].split("-");
        engineTypeLabel.setEngineType(innerString[0]);
        engineTypeLabel.setVersion(innerString[1]);
        labelList.add(engineTypeLabel);
      }
    }
    return labelList;
  }
}
