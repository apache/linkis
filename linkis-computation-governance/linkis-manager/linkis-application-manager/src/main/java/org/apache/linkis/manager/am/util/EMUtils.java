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

package org.apache.linkis.manager.am.util;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.governance.common.protocol.conf.TenantRequest;
import org.apache.linkis.governance.common.protocol.conf.TenantResponse;
import org.apache.linkis.manager.am.vo.ConfigVo;
import org.apache.linkis.rpc.Sender;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EMUtils {

  private static Logger logger = LoggerFactory.getLogger(EMUtils.class);

  public static String getTenant(String username, String creator) {
    Sender sender =
        Sender.getSender(
            Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue());
    TenantResponse response = (TenantResponse) sender.ask(new TenantRequest(username, creator));
    if (StringUtils.isBlank(response.tenant())) {
      response = (TenantResponse) sender.ask(new TenantRequest(username, "*"));
      if (StringUtils.isBlank(response.tenant())) {
        response = (TenantResponse) sender.ask(new TenantRequest("*", creator));
      }
    }
    return response.tenant();
  }

  public static List<ConfigVo> getUserConf(String username, String creator, String engineType) {
    // 获取用户配置信息
    List<ConfigVo> configlist = new ArrayList<>();
    try {
      String url =
          MessageFormat.format(
              "/api/rest_j/v1/configuration/getFullTreesByAppName?creator={0}&engineType={1}",
              creator, engineType);
      HttpGet httpGet = new HttpGet(Configuration.getGateWayURL() + url);
      httpGet.addHeader("Token-User", username);
      httpGet.addHeader("Token-Code", "BML-AUTH");
      String responseStr =
          EntityUtils.toString(HttpClients.createDefault().execute(httpGet).getEntity());
      JsonNode fullTree = new ObjectMapper().readTree(responseStr).get("data").get("fullTree");
      for (JsonNode node : fullTree) {
        JsonNode settingsList = node.get("settings");
        for (JsonNode key : settingsList) {
          configlist.add(
              new ConfigVo(
                  key.get("key").asText(),
                  key.get("configValue").asText(),
                  key.get("defaultValue").asText()));
        }
      }
    } catch (IOException e) {
      logger.error("获取用户配置信息失败(Failed to obtain user configuration information)");
    }
    return configlist;
  }

  public static String getConfValue(List<ConfigVo> configVoList, String confKey) {
    String confValue = "";
    for (ConfigVo configVo : configVoList) {
      if (configVo.getKey().equals(confKey)) {
        confValue = configVo.getConfigValue();
        if (StringUtils.isBlank(confValue)) {
          confValue = configVo.getDefaultValue();
        }
      }
    }
    return removeUnit(confValue);
  }

  public static String removeUnit(String input) {
    // 使用正则表达式匹配数字和单位，然后仅保留数字部分
    Pattern pattern = Pattern.compile("(\\d+)([gG])");
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return input;
    }
  }
}
