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

  private static final String BLACKLIST_REGEX =
      "autodeserialize|allowloadlocalinfile|allowurlinlocalinfile|allowloadlocalinfileinpath";

  // ----------------------- Generic JDBC security layer -----------------------
  // The methods below extend CVE-2023-49566 coverage from MySQL-only to every
  // JDBC driver family used by the metadata-query / datasource-manager modules.
  // They were missing previously, which left PostgreSQL/Oracle/SQLServer/DB2/
  // ClickHouse/KingBase/Greenplum/DM streaming user-supplied params straight
  // into DriverManager.getConnection with no allowlist/denylist.

  /** Master switch for the generic JDBC parameter check (independent of the MySQL switch). */
  private static final CommonVars<String> JDBC_SECURITY_CHECK_ENABLE =
      CommonVars$.MODULE$.apply("linkis.jdbc.security.check.enable", "true");

  /**
   * Parameters blocked for every driver family. The '#', '&', '?' characters block URL-injection
   * tricks that smuggle extra segments into the JDBC URL itself.
   */
  private static final CommonVars<String> JDBC_GLOBAL_BLOCKED_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.jdbc.global.blocked.params",
          "autoDeserialize,#,allowLoadLocalInfile,allowLocalInfile,allowUrlInLocalInfile");

  /**
   * Per-driver denylist. PG-family drivers (PostgreSQL, Greenplum, KingBase) reflectively
   * instantiate socketFactory/sslfactory classes -> RCE on drivers below 42.2.25 / 42.3.2. DB2's
   * clientRerouteServerListJNDIName is the original CVE-2023-49566 JNDI sink. Oracle's
   * tns_admin/trustStore can hijack TLS / TNS configuration. SQL Server's jaasConfigurationName can
   * trigger a JAAS lookup.
   */
  private static final CommonVars<String> JDBC_POSTGRES_BLOCKED_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.jdbc.postgres.blocked.params",
          "socketFactory,socketFactoryArg,sslfactory,sslfactoryarg,sslhostnameverifier,"
              + "loggerLevel,loggerFile");

  private static final CommonVars<String> JDBC_DB2_BLOCKED_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.jdbc.db2.blocked.params",
          // traceLevel / traceFile / traceDirectory / traceFileAppend let the driver write trace
          // output to arbitrary paths; blocked alongside the JNDI-related options.
          "clientRerouteServerListJNDIName,enableSeamlessFailover,JNDIName,"
              + "traceLevel,traceFile,traceDirectory,traceFileAppend");

  private static final CommonVars<String> JDBC_ORACLE_BLOCKED_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.jdbc.oracle.blocked.params",
          "oracle.net.tns_admin,javax.net.ssl.trustStore,javax.net.ssl.trustStorePassword,"
              + "oracle.net.ssl_url,javax.net.ssl.keyStore");

  private static final CommonVars<String> JDBC_SQLSERVER_BLOCKED_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.jdbc.sqlserver.blocked.params", "jaasConfigurationName,jaasApplicationName");

  /** Force-set defaults applied to every driver family. Empty map means no override. */
  private static final CommonVars<String> JDBC_POSTGRES_FORCE_PARAMS =
      CommonVars$.MODULE$.apply("linkis.jdbc.postgres.force.params", "");

  private static final CommonVars<String> JDBC_DB2_FORCE_PARAMS =
      CommonVars$.MODULE$.apply("linkis.jdbc.db2.force.params", "");

  private static final CommonVars<String> JDBC_ORACLE_FORCE_PARAMS =
      CommonVars$.MODULE$.apply("linkis.jdbc.oracle.force.params", "");

  private static final CommonVars<String> JDBC_SQLSERVER_FORCE_PARAMS =
      CommonVars$.MODULE$.apply(
          "linkis.jdbc.sqlserver.force.params", "trustServerCertificate=false");

  private static final CommonVars<String> JDBC_CLICKHOUSE_FORCE_PARAMS =
      CommonVars$.MODULE$.apply("linkis.jdbc.clickhouse.force.params", "");

  private static final CommonVars<String> JDBC_DM_FORCE_PARAMS =
      CommonVars$.MODULE$.apply("linkis.jdbc.dm.force.params", "");

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

    // 4. Check url security, especially for the possibility of malicious characters appearing on
    // the host
    try {
      while (url.contains("%")) {
        String decodedUrl = URLDecoder.decode(url, "UTF-8");
        if (decodedUrl.equals(url)) {
          // If the decomposition is the same as the original, avoid infinite loop
          break;
        }
        url = decodedUrl;
      }
    } catch (UnsupportedEncodingException e) {
      logger.error("URL decode failed: {}", e.getMessage());
      throw new LinkisSecurityException(35001, "URL decode failed.");
    }
    checkUrlIsSafe(url);
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

    // deal with url encode - loop until stable to prevent double-encoding bypass
    String paramUrl = parseParamsMapToMysqlParamUrl(paramsMap);
    try {
      while (paramUrl.contains("%")) {
        String decodedParamUrl = URLDecoder.decode(paramUrl, "UTF-8");
        if (decodedParamUrl.equals(paramUrl)) {
          break;
        }
        paramUrl = decodedParamUrl;
      }
    } catch (UnsupportedEncodingException e) {
      throw new LinkisSecurityException(35000, "mysql connection url decode error: " + e);
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

  /**
   * check url is safe
   *
   * @param url
   */
  public static void checkUrlIsSafe(String url) {
    try {
      String lowercaseURL = url.toLowerCase();

      Pattern pattern = Pattern.compile(BLACKLIST_REGEX);
      Matcher matcher = pattern.matcher(lowercaseURL);

      StringBuilder foundKeywords = new StringBuilder();
      while (matcher.find()) {
        if (foundKeywords.length() > 0) {
          foundKeywords.append(", ");
        }
        foundKeywords.append(matcher.group());
      }

      if (foundKeywords.length() > 0) {
        throw new LinkisSecurityException(
            35000, "url contains blacklisted characters: " + foundKeywords);
      }
    } catch (Exception e) {
      throw new LinkisSecurityException(35000, "error occurred during url security check: " + e);
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

  /**
   * allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false
   *
   * @return
   */
  public static Properties getMysqlSecurityParams() {
    Properties properties = new Properties();
    properties.setProperty("allowLoadLocalInfile", "false");
    properties.setProperty("autoDeserialize", "false");
    properties.setProperty("allowLocalInfile", "false");
    properties.setProperty("allowUrlInLocalInfile", "false");
    return properties;
  }

  // ----------------------- Generic JDBC API (added for CVE-2023-49566 fix-up)
  // -----------------------

  /**
   * Driver-aware replacement for the MySQL-only {@link #checkJdbcConnParams(String, Integer,
   * String, String, String, Map)}.
   *
   * <p>Validates the same invariants (non-blank host/username, URL-encode loop, denylist match on
   * both key and value) but selects the denylist from {@code driverType} instead of always using
   * the MySQL one.
   *
   * @param driverType JDBC driver family
   * @param host connection host
   * @param port connection port (nullable)
   * @param username connection username
   * @param password connection password (not inspected; only passed through)
   * @param database connection database name (nullable)
   * @param extraParams user-supplied params; will be mutated in place (decoded form replaces
   *     encoded form, sensitive entries removed) so the caller can hand the same map to {@link
   *     #buildSecureProperties}
   */
  public static void checkJdbcConnParams(
      JdbcDriverType driverType,
      String host,
      Integer port,
      String username,
      String password,
      String database,
      Map<String, Object> extraParams) {
    if (!Boolean.valueOf(JDBC_SECURITY_CHECK_ENABLE.getValue())) {
      return;
    }
    // 1. Basic blank check. Password is allowed to be blank for some drivers.
    if (StringUtils.isBlank(host) || StringUtils.isBlank(username)) {
      logger.error(
          "Invalid jdbc connection params: driverType={}, host={}, username={}, database={}",
          driverType,
          host,
          username,
          database);
      throw new LinkisSecurityException(35000, "Invalid jdbc connection params.");
    }
    // 2. Host sanity check: reject hosts that smuggle extra URL segments
    // (e.g. "host:port/evil?socketFactory=...").
    checkHostIsSafe(host);
    // 3. Database sanity check (reject URL option separators in the database segment).
    checkDatabaseIsSafe(driverType, database);
    // 4. Param denylist check (also handles URL-encoded bypass).
    checkDriverParams(driverType, extraParams);
  }

  /**
   * Build a JDBC {@link Properties} bag that is safe to pass to {@link
   * java.sql.DriverManager#getConnection(String, java.util.Properties)}.
   *
   * <p>The contract is identical to the MySQL secure-properties pattern: driver-specific force-set
   * security defaults go in first, then user/password, then user-supplied params are layered on top
   * but only if their key does not already exist (so the security defaults always win). This
   * replaces the unsafe pattern of string-concatenating extraParams onto the JDBC URL.
   */
  public static Properties buildSecureProperties(
      JdbcDriverType driverType,
      String username,
      String password,
      Map<String, Object> extraParams) {
    Properties props = new Properties();
    // 1. Driver-specific force params first — these cannot be overridden by user input.
    Map<String, Object> forceParams = getDriverForceParams(driverType);
    for (Map.Entry<String, Object> entry : forceParams.entrySet()) {
      props.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
    }
    // 2. Credentials.
    if (username != null) {
      props.setProperty("user", username);
    }
    if (password != null) {
      props.setProperty("password", password);
    }
    // 3. User params, but never overwrite the force-set keys.
    if (extraParams != null) {
      for (Map.Entry<String, Object> entry : extraParams.entrySet()) {
        if (entry.getKey() == null) {
          continue;
        }
        if (!props.containsKey(entry.getKey())) {
          props.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }
      }
    }
    return props;
  }

  /** Convenience: just the denylist lookup so callers can self-check before connecting. */
  public static List<String> getBlockedParamNames(JdbcDriverType driverType) {
    List<String> blocked = new ArrayList<>();
    Collections.addAll(blocked, parseCsv(JDBC_GLOBAL_BLOCKED_PARAMS.getValue()));
    Collections.addAll(blocked, parseCsv(getDriverBlockedConfig(driverType).getValue()));
    return blocked;
  }

  private static void checkDriverParams(JdbcDriverType driverType, Map<String, Object> paramsMap) {
    if (paramsMap == null || paramsMap.isEmpty()) {
      return;
    }
    // URL-decode loop (handles double-encoded bypass) — same trick as the MySQL path.
    String paramUrl =
        paramsMap.entrySet().stream()
            .map(e -> String.join(EQUAL_SIGN, e.getKey(), String.valueOf(e.getValue())))
            .collect(Collectors.joining(AND_SYMBOL));
    try {
      while (paramUrl.contains("%")) {
        String decoded = URLDecoder.decode(paramUrl, "UTF-8");
        if (decoded.equals(paramUrl)) {
          break;
        }
        paramUrl = decoded;
      }
    } catch (UnsupportedEncodingException e) {
      throw new LinkisSecurityException(35000, "jdbc connection url decode error: " + e);
    }
    // Rebuild the params map from the decoded form so callers see the canonical shape.
    Map<String, Object> decoded = parseParamUrlToMap(paramUrl);
    paramsMap.clear();
    paramsMap.putAll(decoded);

    // Denylist check. Match on either key or value, case-insensitive, substring match so
    // "loggerFile" still catches "loggerfile" typos and similar evasions.
    List<String> blocked = getBlockedParamNames(driverType);
    Iterator<Map.Entry<String, Object>> iterator = paramsMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();
      if (StringUtils.isBlank(key) || value == null || StringUtils.isBlank(value.toString())) {
        // Drop blank entries — they are noise.
        iterator.remove();
        continue;
      }
      if (containsAnyToken(key, value.toString(), blocked)) {
        logger.warn(
            "Sensitive jdbc param blocked: driverType={}, key={}, value={}",
            driverType,
            key,
            value);
        throw new LinkisSecurityException(
            35000, "Invalid jdbc connection parameter for driver " + driverType + ": key=" + key);
      }
    }
  }

  /**
   * Reject hosts that contain URL-meaningful characters. A malicious host like
   * "evil.com:5432/db?socketFactory=x" would otherwise smuggle params past the denylist because
   * they live in the URL rather than in extraParams.
   */
  private static void checkHostIsSafe(String host) {
    if (StringUtils.isBlank(host)) {
      return;
    }
    String trimmed = host.trim();
    if (trimmed.contains("?") || trimmed.contains("#") || trimmed.contains("&")) {
      throw new LinkisSecurityException(35000, "Host contains forbidden URL character: " + trimmed);
    }
  }

  /**
   * Reject database names that contain URL option separators. The metadata-query / datasource
   * manager SqlConnection classes interpolate the user-supplied database/instance segment directly
   * into the JDBC URL via String.format, so a value containing a separator character would be
   * parsed as an extra URL option. Block the characters that any supported driver family treats as
   * a delimiter after the database segment.
   */
  private static void checkDatabaseIsSafe(JdbcDriverType driverType, String database) {
    if (StringUtils.isBlank(database)) {
      return;
    }
    String trimmed = database.trim();
    String forbidden;
    switch (driverType) {
      case DB2:
        // DB2 URL option syntax is "jdbc:db2://h:p/db:opt1=v1;opt2=v2;".
        forbidden = ":;?#&";
        break;
      case ORACLE:
        // Oracle thin URL is "jdbc:oracle:thin:@//h:p/service" — ':' and '/' are structural, but
        // '?' / '&' would only appear if the caller is abusing the service-name slot to inject
        // query-style params. '#' is the fragment anchor and a known smuggling trick.
        forbidden = "?#&";
        break;
      case SQLSERVER:
        // SQL Server URL is "jdbc:sqlserver://h:p;databaseName=db;prop=v;" — ';' is the property
        // separator, so block it along with the usual query/fragment anchors.
        forbidden = ";?#&";
        break;
      case POSTGRESQL:
      case GREENPLUM:
      case KINGBASE:
      case CLICKHOUSE:
      case DM:
      case MYSQL:
      case STARROCKS:
      default:
        // For the mysql:// family the database is followed by '?' for query params; for the others
        // a '/' selects a path segment. Block all of the URL-meaningful characters.
        forbidden = "?#&/";
        break;
    }
    for (int i = 0; i < forbidden.length(); i++) {
      char c = forbidden.charAt(i);
      if (trimmed.indexOf(c) >= 0) {
        throw new LinkisSecurityException(
            35000,
            "Database name contains forbidden character '"
                + c
                + "' for driver "
                + driverType
                + ": "
                + trimmed);
      }
    }
  }

  private static CommonVars<String> getDriverBlockedConfig(JdbcDriverType driverType) {
    switch (driverType) {
      case POSTGRESQL:
      case GREENPLUM:
      case KINGBASE:
        return JDBC_POSTGRES_BLOCKED_PARAMS;
      case DB2:
        return JDBC_DB2_BLOCKED_PARAMS;
      case ORACLE:
        return JDBC_ORACLE_BLOCKED_PARAMS;
      case SQLSERVER:
        return JDBC_SQLSERVER_BLOCKED_PARAMS;
      case MYSQL:
      case STARROCKS:
      case CLICKHOUSE:
      case DM:
      default:
        // MySQL keeps using its own MYSQL_SENSITIVE_PARAMS path for backwards compatibility;
        // ClickHouse/DM fall through with just the global denylist.
        return JDBC_GLOBAL_BLOCKED_PARAMS;
    }
  }

  private static Map<String, Object> getDriverForceParams(JdbcDriverType driverType) {
    CommonVars<String> source;
    switch (driverType) {
      case POSTGRESQL:
      case GREENPLUM:
      case KINGBASE:
        source = JDBC_POSTGRES_FORCE_PARAMS;
        break;
      case DB2:
        source = JDBC_DB2_FORCE_PARAMS;
        break;
      case ORACLE:
        source = JDBC_ORACLE_FORCE_PARAMS;
        break;
      case SQLSERVER:
        source = JDBC_SQLSERVER_FORCE_PARAMS;
        break;
      case CLICKHOUSE:
        source = JDBC_CLICKHOUSE_FORCE_PARAMS;
        break;
      case DM:
        source = JDBC_DM_FORCE_PARAMS;
        break;
      case MYSQL:
      case STARROCKS:
      default:
        return new LinkedHashMap<>();
    }
    return parseParamUrlToMap(source.getValue());
  }

  private static boolean containsAnyToken(String key, String value, List<String> tokens) {
    String lowerKey = key.toLowerCase();
    String lowerValue = value.toLowerCase();
    for (String token : tokens) {
      if (StringUtils.isBlank(token)) {
        continue;
      }
      String lower = token.toLowerCase();
      if (lowerKey.contains(lower) || lowerValue.contains(lower)) {
        return true;
      }
    }
    return false;
  }

  private static String[] parseCsv(String csv) {
    if (StringUtils.isBlank(csv)) {
      return new String[0];
    }
    return csv.split(COMMA);
  }

  private static Map<String, Object> parseParamUrlToMap(String paramsUrl) {
    Map<String, Object> map = new LinkedHashMap<>();
    if (StringUtils.isBlank(paramsUrl)) {
      return map;
    }
    for (String param : paramsUrl.split(AND_SYMBOL)) {
      int idx = param.indexOf(EQUAL_SIGN);
      if (idx < 0) {
        continue;
      }
      String k = param.substring(0, idx);
      String v = param.substring(idx + 1);
      if (StringUtils.isNotBlank(k)) {
        map.put(k, v);
      }
    }
    return map;
  }

  /**
   * Check if the path has a relative path
   *
   * @param path
   * @return
   */
  public static boolean containsRelativePath(String path) {
    if (path.startsWith("./")
        || path.contains("/./")
        || path.startsWith("../")
        || path.contains("/../")) {
      return true;
    }
    if (path.startsWith(".\\")
        || path.contains("\\.\\")
        || path.startsWith("..\\")
        || path.contains("\\..\\")) {
      return true;
    }
    return false;
  }
}
