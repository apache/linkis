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

package org.apache.linkis.metadata.query.service.sqlserver;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlConnection implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(SqlConnection.class);

  private static final CommonVars<String> SQL_DRIVER_CLASS =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.sqlserver.driver",
          "com.microsoft.sqlserver.jdbc.SQLServerDriver");

  private static final CommonVars<String> SQL_CONNECT_URL =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.sqlserver.url", "jdbc:sqlserver://%s:%s;DataBaseName=%s");

  private Connection conn;

  private ConnectMessage connectMessage;

  public SqlConnection(
      String host, Integer port, String username, String password, Map<String, Object> extraParams)
      throws ClassNotFoundException, SQLException {
    connectMessage = new ConnectMessage(host, port, username, password, extraParams);
    conn = getDBConnection(connectMessage, "");
    // Try to create statement
    Statement statement = conn.createStatement();
    statement.close();
  }

  public List<String> getAllDatabases() throws SQLException {
    List<String> dataBaseName = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs = stmt.executeQuery("SELECT Name FROM Master..SysDatabases ORDER BY Name");
      while (rs.next()) {
        dataBaseName.add(rs.getString(1));
      }
    } finally {
      closeResource(null, stmt, rs);
    }
    return dataBaseName;
  }

  /**
   * XType = 'U' 表示所有用户表 XType = 'S' 表示所有系统表
   *
   * @param database 数据库名
   * @return 数据表列表
   * @throws SQLException 异常
   */
  public List<String> getAllTables(String database) throws SQLException {
    List<String> tableNames = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs =
          stmt.executeQuery(
              "SELECT Name FROM " + database + "..SysObjects Where XType='U' ORDER BY Name");
      while (rs.next()) {
        tableNames.add(rs.getString(1));
      }
      return tableNames;
    } finally {
      closeResource(null, stmt, rs);
    }
  }

  /**
   * 这里写死查询的是 database.[dbo].table 的数据
   *
   * @param database 数据库名
   * @param table 表名
   * @return 列信息
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  public List<MetaColumnInfo> getColumns(String database, String table)
      throws SQLException, ClassNotFoundException {
    List<MetaColumnInfo> columns = new ArrayList<>();
    String columnSql = "SELECT * FROM " + database + ".dbo." + table + " WHERE 1 = 2";
    PreparedStatement ps = null;
    ResultSet rs = null;
    ResultSetMetaData meta;
    try {
      List<String> primaryKeys = getPrimaryKeys(table);
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
   * @param table table name
   * @return
   * @throws SQLException
   */
  private List<String> getPrimaryKeys(String table) throws SQLException {
    ResultSet rs = null;
    List<String> primaryKeys = new ArrayList<>();
    try {
      DatabaseMetaData dbMeta = conn.getMetaData();
      rs = dbMeta.getPrimaryKeys(null, null, table);
      while (rs.next()) {
        primaryKeys.add(rs.getString("column_name"));
      }
      return primaryKeys;
    } finally {
      if (null != rs) {
        closeResource(null, null, rs);
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
      if (null != statement /*&& !statement.isClosed()*/) {
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
    }
  }
}
