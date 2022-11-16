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

package org.apache.linkis.manager.label.entity;

import org.apache.linkis.manager.label.utils.LabelUtils;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** Serialize label value to string value and override the equals method */
public abstract class SerializableLabel<T> implements Label<T> {

  /** Cache the value names, weak hashMap */
  private static final Map<Class<?>, List<String>> CACHE =
      Collections.synchronizedMap(new WeakHashMap<>());

  protected static final String VALUE_SEPARATOR = "-";

  protected static final String[] VALUE_METHOD_PREFIX = new String[] {"is", "get", "set"};

  protected static final String SET_VALUE_METHOD_PREFIX = "set";

  @JsonIgnore protected T value;

  private String stringValue;

  public abstract String getFeatureKey();

  /**
   * Serialize method
   *
   * @return serialized string
   */
  @Override
  public String getStringValue() {
    if (StringUtils.isBlank(stringValue) && null != value) {
      if (LabelUtils.isBasicType(value.getClass())) {
        this.stringValue = String.valueOf(value);
      } else if (value instanceof List) {
        this.stringValue = StringUtils.join((List<?>) value, VALUE_SEPARATOR);
      } else {
        // Use jackson default
        Map<String, String> valueMap =
            LabelUtils.Jackson.convert(value, Map.class, String.class, String.class);
        // Deprecated?
        if (StringUtils.isNotBlank(getFeatureKey())) {
          valueMap.remove(getFeatureKey());
        }
        List<String> orderedValueNames = CACHE.get(this.getClass());
        if (null == orderedValueNames) {
          orderedValueNames =
              LabelUtils.getOrderedValueNameInLabelClass(this.getClass(), VALUE_METHOD_PREFIX);
          CACHE.put(this.getClass(), orderedValueNames);
        }
        List<String> valueSerial =
            new ArrayList<>(orderedValueNames)
                .stream().map(valueMap::get).filter(Objects::nonNull).collect(Collectors.toList());
        // Match the number
        if (valueSerial.size() == orderedValueNames.size()) {
          this.stringValue = StringUtils.join(valueSerial, VALUE_SEPARATOR);
        }
      }
    }
    return this.stringValue;
  }

  /**
   * Deserialize method
   *
   * @param stringValue
   */
  protected void setStringValue(String stringValue) {
    if (StringUtils.isNotBlank(stringValue)) {
      String[] stringValueArray = stringValue.split(VALUE_SEPARATOR);
      List<String> orderedValueNames = CACHE.get(this.getClass());
      if (null == orderedValueNames) {
        orderedValueNames =
            LabelUtils.getOrderedValueNameInLabelClass(this.getClass(), VALUE_METHOD_PREFIX);
        CACHE.put(this.getClass(), orderedValueNames);
      }
      if (stringValueArray.length != orderedValueNames.size()) {
        return;
      }
      for (int i = 0; i < orderedValueNames.size(); i++) {
        String valueName = orderedValueNames.get(i);
        try {
          Method method =
              this.getClass()
                  .getMethod(
                      SET_VALUE_METHOD_PREFIX
                          + valueName.substring(0, 1).toUpperCase()
                          + valueName.substring(1),
                      String.class);
          method.invoke(this, stringValueArray[i]);
        } catch (Exception e) {
          // Ignore
        }
      }
    }
  }

  @Override
  public final String toString() {
    return "[key: "
        + getLabelKey()
        + ", value: "
        + LabelUtils.Jackson.toJson(value, null)
        + ", str: "
        + getStringValue()
        + "]";
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof SerializableLabel) {
      String otherSerialStr = other.toString();
      String serialStr = toString();
      return StringUtils.isNotBlank(serialStr) && this.toString().equals(otherSerialStr);
    }
    return super.equals(other);
  }

  @Override
  public Boolean isEmpty() {
    return null == getValue();
  }
}
