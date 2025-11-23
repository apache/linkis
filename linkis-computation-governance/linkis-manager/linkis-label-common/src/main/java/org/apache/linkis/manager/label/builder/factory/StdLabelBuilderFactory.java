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

import org.apache.linkis.manager.label.builder.*;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.exception.LabelRuntimeException;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdLabelBuilderFactory implements LabelBuilderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(StdLabelBuilderFactory.class);

  private final LinkedList<ExtensibleLabelBuilder> labelBuilders = new LinkedList<>();
  /** Mark sort operation */
  private boolean isSorted;

  public StdLabelBuilderFactory() {
    registerInnerLabelBuilder(new GenericLabelBuilder());
    registerInnerLabelBuilder(new DefaultGlobalLabelBuilder());
  }

  @Override
  public void registerLabelBuilder(LabelBuilder labelBuilder) {
    registerInnerLabelBuilder(new LabelBuilderAdapter(labelBuilder));
  }

  @Override
  public <T extends Label<?>> T createLabel(
      String inLabelKey, Object inValueObj, Class<?> outLabelClass, Type... outValueTypes) {
    return processToBuild(
        inLabelKey,
        outLabelClass,
        (builder) -> builder.build(inLabelKey, inValueObj, outLabelClass, outValueTypes));
  }

  @Override
  public <T extends Label<?>> T createLabel(String inLabelKey) {
    return createLabel(inLabelKey, (Object) null, null, (Type) null);
  }

  @Override
  public <T extends Label<?>> T createLabel(Class<T> outLabelClass) {
    return createLabel(null, (Object) null, outLabelClass, (Type) null);
  }

  @Override
  public <T extends Label<?>> T createLabel(Class<?> outLabelClass, Type... outValueTypes) {
    return createLabel(null, (Object) null, outLabelClass, outValueTypes);
  }

  @Override
  public <T extends Label<?>> T createLabel(String inLabelKey, Object inValueObj) {
    return createLabel(inLabelKey, inValueObj, null, (Type) null);
  }

  @Override
  public <T extends Label<?>> T createLabel(
      String inLabelKey, Object inValueObj, Class<T> outLabelClass) {
    return createLabel(inLabelKey, inValueObj, outLabelClass, (Type) null);
  }

  @Override
  public <T extends Label<?>> T createLabel(
      String inLabelKey, InputStream inValueStream, Class<?> outLabelClass, Type... outValueTypes) {
    return processToBuild(
        inLabelKey,
        outLabelClass,
        (builder) -> builder.build(inLabelKey, inValueStream, outLabelClass, outValueTypes));
  }

  @Override
  public <T extends Label<?>> T createLabel(
      String inLabelKey, InputStream inValueStream, Class<T> outLabelClass) {
    return createLabel(inLabelKey, inValueStream, outLabelClass, (Type) null);
  }

  @Override
  public <T extends Label<?>> T createLabel(String inLabelKey, InputStream inValueStream) {
    return createLabel(inLabelKey, inValueStream, null, (Type) null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Label<?>> List<T> getLabels(
      Map<String, Object> inMap, Class<?> outLabelClass, Type... outValueTypes) {
    return inMap.entrySet().stream()
        .map(
            entry ->
                (T) createLabel(entry.getKey(), entry.getValue(), outLabelClass, outValueTypes))
        .collect(Collectors.toList());
  }

  @Override
  public <T extends Label<?>> List<T> getLabels(Map<String, Object> inMap, Class<T> outLabelClass) {
    return getLabels(inMap, outLabelClass, (Type) null);
  }

  @Override
  public <T extends Label<?>> List<T> getLabels(Map<String, Object> inMap) {
    return getLabels(inMap, null, (Type) null);
  }

  @Override
  public <T extends Label<?>> T convertLabel(
      Label<?> sourceLabel, Class<?> targetLabelClass, Type... targetValueTypes) {
    String labelKey = sourceLabel.getLabelKey();
    Object sourceValue = sourceLabel.getValue();
    return createLabel(labelKey, sourceValue, targetLabelClass, targetValueTypes);
  }

  @Override
  public <T extends Label<?>> T convertLabel(Label<?> sourceLabel, Class<T> targetLabelClass) {
    return convertLabel(sourceLabel, targetLabelClass, (Type) null);
  }

  private synchronized void registerInnerLabelBuilder(ExtensibleLabelBuilder labelBuilder) {
    labelBuilders.addFirst(labelBuilder);
    isSorted = false;
  }

  /** Sort label builders collection */
  private synchronized void sortLabelBuilders() {
    if (!isSorted) {
      this.labelBuilders.sort(
          (left, right) -> {
            int compare = left.getOrder() - right.getOrder();
            if (compare == 0) {
              if (left.getClass().equals(DefaultGlobalLabelBuilder.class)) {
                return 1;
              } else if (right.getClass().equals(DefaultGlobalLabelBuilder.class)) {
                return -1;
              }
            }
            return compare;
          });
      isSorted = true;
    }
  }

  /**
   * Go through builders to process function
   *
   * @param labelKey label key
   * @param labelClass label class
   * @param buildFunc function
   * @param <R> result
   * @return
   */
  private <R> R processToBuild(
      String labelKey, Class<?> labelClass, LabelBuildFunction<R> buildFunc) {
    if (!isSorted) {
      sortLabelBuilders();
    }
    R functionOutput = null;
    for (ExtensibleLabelBuilder builder : labelBuilders) {
      if (builder.canBuild(labelKey, labelClass)) {
        try {
          functionOutput = buildFunc.apply(builder);
          if (null != functionOutput) {
            break;
          }
        } catch (LabelErrorException error) {
          LOG.error(
              "Error Exception in using label builder: ["
                  + builder.getClass().getSimpleName()
                  + "]",
              error);
          throw new LabelRuntimeException(
              error.getErrCode(),
              "Fail to build label, message:[" + error.getLocalizedMessage() + "]");
        }
        // If output == null, go on
      }
    }
    return functionOutput;
  }

  @FunctionalInterface
  public interface LabelBuildFunction<R> {

    R apply(ExtensibleLabelBuilder builder) throws LabelErrorException;
  }
}
