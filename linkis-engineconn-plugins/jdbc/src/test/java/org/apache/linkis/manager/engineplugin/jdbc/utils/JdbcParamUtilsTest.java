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

import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant;
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JdbcParamUtilsTest {

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
