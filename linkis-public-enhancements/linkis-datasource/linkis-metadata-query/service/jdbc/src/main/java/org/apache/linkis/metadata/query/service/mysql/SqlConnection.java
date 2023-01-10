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

package org.apache.linkis.metadata.query.service.mysql;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;

import org.apache.commons.lang.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import scala.annotation.meta.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlConnection implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(SqlConnection.class);

  private static final CommonVars<String> SQL_DRIVER_CLASS =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.driver", "com.mysql.jdbc.Driver");

  private static final CommonVars<String> SQL_CONNECT_URL =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.url", "jdbc:mysql://%s:%s/%s");

  private static final CommonVars<Integer> SQL_CONNECT_TIMEOUT =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.connect.timeout", 3000);

  private static final CommonVars<Integer> SQL_SOCKET_TIMEOUT =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.socket.timeout", 6000);

  private static final CommonVars<Boolean> MYSQL_STRONG_SECURITY_ENABLE =
      CommonVars.apply("linkis.mysql.strong.security.enable", false);

  private Connection conn;

  private ConnectMessage connectMessage;

  public SqlConnection(
      String host,
      Integer port,
      String username,
      String password,
      String database,
      Map<String, Object> extraParams)
      throws ClassNotFoundException, SQLException {
    // Handle mysql security vulnerabilities
    validateParams(extraParams);
    connectMessage = new ConnectMessage(host, port, username, password, extraParams);
    conn = getDBConnection(connectMessage, database);
    // Try to create statement
    Statement statement = conn.createStatement();
    statement.close();
  }

  /**
   * Handle mysql security vulnerabilities
   *
   * @param extraParams
   */
  private void validateParams(Map<String, Object> extraParams) {
    if (extraParams == null) {
      return;
    }

    // enable strong security
    if (MYSQL_STRONG_SECURITY_ENABLE.getValue()) {
      LOG.info("mysql metadata use strong security configuration. Remove all connection parameters.");
      extraParams.clear();
    }

    // Delete suspected vulnerability parameters
    Iterator<Map.Entry<String, Object>> iterator = extraParams.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      if (StringUtils.isBlank(key)
          || entry.getValue() == null
          || StringUtils.isBlank(entry.getValue().toString())) {
        iterator.remove();
        continue;
      }
      String value = entry.getValue().toString();
      if (keyAndValueIsNotSecurity(key, value, "allowLoadLocalInfile")
          || keyAndValueIsNotSecurity(key, value, "autoDeserialize")
          || keyAndValueIsNotSecurity(key, value, "allowLocalInfile")
          || keyAndValueIsNotSecurity(key, value, "allowUrlInLocalInfile")
          || keyAndValueIsNotSecurity(key, value, "#")) {
        iterator.remove();
      }
    }

    // Set all vulnerability parameters to false
    extraParams.put("allowLoadLocalInfile", "false");
    extraParams.put("autoDeserialize", "false");
    extraParams.put("allowLocalInfile", "false");
    extraParams.put("allowUrlInLocalInfile", "false");

    // print extraParams
    StringBuilder sb = new StringBuilder("mysql metadata url extraParams: [ ");
    for(Map.Entry<String, Object> paramEntry : extraParams.entrySet()){
      sb.append(paramEntry.getKey()).append("=").append(paramEntry.getValue()).append(" ,");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("]");
    LOG.info(sb.toString());
  }

  private boolean keyAndValueIsNotSecurity(String key, String value, String param) {
    return !(isSecurity(key, param) && isSecurity(value, param));
  }

  private boolean isSecurity(String noSecurityKey, String param) {
    if (StringUtils.isBlank(param) || StringUtils.isBlank(noSecurityKey)) {
      return true;
    }
    return !noSecurityKey.toLowerCase().contains(param.toLowerCase());
  }

  public List<String> getAllDatabases() throws SQLException {
    List<String> dataBaseName = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs = stmt.executeQuery("SHOW DATABASES");
      while (rs.next()) {
        dataBaseName.add(rs.getString(1));
      }
    } finally {
      closeResource(null, stmt, rs);
    }
    return dataBaseName;
  }

  public List<String> getAllTables(String database) throws SQLException {
    List<String> tableNames = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs = stmt.executeQuery("SHOW TABLES FROM `" + database + "`");
      while (rs.next()) {
        tableNames.add(rs.getString(1));
      }
      return tableNames;
    } finally {
      closeResource(null, stmt, rs);
    }
  }

  public List<MetaColumnInfo> getColumns(String database, String table)
      throws SQLException, ClassNotFoundException {
    List<MetaColumnInfo> columns = new ArrayList<>();
    String columnSql = "SELECT * FROM `" + database + "`.`" + table + "` WHERE 1 = 2";
    PreparedStatement ps = null;
    ResultSet rs = null;
    ResultSetMetaData meta = null;
    try {
      List<String> primaryKeys = getPrimaryKeys(getDBConnection(connectMessage, database), table);
      ps = conn.prepareStatement(columnSql);
      rs = ps.executeQuery();
      meta = rs.getMetaData();
      int columnCount = meta.getColumnCount();
      for (int i = 1; i < columnCount + 1; i++) {
        MetaColumnInfo info = new MetaColumnInfo();
        info.setIndex(i);
        info.setName(meta.getColumnName(i));
        info.setType(meta.getColumnTypeName(i));
        if (primaryKeys.contains(meta.getColumnName(i))) {
          info.setPrimaryKey(true);
        }
        columns.add(info);
      }
    } finally {
      closeResource(null, ps, rs);
    }
    return columns;
  }

  /**
   * Get primary keys
   *
   * @param connection connection
   * @param table table name
   * @return
   * @throws SQLException
   */
  private List<String> getPrimaryKeys(Connection connection, String table) throws SQLException {
    ResultSet rs = null;
    List<String> primaryKeys = new ArrayList<>();
    try {
      DatabaseMetaData dbMeta = connection.getMetaData();
      rs = dbMeta.getPrimaryKeys(null, null, table);
      while (rs.next()) {
        primaryKeys.add(rs.getString("column_name"));
      }
      return primaryKeys;
    } finally {
      if (null != rs) {
        closeResource(connection, null, rs);
      }
    }
  }

  /**
   * close database resource
   *
   * @param connection connection
   * @param statement statement
   * @param resultSet result set
   */
  private void closeResource(Connection connection, Statement statement, ResultSet resultSet) {
    try {
      if (null != resultSet && !resultSet.isClosed()) {
        resultSet.close();
      }
      if (null != statement && !statement.isClosed()) {
        statement.close();
      }
      if (null != connection && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      LOG.warn("Fail to release resource [" + e.getMessage() + "]", e);
    }
  }

  @Override
  public void close() throws IOException {
    closeResource(conn, null, null);
  }

  /**
   * @param connectMessage
   * @param database
   * @return
   * @throws ClassNotFoundException
   */
  private Connection getDBConnection(ConnectMessage connectMessage, String database)
      throws ClassNotFoundException, SQLException {
    String extraParamString =
        connectMessage.extraParams.entrySet().stream()
            .map(e -> String.join("=", e.getKey(), String.valueOf(e.getValue())))
            .collect(Collectors.joining("&"));
    Class.forName(SQL_DRIVER_CLASS.getValue());
    String url =
        String.format(
            SQL_CONNECT_URL.getValue(), connectMessage.host, connectMessage.port, database);
    if (!connectMessage.extraParams.isEmpty()) {
      url += "?" + extraParamString;
    }
    return DriverManager.getConnection(url, connectMessage.username, connectMessage.password);
  }

  /** Connect message */
  private static class ConnectMessage {
    private String host;

    private Integer port;

    private String username;

    private String password;

    private Map<String, Object> extraParams;

    public ConnectMessage(
        String host,
        Integer port,
        String username,
        String password,
        Map<String, Object> extraParams) {
      this.host = host;
      this.port = port;
      this.username = username;
      this.password = password;
      this.extraParams = extraParams;
      this.extraParams.put("connectTimeout", SQL_CONNECT_TIMEOUT.getValue());
      this.extraParams.put("socketTimeout", SQL_SOCKET_TIMEOUT.getValue());
    }
  }
}
