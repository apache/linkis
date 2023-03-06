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

import org.apache.linkis.common.exception.LinkisSecurityException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** SecurityUtils Tester */
public class SecurityUtilsTest {

  @Test
  public void testAppendMysqlForceParamsUrl() throws Exception {
    // allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false
    String url = "jdbc:mysql://127.0.0.1:10000/db_name";
    String newUrl = SecurityUtils.appendMysqlForceParams(url);
    Assertions.assertEquals(
        url
            + "?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
        newUrl);

    url = "jdbc:mysql://127.0.0.1:10000/db_name?";
    newUrl = SecurityUtils.appendMysqlForceParams(url);
    Assertions.assertEquals(
        url
            + "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
        newUrl);

    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1";
    newUrl = SecurityUtils.appendMysqlForceParams(url);
    Assertions.assertEquals(
        url
            + "&"
            + "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
        newUrl);
  }

  @Test
  public void testAppendMysqlForceParamsExtraParams() throws Exception {
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
  public void testCheckJdbcSecurityUrl() throws Exception {
    String url = "jdbc:mysql://127.0.0.1:10000/db_name";
    String newUrl = SecurityUtils.checkJdbcSecurity(url);
    Assertions.assertEquals(url, newUrl);

    url = "jdbc:mysql://127.0.0.1:10000/db_name?";
    newUrl = SecurityUtils.checkJdbcSecurity(url);
    Assertions.assertEquals(url, newUrl);

    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1";
    newUrl = SecurityUtils.checkJdbcSecurity(url);
    Assertions.assertEquals(url, newUrl);

    // key is not security
    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1&allowLocalInfile=true";
    AtomicReference<String> atomUrl = new AtomicReference<>(url);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(atomUrl.get());
        });

    // url encode
    url = "jdbc:mysql://127.0.0.1:10000/db_name?allowLocalInfil%65=true";
    atomUrl.set(url);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(atomUrl.get());
        });

    // value is not security
    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=allowLocalInfile";
    atomUrl.set(url);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(atomUrl.get());
        });

    // contains #
    url = "jdbc:mysql://127.0.0.1:10000/db_name?p1=v1&#p2=v2";
    atomUrl.set(url);
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(atomUrl.get());
        });
  }

  @Test
  public void testCheckJdbcSecurityParamsMap() throws Exception {
    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put("p1", "v1");
    Map<String, Object> newMap = SecurityUtils.checkJdbcSecurity(paramsMap);
    Assertions.assertEquals("v1", newMap.get("p1"));

    // key not security
    paramsMap.put("allowLocalInfil%67", "true");
    SecurityUtils.checkJdbcSecurity(paramsMap);
    Assertions.assertEquals("true", newMap.get("allowLocalInfilg"));

    // key not security
    paramsMap.put("allowLocalInfile", "false");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(paramsMap);
        });

    // value not security
    paramsMap.clear();
    paramsMap.put("p1", "allowLocalInfile");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(paramsMap);
        });

    // value not security
    paramsMap.clear();
    paramsMap.put("p1", "allowLocalInfil%65");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(paramsMap);
        });

    // contains #
    paramsMap.clear();
    paramsMap.put("p1#", "v1");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(paramsMap);
        });

    paramsMap.clear();
    paramsMap.put("p1", "v1#");
    Assertions.assertThrows(
        LinkisSecurityException.class,
        () -> {
          SecurityUtils.checkJdbcSecurity(paramsMap);
        });
  }

  @Test
  public void testMapToString() throws Exception {
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
