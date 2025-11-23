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

import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.exception.LabelErrorException;

import java.io.InputStream;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adapt labelBuilder and ExtensibleLabelBuilder */
@SuppressWarnings("unchecked")
public class LabelBuilderAdapter extends AbstractGenericLabelBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(LabelBuilderAdapter.class);
  /** Actual builder */
  private LabelBuilder actualBuilder;

  public LabelBuilderAdapter(LabelBuilder labelBuilder) {
    this.actualBuilder = labelBuilder;
  }

  @Override
  public boolean canBuild(String labelKey, Class<?> labelClass) {
    // Ignore label type
    // Ignore null value to protect label builder
    return null != labelKey && this.actualBuilder.canBuild(labelKey);
  }

  @Override
  public <T extends Label<?>> T build(
      String labelKey, Object valueObj, Class<?> labelClass, Type... valueTypes)
      throws LabelErrorException {
    Label<?> label = this.actualBuilder.build(labelKey, valueObj);
    return checkLabelBuilt(label, labelClass, valueTypes) ? (T) label : null;
  }

  @Override
  public <T extends Label<?>> T build(String labelKey, Object valueObj, Class<T> labelType)
      throws LabelErrorException {
    Label<?> label = this.actualBuilder.build(labelKey, valueObj);
    return checkLabelBuilt(label, labelType) ? (T) label : null;
  }

  @Override
  public <T extends Label<?>> T build(
      String labelKey, InputStream valueInput, Class<?> labelClass, Type... valueTypes)
      throws LabelErrorException {
    Label<?> label = this.actualBuilder.build(labelKey, valueInput);
    return checkLabelBuilt(label, labelClass) ? (T) label : null;
  }

  @Override
  public <T extends Label<?>> T build(String labelKey, InputStream valueInput, Class<T> labelClass)
      throws LabelErrorException {
    Label<?> label = this.actualBuilder.build(labelKey, valueInput);
    return checkLabelBuilt(label, labelClass) ? (T) label : null;
  }

  @Override
  public int getOrder() {
    // Call labelBuilder.getOrder()
    return actualBuilder.getOrder();
  }

  /**
   * Check if the label built meet the demand
   *
   * @param labelBuilt label built
   * @param labelType label type
   * @param valueType value type
   * @return check result
   */
  private boolean checkLabelBuilt(Label<?> labelBuilt, Class<?> labelType, Type... valueType) {
    if (null == labelBuilt || !labelType.isAssignableFrom(labelBuilt.getClass())) {
      // Means that the label built doesn't meet the demand
      LOG.debug(
          "Label built doesn't have the same type as label type: ["
              + labelType.getSimpleName()
              + "], rebuild it");
      return false;
    } else if (valueType.length > 0) {
      Class<?>[] actualValueTypes = findActualLabelValueClass(labelBuilt.getClass());
      if (null != actualValueTypes) {
        for (int i = 0; i < Math.min(valueType.length, actualValueTypes.length); i++) {
          if (actualValueTypes[i] != valueType[i]) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
