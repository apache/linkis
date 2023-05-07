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

import org.apache.linkis.cli.application.interactor.command.template.converter.AbstractStringConverter;

import org.apache.commons.lang3.StringUtils;

public class StdOption<T> extends BaseOption<T> {
  final String[] paramNames;

  public StdOption(
      final String keyPrefix,
      final String key,
      final String[] paramNames,
      final String description,
      final boolean isOptional,
      final T defaultValue,
      final AbstractStringConverter<T> converter) {
    super(keyPrefix, key, description, isOptional, defaultValue, converter);
    this.paramNames = paramNames;
  }

  @Override
  public String toString() {
    T defaultValue = this.getDefaultValue();
    String description = this.getDescription();
    StringBuilder sb = new StringBuilder();
    sb.append("\t")
        .append(StringUtils.join(paramNames, "|"))
        .append(" <")
        .append(defaultValue.getClass().getSimpleName())
        .append(">")
        .append(System.lineSeparator());

    sb.append("\t\t").append(description).append(System.lineSeparator());

    sb.append("\t\tdefault by: ")
        .append(
            defaultValue.getClass().isArray()
                ? StringUtils.join((Object[]) defaultValue, ", ")
                : (defaultValue == null ? "" : defaultValue.toString()))
        .append(System.lineSeparator());

    sb.append("\t\toptional:").append(isOptional());

    return sb.toString();
  }

  public String[] getParamNames() {
    return paramNames;
  }

  @Override
  public String getParamName() {
    return StringUtils.join(paramNames, "|");
  }

  @Override
  public StdOption<T> clone() throws CloneNotSupportedException {
    return (StdOption<T>) super.clone();
  }
}
