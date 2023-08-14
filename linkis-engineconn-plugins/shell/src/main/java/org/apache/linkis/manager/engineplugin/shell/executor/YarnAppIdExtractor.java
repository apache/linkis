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

package org.apache.linkis.manager.engineplugin.shell.executor;

import org.apache.linkis.engineconn.common.conf.EngineConnConf;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YarnAppIdExtractor {

  private final Set<String> appIdList = Collections.synchronizedSet(new HashSet<>());

  private final String regex =
      EngineConnConf.SPARK_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX().getValue();
  private final Pattern pattern = Pattern.compile(regex);

  public void appendLineToExtractor(String content) {
    if (StringUtils.isBlank(content)) {
      return;
    }
    Matcher yarnAppIDMatcher = pattern.matcher(content);
    if (yarnAppIDMatcher.find()) {
      String yarnAppID = yarnAppIDMatcher.group(2);
      appIdList.add(yarnAppID);
    }
  }

  public List<String> getExtractedYarnAppIds() {
    synchronized (appIdList) {
      return new ArrayList<>(appIdList);
    }
  }
}
