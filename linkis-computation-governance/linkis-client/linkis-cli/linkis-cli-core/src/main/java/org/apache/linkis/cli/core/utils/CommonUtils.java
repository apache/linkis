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

package org.apache.linkis.cli.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CommonUtils {

  public static final Gson GSON =
      new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public static <T> T castStringToAny(Class<T> clazz, String val) {
    if (StringUtils.isBlank(val)) {
      return null;
    }
    T ret = null;
    if (clazz == Object.class) {
      ret = clazz.cast(val);
    } else if (clazz == String.class) {
      ret = clazz.cast(val);
    } else if (clazz == Integer.class) {
      ret = clazz.cast(Integer.parseInt(val));
    } else if (clazz == Double.class) {
      ret = clazz.cast(Double.parseDouble(val));
    } else if (clazz == Float.class) {
      ret = clazz.cast(Float.parseFloat(val));
    } else if (clazz == Long.class) {
      ret = clazz.cast(Long.parseLong(val));
    } else if (clazz == Boolean.class) {
      ret = clazz.cast(Boolean.parseBoolean(val));
    }
    return ret;
  }

  public static void doSleepQuietly(Long sleepMills) {
    try {
      Thread.sleep(sleepMills);
    } catch (Exception ignore) {
      // ignored
    }
  }

  public static Map<String, String> parseKVStringToMap(String kvStr, String separator) {
    if (StringUtils.isBlank(separator)) {
      separator = ",";
    }
    if (StringUtils.isBlank(kvStr)) {
      return null;
    }
    Map<String, String> argsProps = new HashMap<>();
    String[] args = StringUtils.splitByWholeSeparator(kvStr, separator);
    for (String arg : args) {
      int index = arg.indexOf("=");
      if (index != -1) {
        argsProps.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
      }
    }

    return argsProps;
  }
}
