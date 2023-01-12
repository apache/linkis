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

import org.apache.linkis.common.exception.LinkisSecurityException;
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant;
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JdbcParamUtilsTest {
  @Test
  @DisplayName("testFilterJdbcUrl")
  public void testFilterJdbcUrl() {
    String securityParam =
        "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";
    String url = "jdbc:mysql://127.0.0.1:10000/db_name";
    String newUrl = JdbcParamUtils.filterJdbcUrl(url);
    Assertions.assertEquals(url + "?" + securityParam, newUrl);

    // not mysql url
    url = "h2:mysql";
    newUrl = JdbcParamUtils.filterJdbcUrl(url);
    Assertions.assertEquals(url, newUrl);

    // start with JDBC
    url = "JDBC:mysql://127.0.0.1:10000/db_name?";
    newUrl = JdbcParamUtils.filterJdbcUrl(url);
    Assertions.assertEquals(url + securityParam, newUrl);

    url = "jdbc:mysql://127.0.0.1:10000/db_name?";
    newUrl = JdbcParamUtils.filterJdbcUrl(url);
    Assertions.assertEquals(url + securityParam, newUrl);

    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1";
    newUrl = JdbcParamUtils.filterJdbcUrl(url);
    Assertions.assertEquals(url + "&" + securityParam, newUrl);

    // key is not security
    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1&allowLocalInfile=true";
    AtomicReference<String> atomUrl = new AtomicReference<>(url);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          JdbcParamUtils.filterJdbcUrl(atomUrl.get());
        });

    // value is not security
    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=allowLocalInfile";
    atomUrl.set(url);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          JdbcParamUtils.filterJdbcUrl(atomUrl.get());
        });

    // contains #
    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1&#p2=v2";
    atomUrl.set(url);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          JdbcParamUtils.filterJdbcUrl(atomUrl.get());
        });
  }

  @Test
  @DisplayName("testGetJdbcUsername")
  public void testGetJdbcUsername() throws JDBCParamsIllegalException {
    Map<String, String> properties = new HashMap<>();
    properties.put(JDBCEngineConnConstant.JDBC_USERNAME, "test123?autoDeserialize=true");
    String username = JdbcParamUtils.getJdbcUsername(properties);
    Assertions.assertEquals("test123?=true", username);
  }

  @Test
  @DisplayName("testGetJdbcPassword")
  public void testGetJdbcPassword() throws JDBCParamsIllegalException {
    Map<String, String> properties = new HashMap<>();
    properties.put(JDBCEngineConnConstant.JDBC_USERNAME, "test_pwd?autoDeserialize=true");
    String password = JdbcParamUtils.getJdbcUsername(properties);
    Assertions.assertEquals("test_pwd?=true", password);
  }
}
