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

import org.apache.linkis.common.conf.BDPConfiguration;
import org.apache.linkis.common.exception.LinkisSecurityException;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** SecurityUtils Tester */
public class SecurityUtilsTest {

  @BeforeAll
  public static void init() {
    BDPConfiguration.set("linkis.mysql.strong.security.enable", "true");
  }

  @Test
  public void testCheckUrl() {
    // true
    String url = "jdbc:mysql://127.0.0.1:10000/db_name";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkUrl(url);
        });
    // false
    String url1 = "jdbc:mysql://127.0.0.1:10000/db_name?";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkUrl(url1);
        });
    // false
    String url11 = "jdbc:mysql://127.0.0.1:10000/db_name?abc";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkUrl(url11);
        });
    // true
    String url2 = "jdbc:mysql://127.0.0.1:10000/";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkUrl(url2);
        });
    // true
    String url3 = "jdbc:mysql://127.0.0.1:10000";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkUrl(url3);
        });
    // true
    String url4 = "JDBC:mysql://127.0.0.1:10000/db_name";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkUrl(url4);
        });
    // true
    String url5 = "JDBC:H2://127.0.0.1:10000/db_name";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkUrl(url5);
        });
    // true
    String url6 = "JDBC:H2://test-example.com:10000/db_name";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkUrl(url6);
        });
    // true
    String url7 = "JDBC:H2://example.测试:10000/db_name";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkUrl(url7);
        });
  }

  @Test
  public void testGetUrl() {
    BDPConfiguration.set("linkis.mysql.strong.security.enable", "true");
    String baseUrl = "jdbc:mysql://127.0.0.1:10000/db_name";
    String securityStr =
        "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";
    String url1 = "jdbc:mysql://127.0.0.1:10000/db_name";
    Assertions.assertEquals(baseUrl, SecurityUtils.getJdbcUrl(url1));
    String url11 = "jdbc:mysql://127.0.0.1:10000/db_name?";
    Assertions.assertEquals(baseUrl, SecurityUtils.getJdbcUrl(url11));
    String url2 = "jdbc:mysql://127.0.0.1:10000/db_name?k1=v1&";
    Assertions.assertEquals(baseUrl + "?k1=v1&" + securityStr, SecurityUtils.getJdbcUrl(url2));
    String url3 = "jdbc:mysql://127.0.0.1:10000/db_name?k1=v1&k2";
    Assertions.assertEquals(baseUrl + "?k1=v1&" + securityStr, SecurityUtils.getJdbcUrl(url3));
  }

  @Test
  public void testCheckJdbcConnParams() {
    String host = "127.0.0.1";
    Integer port = 3306;
    String username = "test";
    String password = "test";
    String database = "tdb";
    Map<String, Object> extraParams = new HashMap<>();
    extraParams.put("k1", "v1");

    // match ip
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });
    String host1 = "localhost";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnParams(host1, port, username, password, database, extraParams);
        });

    // match domain
    String host2 = "www.apache.com";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnParams(host2, port, username, password, database, extraParams);
        });

    // error host
    String host3 = "localhost:3306";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host3, port, username, password, database, extraParams);
        });

    String host4 = "localhost:3306/test";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host4, port, username, password, database, extraParams);
        });

    // error port
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, null, username, password, database, extraParams);
        });

    // error username
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, "   ", password, database, extraParams);
        });
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, null, password, database, extraParams);
        });

    // check database, The database name can be empty
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, "   ", extraParams);
        });

    String database1 = "test?k1=v1";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database1, extraParams);
        });

    // error param
    extraParams.put("allowLoadLocalInfile", "true");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });

    extraParams.clear();
    extraParams.put("autoDeserialize", "true");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });

    extraParams.clear();
    extraParams.put("allowLocalInfile", "true");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });

    extraParams.clear();
    extraParams.put("allowUrlInLocalInfile", "false");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });

    extraParams.clear();
    extraParams.put("allowLocalInfil%65", "true");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });

    extraParams.clear();
    extraParams.put("#", "true");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });

    extraParams.clear();
    extraParams.put("test", "#");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnParams(host, port, username, password, database, extraParams);
        });
  }

  @Test
  public void testCheckJdbcConnUrl() {
    // true
    String url = "jdbc:mysql://127.0.0.1:10000/db_name";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnUrl(url);
        });
    // true
    String url1 = "jdbc:mysql://127.0.0.1:10000/db_name?";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnUrl(url1);
        });
    // true
    String url11 = "jdbc:mysql://127.0.0.1/db_name?";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnUrl(url11);
        });
    // true
    String url2 = "JDBC:mysql://127.0.0.1:10000/db_name?";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnUrl(url2);
        });
    // true
    String url21 = "JDBC:h2://127.0.0.1:10000/db_name?";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnUrl(url21);
        });
    // true
    String url3 = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1";
    Assertions.assertDoesNotThrow(
        () -> {
          SecurityUtils.checkJdbcConnUrl(url3);
        });
    // false url error
    String url33 =
        "jdbc:mysql://127.0.0.1:10000:/db_name?jdbc:mysql://127.0.0.1:10000?allowLocalInfile=true";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnUrl(url33);
        });
    // false key is not security
    String url4 = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1&allowLocalInfile=true";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnUrl(url4);
        });

    // false value is not security
    String url5 = "jdbc:mysql://127.0.0.1:10000/db_name?p1=allowLocalInfile";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnUrl(url5);
        });

    // false contains #
    String url6 = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1&#p2=v2";
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcConnUrl(url6);
        });
  }

  @Test
  public void testAppendMysqlForceParamsExtraParams() {
    Map<String, Object> extraParams = new HashMap<>();
    extraParams.put("testKey", "testValue");
    SecurityUtils.appendMysqlForceParams(extraParams);
    Assertions.assertEquals("false", extraParams.get("allowLoadLocalInfile"));
    Assertions.assertEquals("false", extraParams.get("autoDeserialize"));
    Assertions.assertEquals("false", extraParams.get("allowLocalInfile"));
    Assertions.assertEquals("false", extraParams.get("allowUrlInLocalInfile"));
    Assertions.assertEquals("testValue", extraParams.get("testKey"));
    Assertions.assertEquals(null, extraParams.get("otherKey"));
  }

  @Test
  public void testMapToString() {
    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put("p1", "v1");
    String str = SecurityUtils.parseParamsMapToMysqlParamUrl(paramsMap);
    Assertions.assertEquals("p1=v1", str);

    paramsMap.clear();
    str = SecurityUtils.parseParamsMapToMysqlParamUrl(paramsMap);
    Assertions.assertEquals("", str);

    str = SecurityUtils.parseParamsMapToMysqlParamUrl(null);
    Assertions.assertEquals("", str);
  }
}
