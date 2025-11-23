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

package org.apache.linkis.manager.label.builder.factory;

import org.apache.linkis.manager.label.builder.LabelBuilder;
import org.apache.linkis.manager.label.entity.Label;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface LabelBuilderFactory {

  /**
   * register label builder
   *
   * @param labelBuilder builder
   */
  void registerLabelBuilder(LabelBuilder labelBuilder);

  /**
   * Create single label
   *
   * @param inLabelKey input key
   * @param inValueObj input value obj
   * @param outLabelClass output label class (interface or entity class)
   * @param outValueTypes output value types
   * @param <T> extends Label
   * @return label
   */
  <T extends Label<?>> T createLabel(
      String inLabelKey, Object inValueObj, Class<?> outLabelClass, Type... outValueTypes);

  <T extends Label<?>> T createLabel(String inLabelKey);

  <T extends Label<?>> T createLabel(Class<T> outLabelClass);

  <T extends Label<?>> T createLabel(Class<?> outLabelClass, Type... outValueTypes);

  <T extends Label<?>> T createLabel(String inLabelKey, Object inValueObj);

  <T extends Label<?>> T createLabel(String inLabelKey, Object inValueObj, Class<T> outLabelClass);

  /**
   * Create single label
   *
   * @param inLabelKey input key
   * @param inValueStream input stream
   * @param outLabelClass output label class (interface or entity class)
   * @param outValueTypes output value type
   * @param <T> extends Label
   * @return label
   */
  <T extends Label<?>> T createLabel(
      String inLabelKey, InputStream inValueStream, Class<?> outLabelClass, Type... outValueTypes);

  <T extends Label<?>> T createLabel(
      String inLabelKey, InputStream inValueStream, Class<T> outLabelClass);

  <T extends Label<?>> T createLabel(String inLabelKey, InputStream inValueStream);

  /**
   * Parse map to label list
   *
   * @param inMap input map
   * @param outLabelClass output label class (interface or entity class)
   * @param outValueTypes output value type
   * @param <T> extends Label
   * @return
   */
  <T extends Label<?>> List<T> getLabels(
      Map<String, Object> inMap, Class<?> outLabelClass, Type... outValueTypes);

  <T extends Label<?>> List<T> getLabels(Map<String, Object> inMap, Class<T> outLabelType);

  <T extends Label<?>> List<T> getLabels(Map<String, Object> inMap);

  /**
   * Convert label
   *
   * @param sourceLabel source label
   * @param targetLabelClass target label (interface or entity class)
   * @param targetValueTypes target type
   * @param <T> extends Label
   * @return
   */
  <T extends Label<?>> T convertLabel(
      Label<?> sourceLabel, Class<?> targetLabelClass, Type... targetValueTypes);

  <T extends Label<?>> T convertLabel(Label<?> sourceLabel, Class<T> targetLabelClass);
}
