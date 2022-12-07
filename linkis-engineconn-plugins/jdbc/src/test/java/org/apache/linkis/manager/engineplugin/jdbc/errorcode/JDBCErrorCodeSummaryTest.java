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

package org.apache.linkis.manager.engineplugin.jdbc.errorcode;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.manager.engineplugin.jdbc.errorcode.JDBCErrorCodeSummary.JDBC_GET_DATASOURCEINFO_ERROR;
import static org.apache.linkis.manager.engineplugin.jdbc.errorcode.JDBCErrorCodeSummary.JDBC_PARAMS_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JDBCErrorCodeSummaryTest {
  @Test
  void testGetErrorCode() {
    assertEquals(26010, JDBC_GET_DATASOURCEINFO_ERROR.getErrorCode());
    assertEquals(26011, JDBC_PARAMS_ILLEGAL.getErrorCode());
  }

  @Test
  void testSetErrorCode() {
    JDBC_GET_DATASOURCEINFO_ERROR.setErrorCode(1);
    assertEquals(1, JDBC_GET_DATASOURCEINFO_ERROR.getErrorCode());
    JDBC_GET_DATASOURCEINFO_ERROR.setErrorCode(26010);
    assertEquals(26010, JDBC_GET_DATASOURCEINFO_ERROR.getErrorCode());

    JDBC_PARAMS_ILLEGAL.setErrorCode(1);
    assertEquals(1, JDBC_PARAMS_ILLEGAL.getErrorCode());
    JDBC_PARAMS_ILLEGAL.setErrorCode(26011);
    assertEquals(26011, JDBC_PARAMS_ILLEGAL.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "Failed to get datasource info from datasource server(从数据源服务器获取数据源信息失败)",
        JDBC_GET_DATASOURCEINFO_ERROR.getErrorDesc());
    assertEquals(
        "JDBC related parameters are illegal(JDBC相关参数非法)", JDBC_PARAMS_ILLEGAL.getErrorDesc());
  }

  @Test
  void testSetErrorDesc() {
    JDBC_GET_DATASOURCEINFO_ERROR.setErrorDesc("test");
    assertEquals("test", JDBC_GET_DATASOURCEINFO_ERROR.getErrorDesc());
    JDBC_GET_DATASOURCEINFO_ERROR.setErrorDesc(
        "Failed to get datasource info from datasource server(从数据源服务器获取数据源信息失败)");
    assertEquals(
        "Failed to get datasource info from datasource server(从数据源服务器获取数据源信息失败)",
        JDBC_GET_DATASOURCEINFO_ERROR.getErrorDesc());

    JDBC_PARAMS_ILLEGAL.setErrorDesc("test");
    assertEquals("test", JDBC_PARAMS_ILLEGAL.getErrorDesc());
    JDBC_PARAMS_ILLEGAL.setErrorDesc("JDBC related parameters are illegal(JDBC相关参数非法)");
    assertEquals(
        "JDBC related parameters are illegal(JDBC相关参数非法)", JDBC_PARAMS_ILLEGAL.getErrorDesc());
  }
}
