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

package org.apache.linkis.manager.engineplugin.hbase;

import java.util.Map;

public class HBasePropertiesParser extends PropertiesParser {
  public static long getLong(Map<String, String> prop, String key, long defaultValue) {
    return getValue(prop, key, defaultValue, Long::parseLong);
  }

  public static int getInt(Map<String, String> prop, String key, int defaultValue) {
    return getValue(prop, key, defaultValue, Integer::parseInt);
  }

  public static boolean getBool(Map<String, String> prop, String key, boolean defaultValue) {
    return getValue(prop, key, defaultValue, "true"::equalsIgnoreCase);
  }
}
