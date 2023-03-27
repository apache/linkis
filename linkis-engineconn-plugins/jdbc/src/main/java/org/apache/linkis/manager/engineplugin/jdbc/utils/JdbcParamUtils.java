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

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.CommonVars$;
import org.apache.linkis.manager.engineplugin.jdbc.JDBCPropertiesParser;
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant;
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.jdbc.errorcode.JDBCErrorCodeSummary.*;

public class JdbcParamUtils {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcParamUtils.class);
  private static final String AUTO_DESERIALIZE = "autoDeserialize";

  private static final String APPEND_PARAMS =
      "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";

  public static final CommonVars<String> MYSQL_STRONG_SECURITY_ENABLE =
      CommonVars$.MODULE$.apply("linkis.mysql.strong.security.enable", "false");

  private static final String QUOTATION_MARKS = "\"";

  public static String clearJdbcUrl(String url) {
    if (url.startsWith(QUOTATION_MARKS) && url.endsWith(QUOTATION_MARKS)) {
      url = url.trim();
      return url.substring(1, url.length() - 1);
    }
    return url;
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
