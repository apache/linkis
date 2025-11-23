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

package org.apache.linkis.manager.engineplugin.jdbc;

import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant;
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConnectionManagerTest {
  @Test
  @DisplayName("testCreateJdbcConnAndExecSql")
  public void testCreateJdbcConnAndExecSql()
      throws SQLException, JDBCParamsIllegalException, ClassNotFoundException {
    Map<String, String> properties = new HashMap<>(8);
    properties.put(
        JDBCEngineConnConstant.JDBC_URL, "jdbc:h2:mem:linkis_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
    properties.put(JDBCEngineConnConstant.JDBC_DRIVER, "org.h2.Driver");
    properties.put(JDBCEngineConnConstant.JDBC_USERNAME, "user");
    properties.put(JDBCEngineConnConstant.JDBC_PASSWORD, "password");
    properties.put(JDBCEngineConnConstant.JDBC_POOL_DEFAULT_VALIDATION_QUERY, "SELECT 1");
    properties.put(JDBCEngineConnConstant.JDBC_AUTH_TYPE, "USERNAME");
    properties.put(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL, "");
    properties.put(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION, "");
    properties.put(JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY, "");
    properties.put(JDBCEngineConnConstant.JDBC_PROXY_USER, "");
    properties.put(JDBCEngineConnConstant.JDBC_SCRIPTS_EXEC_USER, "leo_jie");
    ConnectionManager connectionManager = ConnectionManager.getInstance();
    Connection conn = connectionManager.getConnection("jdbc-1", properties);
    Statement statement = conn.createStatement();
    ResultSet rs = statement.executeQuery("show databases;");
    while (rs.next()) {
      System.out.println(rs.getObject(1));
    }
    rs.close();
    statement.close();
    conn.close();
  }

  @Test
  @DisplayName("testCreateJdbcConnAndExecSql")
  public void testJDBCPropertiesParserGetLong() {
    Map<String, String> properties = new HashMap<>(1);
    properties.put("key", "10");
    long v = JDBCPropertiesParser.getLong(properties, "key", 0);
    Assertions.assertEquals(10, v);
  }
}
