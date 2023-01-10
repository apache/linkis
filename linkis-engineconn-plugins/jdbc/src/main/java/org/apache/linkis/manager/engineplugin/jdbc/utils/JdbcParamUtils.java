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

package org.apache.linkis.manager.engineplugin.jdbc.utils;

import org.apache.linkis.manager.engineplugin.jdbc.JDBCPropertiesParser;
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant;
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.jdbc.errorcode.JDBCErrorCodeSummary.*;

public class JdbcParamUtils {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcParamUtils.class);
  private static final String JDBC_MATCH_REGEX = "jdbc:\\w+://\\S+:[0-9]{2,6}(/\\S*)?";
  private static final String JDBC_H2_PROTOCOL = "jdbc:h2";

  private static final String JDBC_MYSQL_PROTOCOL = "jdbc:mysql";

  private static final String SENSITIVE_PARAM = "autoDeserialize=true";
  private static final String AUTO_DESERIALIZE = "autoDeserialize";

  private static final String APPEND_PARAMS =
      "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";

  private static final char AND_SYMBOL = '&';

  private static final String QUOTATION_MARKS = "\"";

  private static final char QUESTION_MARK = '?';

  public static String clearJdbcUrl(String url) {
    if (url.startsWith(QUOTATION_MARKS) && url.endsWith(QUOTATION_MARKS)) {
      url = url.trim();
      return url.substring(1, url.length() - 1);
    }
    return url;
  }

  public static void validateJdbcUrl(String url) {
    if (!url.matches(JDBC_MATCH_REGEX) && !url.startsWith(JDBC_H2_PROTOCOL)) {
      throw new IllegalArgumentException("JDBC url format error!" + url);
    }
  }

  public static String filterJdbcUrl(String url) {
    if (StringUtils.isBlank(url)) {
      return url;
    }
    // temporarily filter only mysql jdbc url. & Handles cases that start with JDBC
    if (!url.toLowerCase().contains(JDBC_MYSQL_PROTOCOL)) {
      return url;
    }

    // no params
    if (!url.contains(String.valueOf(QUESTION_MARK))) {
      return url;
    }
    int two = 2;

    // deal with params
    String[] items = url.split(String.valueOf(QUESTION_MARK));
    // params error: multiple question marks
    if (items.length != two) {
      LOG.warn("JDBC params error, the url is : " + url);
      return items[0];
    }

    String[] params = items[1].split("&");
    Map<String, String> paramsMap = new HashMap<>(params.length);
    for (String param : params) {
      String[] keyAndValues = param.split("=");
      // params error: key and value error
      if (keyAndValues.length != two) {
        continue;
      }
      String key = keyAndValues[0];
      String value = keyAndValues[1];
      // key and value is blank
      if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
        continue;
      }
      if (isSecurity(key, value)) {
        paramsMap.put(key, value);
      } else {
        LOG.warn("Sensitive param : {} and {}", key, value);
      }
    }
    String extraParamString =
        paramsMap.entrySet().stream()
            .map(e -> String.join("=", e.getKey(), String.valueOf(e.getValue())))
            .collect(Collectors.joining("&"));

    if (StringUtils.isBlank(extraParamString)) {
      url = items[0];
    } else {

      url = items + String.valueOf(QUESTION_MARK) + extraParamString;
    }
    if (url.endsWith(String.valueOf(QUESTION_MARK))) {
      url = url + APPEND_PARAMS;
    } else if (url.lastIndexOf(QUESTION_MARK) < 0) {
      url = url + QUESTION_MARK + APPEND_PARAMS;
    } else {
      url = url + AND_SYMBOL + APPEND_PARAMS;
    }
    LOG.info("The filtered jdbc url is: {}", url);
    return url;
  }

  private static boolean isSecurity(String key, String value) {
    return !(isNotSecurity(key) || isNotSecurity(value));
  }

  private static boolean isNotSecurity(String key) {
    return key.toLowerCase().contains("allowLoadLocalInfile".toLowerCase())
        || key.toLowerCase().contains("autoDeserialize".toLowerCase())
        || key.toLowerCase().contains("allowLocalInfile".toLowerCase())
        || key.toLowerCase().contains("allowUrlInLocalInfile".toLowerCase())
        || key.toLowerCase().contains("#".toLowerCase());
  }

  public static String getJdbcUsername(Map<String, String> properties)
      throws JDBCParamsIllegalException {
    String username =
        JDBCPropertiesParser.getString(properties, JDBCEngineConnConstant.JDBC_USERNAME, "");
    if (StringUtils.isBlank(username)) {
      throw new JDBCParamsIllegalException(
          JDBC_USERNAME_NOT_EMPTY.getErrorCode(), JDBC_USERNAME_NOT_EMPTY.getErrorDesc());
    }
    if (username.contains(AUTO_DESERIALIZE)) {
      LOG.warn("Sensitive param : {} in username field is filtered.", AUTO_DESERIALIZE);
      username = username.replace(AUTO_DESERIALIZE, "");
    }
    LOG.info("The jdbc username is: {}", username);
    return username;
  }

  public static String getJdbcPassword(Map<String, String> properties)
      throws JDBCParamsIllegalException {
    String password =
        JDBCPropertiesParser.getString(properties, JDBCEngineConnConstant.JDBC_PASSWORD, "");
    if (StringUtils.isBlank(password)) {
      throw new JDBCParamsIllegalException(
          JDBC_PASSWORD_NOT_EMPTY.getErrorCode(), JDBC_PASSWORD_NOT_EMPTY.getErrorDesc());
    }
    if (password.contains(AUTO_DESERIALIZE)) {
      LOG.warn("Sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
      password = password.replace(AUTO_DESERIALIZE, "");
    }
    return password;
  }
}
