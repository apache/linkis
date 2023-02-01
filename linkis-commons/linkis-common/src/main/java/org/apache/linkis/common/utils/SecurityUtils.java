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

package org.apache.linkis.common.utils;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.CommonVars$;
import org.apache.linkis.common.exception.LinkisSecurityException;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SecurityUtils {

  private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

  private static final String COMMA = ",";

  private static final String EQUAL_SIGN = "=";

  private static final String AND_SYMBOL = "&";

  private static final String QUESTION_MARK = "?";

  /** allowLoadLocalInfile,allowLoadLocalInfiled,# */
  public static final CommonVars<String> MYSQL_SENSITIVE_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.mysql.sensitive.params",
          "allowLoadLocalInfile,autoDeserialize,allowLocalInfile,allowUrlInLocalInfile,#");

  /**
   * "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false"
   */
  public static final CommonVars<String> MYSQL_FORCE_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.mysql.force.params",
          "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false");

  public static final CommonVars<String> MYSQL_STRONG_SECURITY_ENABLE =
      CommonVars$.MODULE$.apply("linkis.mysql.strong.security.enable", "false");

  /**
   * mysql url append force params
   *
   * @param url
   * @return
   */
  public static String appendMysqlForceParams(String url) {
    if (StringUtils.isBlank(url)) {
      return "";
    }

    String extraParamString = MYSQL_FORCE_PARAMS.getValue();

    if (url.endsWith(QUESTION_MARK)) {
      url = url + extraParamString;
    } else if (url.lastIndexOf(QUESTION_MARK) < 0) {
      url = url + QUESTION_MARK + extraParamString;
    } else {
      url = url + AND_SYMBOL + extraParamString;
    }
    return url;
  }

  public static void appendMysqlForceParams(Map<String, Object> extraParams) {
    extraParams.putAll(parseMysqlUrlParamsToMap(MYSQL_FORCE_PARAMS.getValue()));
  }

  public static String checkJdbcSecurity(String url) {
    logger.info("checkJdbcSecurity origin url: {}", url);
    if (StringUtils.isBlank(url)) {
      throw new LinkisSecurityException(35000, "Invalid mysql connection cul, url is empty");
    }
    // deal with url encode
    try {
      url = URLDecoder.decode(url, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new LinkisSecurityException(35000, "mysql connection cul decode error: " + e);
    }
    if (url.endsWith(QUESTION_MARK) || !url.contains(QUESTION_MARK)) {
      logger.info("checkJdbcSecurity target url: {}", url);
      return url;
    }
    String[] items = url.split("\\?");
    if (items.length != 2) {
      logger.warn("Invalid url: {}", url);
      throw new LinkisSecurityException(35000, "Invalid mysql connection cul: " + url);
    }
    Map<String, Object> params = parseMysqlUrlParamsToMap(items[1]);
    Map<String, Object> securityMap = checkJdbcSecurity(params);
    String paramUrl = parseParamsMapToMysqlParamUrl(securityMap);
    url = items[0] + QUESTION_MARK + paramUrl;
    logger.info("checkJdbcSecurity target url: {}", url);
    return url;
  }

  /**
   * check jdbc params
   *
   * @param paramsMap
   */
  public static Map<String, Object> checkJdbcSecurity(Map<String, Object> paramsMap) {
    if (paramsMap == null) {
      return new HashMap<>();
    }

    // mysql url strong security
    if (Boolean.valueOf(MYSQL_STRONG_SECURITY_ENABLE.getValue())) {
      paramsMap.clear();
      return paramsMap;
    }

    // deal with url encode
    String paramUrl = parseParamsMapToMysqlParamUrl(paramsMap);
    try {
      paramUrl = URLDecoder.decode(paramUrl, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new LinkisSecurityException(35000, "mysql connection cul decode error: " + e);
    }

    Map<String, Object> newParamsMap = parseMysqlUrlParamsToMap(paramUrl);
    paramsMap.clear();
    paramsMap.putAll(newParamsMap);

    Iterator<Map.Entry<String, Object>> iterator = paramsMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();
      if (StringUtils.isBlank(key) || value == null || StringUtils.isBlank(value.toString())) {
        logger.warn("Invalid parameter key or value is blank.");
        iterator.remove();
        continue;
      }
      if (isNotSecurity(key, value.toString())) {
        logger.warn("Sensitive param : key={} and value={}", key, value);
        throw new LinkisSecurityException(
            35000,
            "Invalid mysql connection parameters: " + parseParamsMapToMysqlParamUrl(paramsMap));
      }
    }
    return paramsMap;
  }

  public static String parseParamsMapToMysqlParamUrl(Map<String, Object> forceParams) {
    if (forceParams == null) {
      return "";
    }
    return forceParams.entrySet().stream()
        .map(e -> String.join(EQUAL_SIGN, e.getKey(), String.valueOf(e.getValue())))
        .collect(Collectors.joining(AND_SYMBOL));
  }

  private static Map<String, Object> parseMysqlUrlParamsToMap(String paramsUrl) {
    String[] params = paramsUrl.split(AND_SYMBOL);
    Map<String, Object> map = new LinkedHashMap<>(params.length);
    for (String param : params) {
      String[] item = param.split(EQUAL_SIGN);
      if (item.length != 2) {
        logger.warn("mysql force param {} error.", param);
        continue;
      }
      map.put(item[0], item[1]);
    }
    return map;
  }

  private static boolean isNotSecurity(String key, String value) {
    boolean res = true;
    String sensitiveParamsStr = MYSQL_SENSITIVE_PARAMS.getValue();
    if (StringUtils.isBlank(sensitiveParamsStr)) {
      return false;
    }
    String[] forceParams = sensitiveParamsStr.split(COMMA);
    for (String forceParam : forceParams) {
      if (isNotSecurity(key, value, forceParam)) {
        res = false;
        break;
      }
    }
    return !res;
  }

  private static boolean isNotSecurity(String key, String value, String param) {
    return key.toLowerCase().contains(param.toLowerCase())
        || value.toLowerCase().contains(param.toLowerCase());
  }
}
