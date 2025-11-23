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

package org.apache.linkis.engineplugin.spark.datacalc.service.strategy;

import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class DataSourceStrategy {

  public abstract String getJdbcUrl(
      String address, Map<String, String> paramsJson, String paramsStr);

  public abstract String defaultDriver();

  public abstract String defaultPort();

  protected String getConnectParams(String paramsStr) {
    if (StringUtils.isBlank(paramsStr)) return "";

    Map<String, ?> paramsMap = BDPJettyServerHelper.gson().fromJson(paramsStr, Map.class);
    if (paramsMap.isEmpty()) return "";

    String paramsSplitCharacter = getParamsSplitCharacter();
    String params =
        paramsMap.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(paramsSplitCharacter));
    return getParamsStartCharacter() + params;
  }

  protected String getParamsStartCharacter() {
    return "?";
  }

  protected String getParamsSplitCharacter() {
    return "&";
  }
}
