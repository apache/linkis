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

package org.apache.linkis.manager.label.utils;

import org.apache.linkis.common.utils.ClassUtils;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.linkis.manager.label.exception.LabelRuntimeException;
import org.apache.linkis.protocol.util.ImmutablePair;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelUtils {

  private static Set<String> modifiableLabelKeyList;

  public static final String COMMON_VALUE = "*";

  public static final String UNIVERSAL_LABEL_SEPARATOR = "-";

  public static Logger logger = LoggerFactory.getLogger(LabelUtils.class);

  /**
   * If is basic type
   *
   * @param clz class
   * @return
   */
  public static boolean isBasicType(Class<?> clz) {
    if (clz == null) {
      return false;
    }
    return clz.equals(String.class)
        || clz.equals(Enum.class)
        || org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(clz);
  }

  /**
   * Get ordered value names
   *
   * @param clazz class
   * @param namePrefixes prefixes
   * @return
   */
  public static List<String> getOrderedValueNameInLabelClass(
      Class<?> clazz, String[] namePrefixes) {
    Method[] methods = clazz.getDeclaredMethods();
    List<MethodWrapper> methodWrappers = new ArrayList<>();
    for (Method method : methods) {
      String methodName = method.getName();
      if (method.isAnnotationPresent(ValueSerialNum.class)) {
        ValueSerialNum position = method.getAnnotation(ValueSerialNum.class);
        if (null == namePrefixes || namePrefixes.length == 0) {
          methodWrappers.add(new MethodWrapper(methodName, position.value()));
        } else {
          for (String prefix : namePrefixes) {
            if (methodName.startsWith(prefix)) {
              methodName = methodName.substring(prefix.length());
              if (methodName.length() > 0) {
                methodWrappers.add(
                    new MethodWrapper(
                        methodName.substring(0, 1).toLowerCase() + methodName.substring(1),
                        position.value()));
                break;
              }
            }
          }
        }
      }
    }
    Collections.sort(methodWrappers);
    return methodWrappers.stream()
        .distinct()
        .map(methodWrapper -> methodWrapper.methodName)
        .collect(Collectors.toList());
  }

  private static class MethodWrapper implements Comparable<MethodWrapper> {
    String methodName;
    int order;

    MethodWrapper(String methodName, int order) {
      this.methodName = methodName;
      this.order = order;
    }

    @Override
    public int compareTo(MethodWrapper o) {
      return this.order - o.order;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MethodWrapper) {
        return this.methodName.equals(((MethodWrapper) obj).methodName);
      }
      return super.equals(obj);
    }

    @Override
    public String toString() {
      return this.methodName;
    }
  }

  public static class Jackson {
    public static final String PREFIX = "[";
    public static final String SUFFIX = "]";

    private static final ObjectMapper mapper;

    static {
      mapper = new ObjectMapper();
      // Custom the feature of serialization and deserialization
      mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
      mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
      // Enum
      mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
      mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
      // Empty beans allowed
      mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      // Ignore unknown properties
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Generate json string
     *
     * @param simpleObj object
     * @param viewModel model
     * @return string
     */
    public static String toJson(Object simpleObj, Class<?> viewModel) {
      ObjectWriter writer = mapper.writer();
      if (null != simpleObj) {
        try {
          if (null != viewModel) {
            writer = writer.withView(viewModel);
          }
          return writer.writeValueAsString(simpleObj);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(
              "Fail to process method 'toJson("
                  + simpleObj
                  + ": "
                  + simpleObj.getClass()
                  + ", "
                  + (viewModel != null ? viewModel.getSimpleName() : null)
                  + ")'",
              e);
        }
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Class<?> tClass, Class<?>... parameters) {
      if (StringUtils.isNotBlank(json)) {
        try {
          if (parameters.length > 0) {
            return (T)
                mapper.readValue(
                    json, mapper.getTypeFactory().constructParametricType(tClass, parameters));
          }
          return (T) mapper.readValue(json, tClass);
        } catch (Exception e) {
          logger.warn(
              "Fail to process method 'fromJson("
                  + (json.length() > 5 ? json.substring(0, 5) + "..." : json)
                  + ": "
                  + json.getClass()
                  + ", "
                  + tClass.getSimpleName()
                  + ": "
                  + Class.class
                  + ", ...: "
                  + Class.class
                  + ")",
              e);
          return null;
        }
      }
      return null;
    }

    public static <T> T fromJson(String json, JavaType javaType) {
      if (StringUtils.isNotBlank(json)) {
        try {
          return mapper.readValue(json, javaType);
        } catch (Exception e) {
          throw new RuntimeException(
              "Fail to process method 'fromJson("
                  + (json.length() > 5 ? json.substring(0, 5) + "..." : json)
                  + ": "
                  + json.getClass()
                  + ", "
                  + javaType.getTypeName()
                  + ": "
                  + JavaType.class
                  + ")",
              e);
        }
      }
      return null;
    }
    /**
     * Convert object using serialization and deserialization
     *
     * @param simpleObj simpleObj
     * @param tClass type class
     * @param <T> T
     * @return result
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object simpleObj, Class<?> tClass, Class<?>... parameters) {
      try {
        if (parameters.length > 0) {
          return mapper.convertValue(
              simpleObj, mapper.getTypeFactory().constructParametricType(tClass, parameters));
        }
        return (T) mapper.convertValue(simpleObj, tClass);
      } catch (Exception e) {
        throw new RuntimeException(
            "Fail to process method 'convert("
                + simpleObj
                + ": "
                + simpleObj.getClass().getSimpleName()
                + ", "
                + tClass.getSimpleName()
                + ": "
                + Class.class
                + ", ...: "
                + Class.class
                + ")",
            e);
      }
    }

    public static <T> T convert(Object simpleObj, JavaType javaType) {
      try {
        return mapper.convertValue(simpleObj, javaType);
      } catch (Exception e) {
        throw new RuntimeException(
            "Fail to process method 'convert("
                + simpleObj
                + ": "
                + simpleObj.getClass().getSimpleName()
                + ", "
                + javaType.getTypeName()
                + ": "
                + JavaType.class
                + ")",
            e);
      }
    }
  }

  /**
   * 通过Label Key判断相同的Label，保留labelListA
   *
   * @return
   */
  public static List<Label<?>> distinctLabel(List<Label<?>> labelListA, List<Label<?>> labelListB) {

    if (CollectionUtils.isEmpty(labelListA)) {
      return labelListB;
    }
    if (CollectionUtils.isEmpty(labelListB)) {
      return labelListA;
    }
    List<Label<?>> resList = new ArrayList<>();
    Set<String> labelAKeys = new HashSet<>();
    for (Label label : labelListA) {
      if (!labelAKeys.contains(label.getLabelKey())) {
        labelAKeys.add(label.getLabelKey());
        resList.add(label);
      }
    }

    for (Label label : labelListB) {
      if (!labelAKeys.contains(label.getLabelKey())) {
        resList.add(label);
      }
    }
    return resList;
  }

  public static Map<String, Object> labelsToMap(List<Label<?>> labelList)
      throws LabelRuntimeException {
    if (CollectionUtils.isEmpty(labelList)) {
      return null;
    }
    Map<String, Object> labelMap = new HashMap<>();
    for (Label<?> label : labelList) {
      if (!labelMap.containsKey(label.getLabelKey())) {
        labelMap.put(label.getLabelKey(), label.getStringValue());
      }
    }
    return labelMap;
  }

  public static List<ImmutablePair<String, String>> labelsToPairList(List<Label<?>> labelList) {
    if (CollectionUtils.isEmpty(labelList)) {
      return null;
    }
    List<ImmutablePair<String, String>> rsList = new ArrayList<>(labelList.size());
    for (Label<?> label : labelList) {
      if (null != label) {
        rsList.add(new ImmutablePair<>(label.getLabelKey(), label.getStringValue()));
      } else {
        logger.warn("LabelList contans empty label.");
      }
    }
    return rsList;
  }

  public static Set<String> listAllUserModifiableLabel() {
    if (modifiableLabelKeyList != null) {
      return modifiableLabelKeyList;
    }
    Set<Class<? extends Label>> labelSet = ClassUtils.reflections().getSubTypesOf(Label.class);
    Set<String> result = new HashSet<>();
    labelSet.stream()
        .forEach(
            label -> {
              try {
                if (!ClassUtils.isInterfaceOrAbstract(label)) {
                  Label instanceLabel = label.newInstance();
                  if (instanceLabel instanceof UserModifiable) {
                    result.add(instanceLabel.getLabelKey());
                  }
                }
              } catch (InstantiationException | IllegalAccessException e) {
                logger.info("Failed to instantiation", e);
              }
            });
    modifiableLabelKeyList = result;
    return modifiableLabelKeyList;
  }
}
