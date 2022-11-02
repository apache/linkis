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

package org.apache.linkis.manager.util;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.entity.Tunple;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.utils.LabelUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersistenceUtils {

  private static final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  public static PersistenceLabel setValue(PersistenceLabel persistenceLabel) {
    Label<?> label =
        labelFactory.createLabel(persistenceLabel.getLabelKey(), persistenceLabel.getStringValue());
    if (label.getValue() instanceof Map) {
      persistenceLabel.setValue(
          LabelUtils.Jackson.fromJson(persistenceLabel.getStringValue(), Map.class));
    }
    return persistenceLabel;
  }

  public static Tunple<String, Map<String, String>> entryToTunple(PersistenceLabel label) {
    return new Tunple<>(label.getLabelKey(), label.getValue());
  }

  public static boolean valueListIsEmpty(List<Map<String, String>> valueList) {
    return CollectionUtils.isEmpty(valueList)
        || CollectionUtils.isEmpty(
            valueList.stream().filter(MapUtils::isNotEmpty).collect(Collectors.toList()));
  }

  public static List<Map<String, String>> filterEmptyValueList(
      List<Map<String, String>> valueList) {
    return valueList.stream().filter(MapUtils::isNotEmpty).collect(Collectors.toList());
  }

  public static boolean KeyValueMapIsEmpty(Map<String, Map<String, String>> keyValueMap) {
    return MapUtils.isEmpty(keyValueMap)
        || MapUtils.isEmpty(
            keyValueMap.entrySet().stream()
                .filter(e -> MapUtils.isNotEmpty(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  public static Map<String, Map<String, String>> filterEmptyKeyValueMap(
      Map<String, Map<String, String>> keyValueMap) {
    return keyValueMap.entrySet().stream()
        .filter(e -> MapUtils.isNotEmpty(e.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static boolean persistenceLabelListIsEmpty(List<PersistenceLabel> persistenceLabelList) {
    return CollectionUtils.isEmpty(persistenceLabelList)
        || CollectionUtils.isEmpty(
            persistenceLabelList.stream()
                .filter(l -> MapUtils.isNotEmpty(l.getValue()))
                .collect(Collectors.toList()));
  }

  public static List<PersistenceLabel> filterEmptyPersistenceLabelList(
      List<PersistenceLabel> persistenceLabelList) {
    return persistenceLabelList.stream()
        .filter(e -> MapUtils.isNotEmpty(e.getValue()))
        .collect(Collectors.toList());
  }
}
