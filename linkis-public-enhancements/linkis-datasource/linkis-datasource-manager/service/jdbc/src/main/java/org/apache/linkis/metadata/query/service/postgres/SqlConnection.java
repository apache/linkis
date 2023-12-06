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

package org.apache.linkis.metadata.query.service.postgres;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.metadata.query.service.AbstractSqlConnection;

import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.util.Strings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlConnection extends AbstractSqlConnection {
  private static final Logger LOG = LoggerFactory.getLogger(SqlConnection.class);

  private static final CommonVars<String> SQL_DRIVER_CLASS =
      CommonVars.apply("wds.linkis.server.mdm.service.postgre.driver", "org.postgresql.Driver");

  private static final CommonVars<String> SQL_CONNECT_URL =
      CommonVars.apply("wds.linkis.server.mdm.service.postgre.url", "jdbc:postgresql://%s:%s/%s");

  public SqlConnection(
      String host,
      Integer port,
      String username,
      String password,
      String database,
      Map<String, Object> extraParams)
      throws ClassNotFoundException, SQLException {
    super(host, port, username, password, Strings.isBlank(database) ? "" : database, extraParams);
  }

  public List<String> getAllDatabases() throws SQLException {
    List<String> dataBaseName = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs = stmt.executeQuery("select datname from pg_database");
      while (rs.next()) {
        dataBaseName.add(rs.getString(1));
      }
    } finally {
      closeResource(null, stmt, rs);
    }
    return dataBaseName;
  }

  public List<String> getAllTables(String schemaname) throws SQLException {
    List<String> tableNames = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs =
          stmt.executeQuery(
              "SELECT tablename FROM pg_tables where schemaname = '" + schemaname + "'");
      while (rs.next()) {
        tableNames.add(rs.getString(1));
      }
      return tableNames;
    } finally {
      closeResource(null, stmt, rs);
    }
  }

  /**
   * @param connectMessage
   * @param database
   * @return
   * @throws ClassNotFoundException
   */
  public Connection getDBConnection(ConnectMessage connectMessage, String database)
      throws ClassNotFoundException, SQLException {
    Class.forName(SQL_DRIVER_CLASS.getValue());
    String url =
        String.format(
            SQL_CONNECT_URL.getValue(), connectMessage.host, connectMessage.port, database);

    if (MapUtils.isNotEmpty(connectMessage.extraParams)) {
      String extraParamString =
          connectMessage.extraParams.entrySet().stream()
              .map(e -> String.join("=", e.getKey(), String.valueOf(e.getValue())))
              .collect(Collectors.joining("&"));
      url += "?" + extraParamString;
    }
    return DriverManager.getConnection(url, connectMessage.username, connectMessage.password);
  }

  public String getSqlConnectUrl() {
    return SQL_CONNECT_URL.getValue();
  }
}
