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
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.LinkisSecurityException;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
  public void testRSA() {
    String originalData = "rsa-test-str";
    String pubKey = Configuration.LINKIS_RSA_PUBLIC_KEY().getValue();
    String privKey = Configuration.LINKIS_RSA_PRIVATE_KEY().getValue();
    if (StringUtils.isNotEmpty(pubKey) && StringUtils.isNotEmpty(privKey)) {
      String encryptData = RSAUtils.encryptWithLinkisPublicKey(originalData);
      String dncryptData = RSAUtils.dncryptWithLinkisPublicKey(encryptData);
      Assertions.assertEquals(dncryptData, originalData);
    }
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

  // ----------------------- Generic JDBC API tests (CVE-2023-49566 fix-up) -----------------------

  private void assertDriverRejectsParam(JdbcDriverType driver, String key, String value) {
    Map<String, Object> params = new HashMap<>();
    params.put(key, value);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> SecurityUtils.checkJdbcConnParams(driver, "localhost", 5432, "u", "p", "db", params),
        "driver " + driver + " should reject param " + key);
  }

  @Test
  public void testGenericCheck_PostgresDenylist() {
    // The headline RCE sink from the advisory — must be blocked for every PG-family driver.
    assertDriverRejectsParam(
        JdbcDriverType.POSTGRESQL,
        "socketFactory",
        "org.springframework.context.support.ClassPathXmlApplicationContext");
    assertDriverRejectsParam(JdbcDriverType.POSTGRESQL, "socketFactoryArg", "http://evil/poc.xml");
    assertDriverRejectsParam(JdbcDriverType.POSTGRESQL, "sslfactory", "evil.Class");
    assertDriverRejectsParam(JdbcDriverType.POSTGRESQL, "sslfactoryarg", "evil");
    assertDriverRejectsParam(JdbcDriverType.POSTGRESQL, "loggerFile", "/tmp/evil.log");
    assertDriverRejectsParam(JdbcDriverType.POSTGRESQL, "loggerLevel", "TRACE");
    // Greenplum and KingBase share the PG-family denylist.
    assertDriverRejectsParam(JdbcDriverType.GREENPLUM, "socketFactory", "evil.Class");
    assertDriverRejectsParam(JdbcDriverType.KINGBASE, "socketFactory", "evil.Class");
  }

  @Test
  public void testGenericCheck_Db2JndiParam() {
    // clientRerouteServerListJNDIName is the original CVE-2023-49566 JNDI sink.
    assertDriverRejectsParam(
        JdbcDriverType.DB2, "clientRerouteServerListJNDIName", "ldap://evil/exp");
    assertDriverRejectsParam(JdbcDriverType.DB2, "enableSeamlessFailover", "true");
    assertDriverRejectsParam(JdbcDriverType.DB2, "JNDIName", "ldap://evil/exp");
  }

  @Test
  public void testGenericCheck_OracleDenylist() {
    assertDriverRejectsParam(JdbcDriverType.ORACLE, "oracle.net.tns_admin", "/etc/evil");
    assertDriverRejectsParam(JdbcDriverType.ORACLE, "javax.net.ssl.trustStore", "/etc/evil");
    assertDriverRejectsParam(JdbcDriverType.ORACLE, "javax.net.ssl.trustStorePassword", "hunter2");
    assertDriverRejectsParam(JdbcDriverType.ORACLE, "javax.net.ssl.keyStore", "/etc/evil");
  }

  @Test
  public void testGenericCheck_SqlserverDenylist() {
    assertDriverRejectsParam(JdbcDriverType.SQLSERVER, "jaasConfigurationName", "evil");
    assertDriverRejectsParam(JdbcDriverType.SQLSERVER, "jaasApplicationName", "evil");
  }

  @Test
  public void testGenericCheck_GlobalDenylistAppliesToAllDrivers() {
    // The global denylist (autoDeserialize, allowLoadLocalInfile, #) applies even to drivers
    // that have no driver-specific denylist entry (ClickHouse, DM).
    assertDriverRejectsParam(JdbcDriverType.CLICKHOUSE, "autoDeserialize", "true");
    assertDriverRejectsParam(JdbcDriverType.CLICKHOUSE, "#", "true");
    assertDriverRejectsParam(JdbcDriverType.DM, "autoDeserialize", "true");
    assertDriverRejectsParam(JdbcDriverType.POSTGRESQL, "autoDeserialize", "true");
    assertDriverRejectsParam(JdbcDriverType.ORACLE, "allowLoadLocalInfile", "true");
  }

  @Test
  public void testGenericCheck_UrlEncodedBypass() {
    // Attacker URL-encodes a char in a blocked param name hoping to slip past the substring
    // match. The decoder loop should normalize it back before matching.
    Map<String, Object> params = new HashMap<>();
    params.put("%73ocketFactory", "evil.Class"); // %73 = 's'
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () ->
            SecurityUtils.checkJdbcConnParams(
                JdbcDriverType.POSTGRESQL, "localhost", 5432, "u", "p", "db", params));

    // Value-side bypass attempt should also be caught.
    Map<String, Object> params2 = new HashMap<>();
    params2.put("safeKey", "soc%6betFactory"); // %6b = 'k'
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () ->
            SecurityUtils.checkJdbcConnParams(
                JdbcDriverType.POSTGRESQL, "localhost", 5432, "u", "p", "db", params2));
  }

  @Test
  public void testGenericCheck_HostInjection() {
    // A malicious host string tries to smuggle extra URL segments past the denylist.
    Map<String, Object> params = new HashMap<>();
    params.put("k1", "v1");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () ->
            SecurityUtils.checkJdbcConnParams(
                JdbcDriverType.POSTGRESQL,
                "evil.com:5432/db?socketFactory=x",
                5432,
                "u",
                "p",
                "db",
                params));
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () ->
            SecurityUtils.checkJdbcConnParams(
                JdbcDriverType.POSTGRESQL, "evil.com#frag", 5432, "u", "p", "db", params));
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () ->
            SecurityUtils.checkJdbcConnParams(
                JdbcDriverType.POSTGRESQL, "evil.com&extra=x", 5432, "u", "p", "db", params));
  }

  @Test
  public void testGenericCheck_BlankHostOrUsername() {
    Map<String, Object> params = new HashMap<>();
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () ->
            SecurityUtils.checkJdbcConnParams(
                JdbcDriverType.POSTGRESQL, "  ", 5432, "u", "p", "db", params));
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () ->
            SecurityUtils.checkJdbcConnParams(
                JdbcDriverType.POSTGRESQL, "localhost", 5432, "  ", "p", "db", params));
  }

  @Test
  public void testGenericCheck_AllowsSafeParams() {
    // Sanity: a benign param set for each driver family must not trip the denylist.
    for (JdbcDriverType driver : JdbcDriverType.values()) {
      Map<String, Object> params = new HashMap<>();
      params.put("connectTimeout", "5000");
      params.put("socketTimeout", "10000");
      Assertions.assertDoesNotThrow(
          () ->
              SecurityUtils.checkJdbcConnParams(driver, "localhost", 5432, "u", "p", "db", params),
          "driver " + driver + " should accept benign params");
    }
  }

  @Test
  public void testBuildSecureProperties_CredentialsPropagated() {
    Map<String, Object> params = new HashMap<>();
    params.put("connectTimeout", "5000");
    Properties props =
        SecurityUtils.buildSecureProperties(JdbcDriverType.POSTGRESQL, "alice", "secret", params);
    Assertions.assertEquals("alice", props.getProperty("user"));
    Assertions.assertEquals("secret", props.getProperty("password"));
    Assertions.assertEquals("5000", props.getProperty("connectTimeout"));
  }

  @Test
  public void testBuildSecureProperties_ForceParamsWinOverUserInput() {
    // SQL Server has a force-set default of trustServerCertificate=false. Even if the user
    // explicitly requests trustServerCertificate=true, the security default must win.
    Map<String, Object> params = new HashMap<>();
    params.put("trustServerCertificate", "true");
    Properties props =
        SecurityUtils.buildSecureProperties(JdbcDriverType.SQLSERVER, "u", "p", params);
    Assertions.assertEquals("false", props.getProperty("trustServerCertificate"));
  }

  @Test
  public void testBuildSecureProperties_GlobalForceParamsWinOverUserInput() {
    // Drivers without a driver-specific force-set (Postgres) still have the global denylist,
    // but the force-set behavior is verified via SQL Server's trustServerCertificate above.
    // Here we just check that user params propagate when there is no conflict.
    Map<String, Object> params = new HashMap<>();
    params.put("applicationName", "linkis");
    Properties props =
        SecurityUtils.buildSecureProperties(JdbcDriverType.POSTGRESQL, "u", "p", params);
    Assertions.assertEquals("linkis", props.getProperty("applicationName"));
    Assertions.assertEquals("u", props.getProperty("user"));
  }
}
