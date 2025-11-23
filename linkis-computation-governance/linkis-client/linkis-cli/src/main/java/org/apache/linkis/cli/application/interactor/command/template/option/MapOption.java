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

import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MapOption extends BaseOption<Map<String, String>> implements Cloneable {
  final String[] paramNames;

  public MapOption(
      String keyPrefix, String key, String[] paramNames, String description, boolean isOptional) {
    super(keyPrefix, key, description, isOptional, null, null);
    this.paramNames = paramNames;
    super.value = new HashMap<>();
  }

  public MapOption(
      String keyPrefix,
      String key,
      String[] paramNames,
      String description,
      boolean isOptional,
      Map<String, String> value) {
    super(keyPrefix, key, description, isOptional, null, null);
    this.paramNames = paramNames;
    super.value = value;
  }

  @Override
  public String toString() {
    Map<String, String> defaultValue = this.value;
    String description = this.getDescription();
    StringBuilder sb = new StringBuilder();
    sb.append("\t")
        .append(StringUtils.join(paramNames, "|"))
        .append(" <")
        .append(defaultValue.getClass().getSimpleName())
        .append(">")
        .append(System.lineSeparator());

    sb.append("\t\t").append(description).append(System.lineSeparator());

    sb.append("\t\tdefault by: ").append("null").append(System.lineSeparator());

    sb.append("\t\toptional:").append(isOptional());

    return sb.toString();
  }

  public String[] getParamNames() {
    return paramNames;
  }

  @Override
  public void setValueWithStr(String value) throws IllegalArgumentException {
    try {
      super.rawVal += "#@#" + value;
      this.value = parseKvAndPut(this.value, value);
      this.hasVal = true;
    } catch (Throwable e) {
      throw new IllegalArgumentException(e);
    }
  }

  private Map<String, String> parseKvAndPut(Map<String, String> kvMap, String rawVal) {
    rawVal = rawVal.trim();
    int index = rawVal.indexOf("=");
    if (index != -1) {
      String key = rawVal.substring(0, index).trim();
      String value = StringUtils.strip(rawVal.substring(index + 1).trim(), " \"");
      if (kvMap.containsKey(key) && !StringUtils.equals(kvMap.get(key), value)) {
        String msg =
            MessageFormat.format(
                "Multiple values for same key were found! Option: \"{0}\", key: \"{1}\"",
                this.getParamName(), key);
        throw new IllegalArgumentException(msg);
      }
      kvMap.put(key, value);
    } else {
      throw new CommandException(
          "CMD0021",
          ErrorLevel.ERROR,
          CommonErrMsg.ParserParseErr,
          "Illegal Input: "
              + rawVal
              + ". Input should be a Map-entry described by kv-pairs. e.g. key1=value1");
    }
    return kvMap;
  }

  @Override
  public String getParamName() {
    return StringUtils.join(paramNames, "|");
  }

  @Override
  public MapOption clone() throws CloneNotSupportedException {
    MapOption ret = (MapOption) super.clone();
    ret.value = new HashMap<>(this.value);
    return ret;
  }
}
