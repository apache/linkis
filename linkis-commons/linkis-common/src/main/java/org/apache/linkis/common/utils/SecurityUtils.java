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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SecurityUtils {

  private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

  private static final String COMMA = ",";

  private static final String EQUAL_SIGN = "=";

  private static final String AND_SYMBOL = "&";

  private static final String QUESTION_MARK = "?";

  private static final String REGEX_QUESTION_MARK = "\\?";

  private static final int JDBC_URL_ITEM_COUNT = 2;

  /** allowLoadLocalInfile,allowLoadLocalInfiled,# */
  private static final CommonVars<String> MYSQL_SENSITIVE_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.mysql.sensitive.params",
          "allowLoadLocalInfile,autoDeserialize,allowLocalInfile,allowUrlInLocalInfile,#");

  /**
   * "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false"
   */
  private static final CommonVars<String> MYSQL_FORCE_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.mysql.force.params",
          "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false");

  private static final CommonVars<String> MYSQL_STRONG_SECURITY_ENABLE =
      CommonVars$.MODULE$.apply("linkis.mysql.strong.security.enable", "false");

  private static final CommonVars<String> MYSQL_SECURITY_CHECK_ENABLE =
      CommonVars$.MODULE$.apply("linkis.mysql.security.check.enable", "true");

  private static final CommonVars<String> MYSQL_CONNECT_URL =
      CommonVars.apply("linkis.security.mysql.url.template", "jdbc:mysql://%s:%s/%s");

  private static final CommonVars<String> JDBC_MATCH_REGEX =
      CommonVars$.MODULE$.apply(
          "linkis.mysql.jdbc.match.regex",
          "(?i)jdbc:(?i)(mysql)://([^:]+)(:[0-9]+)?(/[a-zA-Z0-9_-]*[\\.\\-]?)?");

  private static final String JDBC_MYSQL_PROTOCOL = "jdbc:mysql";

  /**
   * check mysql connection params
   *
   * @param host
   * @param port
   * @param username
   * @param password
   * @param database
   * @param extraParams
   */
  public static void checkJdbcConnParams(
      String host,
      Integer port,
      String username,
      String password,
      String database,
      Map<String, Object> extraParams) {

    // check switch
    if (!Boolean.valueOf(MYSQL_SECURITY_CHECK_ENABLE.getValue())) {
      return;
    }

    // 1. Check blank params
    if (StringUtils.isAnyBlank(host, username)) {
      logger.error(
          "Invalid mysql connection params: host: {}, username: {}, database: {}",
          host,
          username,
          database);
      throw new LinkisSecurityException(35000, "Invalid mysql connection params.");
    }

    // 2. Check url format
    String url = String.format(MYSQL_CONNECT_URL.getValue(), host.trim(), port, database.trim());
    checkUrl(url);

    // 3. Check params. Mainly vulnerability parameters. Note the url encoding
    checkParams(extraParams);
  }

  /** @param url */
  public static void checkJdbcConnUrl(String url) {

    // check switch
    if (!Boolean.valueOf(MYSQL_SECURITY_CHECK_ENABLE.getValue())) {
      return;
    }

    logger.info("jdbc url: {}", url);
    if (StringUtils.isBlank(url)) {
      throw new LinkisSecurityException(35000, "Invalid jdbc connection url.");
    }

    // temporarily only check mysql jdbc url.
    if (!url.toLowerCase().startsWith(JDBC_MYSQL_PROTOCOL)) {
      return;
    }

    String[] urlItems = url.split(REGEX_QUESTION_MARK);
    if (urlItems.length > JDBC_URL_ITEM_COUNT) {
      throw new LinkisSecurityException(35000, "Invalid jdbc connection url.");
    }

    // check url
    checkUrl(urlItems[0]);

    // check params
    if (urlItems.length == JDBC_URL_ITEM_COUNT) {
      Map<String, Object> params = parseMysqlUrlParamsToMap(urlItems[1]);
      checkParams(params);
    }
  }

  /**
   * call after checkJdbcConnUrl
   *
   * @param url
   * @return
   */
  public static String getJdbcUrl(String url) {
    // preventing NPE
    if (StringUtils.isBlank(url)) {
      return url;
    }
    // temporarily deal with only mysql jdbc url.
    if (!url.toLowerCase().startsWith(JDBC_MYSQL_PROTOCOL)) {
      return url;
    }
    String[] items = url.split(REGEX_QUESTION_MARK);
    String result = items[0];
    if (items.length == JDBC_URL_ITEM_COUNT) {
      Map<String, Object> params = parseMysqlUrlParamsToMap(items[1]);
      appendMysqlForceParams(params);
      String paramUrl = parseParamsMapToMysqlParamUrl(params);
      result += QUESTION_MARK + paramUrl;
    }
    return result;
  }

  /**
   * append force params, Should be called after the checkJdbcConnParams method
   *
   * @param url
   * @return
   */
  public static String appendMysqlForceParams(String url) {
    if (StringUtils.isBlank(url)) {
      return "";
    }
    if (!Boolean.valueOf(MYSQL_STRONG_SECURITY_ENABLE.getValue())) {
      return url;
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

  /**
   * append force params, Should be called after the checkJdbcConnParams method
   *
   * @param extraParams
   */
  public static void appendMysqlForceParams(Map<String, Object> extraParams) {
    if (Boolean.valueOf(MYSQL_STRONG_SECURITY_ENABLE.getValue())) {
      extraParams.putAll(parseMysqlUrlParamsToMap(MYSQL_FORCE_PARAMS.getValue()));
    }
  }

  public static String parseParamsMapToMysqlParamUrl(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return "";
    }
    return params.entrySet().stream()
        .map(e -> String.join(EQUAL_SIGN, e.getKey(), String.valueOf(e.getValue())))
        .collect(Collectors.joining(AND_SYMBOL));
  }

  /**
   * check url, format: jdbc:mysql://host:port/dbname
   *
   * @param url
   */
  public static void checkUrl(String url) {
    if (url != null && !url.toLowerCase().startsWith(JDBC_MYSQL_PROTOCOL)) {
      return;
    }
    Pattern regex = Pattern.compile(JDBC_MATCH_REGEX.getValue());
    Matcher matcher = regex.matcher(url);
    if (!matcher.matches()) {
      logger.info("Invalid mysql connection url: {}", url);
      throw new LinkisSecurityException(35000, "Invalid mysql connection url.");
    }
  }

  /**
   * check jdbc params
   *
   * @param paramsMap
   */
  private static void checkParams(Map<String, Object> paramsMap) {
    if (paramsMap == null || paramsMap.isEmpty()) {
      return;
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
  }

  private static Map<String, Object> parseMysqlUrlParamsToMap(String paramsUrl) {
    if (StringUtils.isBlank(paramsUrl)) {
      return new LinkedHashMap<>();
    }
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
