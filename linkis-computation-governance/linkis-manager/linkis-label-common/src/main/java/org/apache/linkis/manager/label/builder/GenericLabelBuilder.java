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

package org.apache.linkis.manager.label.builder;

import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.exception.LabelErrorException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Use to build subLabels of generic label */
public class GenericLabelBuilder extends DefaultGlobalLabelBuilder {
  @Override
  public boolean canBuild(String labelKey, Class<?> labelClass) {
    return null != labelClass && GenericLabel.class.isAssignableFrom(labelClass);
  }

  @Override
  @SuppressWarnings(value = {"unchecked", "rawtypes"})
  public <T extends Label<?>> T build(
      String labelKey, Object valueObj, Class<?> labelClass, Type... valueTypes)
      throws LabelErrorException {
    Class<? extends Label> suitableLabelClass = getSuitableLabelClass(labelKey, labelClass);
    if (null != suitableLabelClass) {
      Type suitableValueType = getSuitableValueType(suitableLabelClass, valueTypes);
      if (null != suitableValueType) {
        if (null != valueObj && List.class.isAssignableFrom(valueObj.getClass())) {
          List<?> rawList = (List<?>) valueObj;
          if (!rawList.isEmpty() && Label.class.isAssignableFrom(rawList.get(0).getClass())) {
            List<GenericLabel> genericLabels = new ArrayList<>();
            for (Object rawObj : rawList) {
              genericLabels.add(
                  buildInner(
                      labelKey,
                      ((Label<?>) rawObj).getValue(),
                      suitableLabelClass,
                      suitableValueType));
            }
            return (T)
                genericLabels.stream()
                    .filter((label) -> label != null && label.getValue() != null)
                    .reduce(
                        (leftLabel, rightLabel) -> {
                          leftLabel.getValue().putAll(rightLabel.getValue());
                          return leftLabel;
                        })
                    .orElse(null);
          }
        }
        return super.buildInner(labelKey, valueObj, suitableLabelClass, suitableValueType);
      }
    }
    return null;
  }
}
