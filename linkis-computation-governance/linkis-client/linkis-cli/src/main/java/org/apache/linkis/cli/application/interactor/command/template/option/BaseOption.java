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

package org.apache.linkis.cli.application.interactor.command.template.option;

import org.apache.linkis.cli.application.entity.command.CmdOption;
import org.apache.linkis.cli.application.interactor.command.template.converter.AbstractStringConverter;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * Abstract StdOption for CommandTemplate. key:unique id key for an option. defaultValue takes no
 * effect other than displaying default value
 */
public abstract class BaseOption<T> implements CmdOption<T>, Cloneable {
  private final String keyPrefix;
  private final String key;
  private final String description;
  private final boolean isOptional;
  private final AbstractStringConverter<T> converter;
  private final T defaultValue;
  protected boolean hasVal = false;

  protected String rawVal = null;
  protected T value = null;

  protected BaseOption(
      final String keyPrefix,
      final String key,
      final String description,
      final boolean isOptional,
      final T defaultValue,
      final AbstractStringConverter<T> converter) {
    this.keyPrefix = keyPrefix;
    this.key = key;
    this.description = description;
    this.defaultValue = defaultValue;
    this.converter = converter;
    this.isOptional = isOptional;
  }

  /**
   * Get StdOption paramName
   *
   * @return StdOption paramName
   */
  public String getKeyPrefix() {
    return keyPrefix;
  }

  public String getKey() {
    return key;
  }

  @Override
  public abstract String getParamName();

  public String getDescription() {
    return description;
  }

  public void setValueWithStr(String value) throws IllegalArgumentException {
    if (StringUtils.isNotBlank(this.rawVal) && !StringUtils.equals(this.rawVal, value)) {
      String msg =
          MessageFormat.format(
              "Multiple Values for same option were found! Option: \"{0}\"", this.getParamName());
      throw new IllegalArgumentException(msg);
    } else {
      try {
        this.rawVal = value;
        this.value = converter.convert(value);
        this.hasVal = true;
      } catch (Throwable e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  public T getValue() {
    return this.value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public void reset() {
    value = null;
  }

  public boolean isOptional() {
    return isOptional;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  @Override
  public String getRawVal() {
    return rawVal;
  }

  public AbstractStringConverter<T> getConverter() {
    return converter;
  }

  @Override
  public BaseOption<T> clone() throws CloneNotSupportedException {
    BaseOption<T> ret = (BaseOption<T>) super.clone();
    ret.value =
        StringUtils.isBlank(rawVal) || ret.converter == null ? null : ret.converter.convert(rawVal);
    return ret;
  }

  @Override
  public boolean hasVal() {
    return hasVal;
  }
}
