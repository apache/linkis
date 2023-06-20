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

package org.apache.linkis.metadata.query.service;

import org.apache.linkis.metadata.query.common.domain.GenerateSqlInfo;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.service.GenerateSqlTemplate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSqlConnection implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractSqlConnection.class);

  public Connection conn;

  public ConnectMessage connectMessage;

  public AbstractSqlConnection(
      String host,
      Integer port,
      String username,
      String password,
      String database,
      Map<String, Object> extraParams)
      throws ClassNotFoundException, SQLException {
    connectMessage = new ConnectMessage(host, port, username, password, extraParams);
    conn = getDBConnection(connectMessage, database);
    // Try to create statement
    Statement statement = conn.createStatement();
    statement.close();
  }

  public abstract Connection getDBConnection(ConnectMessage connectMessage, String database)
      throws ClassNotFoundException, SQLException;

  public List<MetaColumnInfo> getColumns(String schemaname, String table)
      throws SQLException, ClassNotFoundException {
    List<MetaColumnInfo> columns = new ArrayList<>();
    String columnSql = "SELECT * FROM " + schemaname + "." + table + " WHERE 1 = 2";
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
        info.setLength(meta.getColumnDisplaySize(i));
        info.setNullable((meta.isNullable(i) == ResultSetMetaData.columnNullable));
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
   * Get primary keys // * @param connection connection
   *
   * @param table table name
   * @return
   * @throws SQLException
   */
  public List<String> getPrimaryKeys(String table) throws SQLException {
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
        rs.close();
      }
    }
  }

  public GenerateSqlInfo queryJdbcSql(String database, String table) {
    GenerateSqlInfo generateSqlInfo = new GenerateSqlInfo();
    String ddl = generateJdbcDdlSql(database, table);
    generateSqlInfo.setDdl(ddl);

    generateSqlInfo.setDml(GenerateSqlTemplate.generateDmlSql(table));

    String columnStr = "*";
    try {
      List<MetaColumnInfo> columns = getColumns(database, table);
      if (CollectionUtils.isNotEmpty(columns)) {
        columnStr =
            columns.stream().map(column -> column.getName()).collect(Collectors.joining(","));
      }
    } catch (Exception e) {
      LOG.warn("Fail to get Sql columns(获取字段列表失败)", e);
    }
    generateSqlInfo.setDql(GenerateSqlTemplate.generateDqlSql(columnStr, table));

    return generateSqlInfo;
  }

  public String generateJdbcDdlSql(String database, String table) {
    StringBuilder ddl = new StringBuilder();
    ddl.append("CREATE TABLE ").append(String.format("%s.%s", database, table)).append(" (");

    try {
      List<MetaColumnInfo> columns = getColumns(database, table);
      if (CollectionUtils.isNotEmpty(columns)) {
        for (MetaColumnInfo column : columns) {
          ddl.append("\n\t").append(column.getName()).append(" ").append(column.getType());
          if (column.getLength() > 0) {
            ddl.append("(").append(column.getLength()).append(")");
          }
          if (!column.isNullable()) {
            ddl.append(" NOT NULL");
          }
          ddl.append(",");
        }
        String primaryKeys =
            columns.stream()
                .filter(MetaColumnInfo::isPrimaryKey)
                .map(MetaColumnInfo::getName)
                .collect(Collectors.joining(", "));
        if (StringUtils.isNotBlank(primaryKeys)) {
          ddl.append(String.format("\n\tPRIMARY KEY (%s),", primaryKeys));
        }
        ddl.deleteCharAt(ddl.length() - 1);
      }
    } catch (Exception e) {
      LOG.warn("Fail to get Sql columns(获取字段列表失败)", e);
    }

    ddl.append("\n)");

    return ddl.toString();
  }

  /**
   * close database resource
   *
   * @param connection connection
   * @param statement statement
   * @param resultSet result set
   */
  public void closeResource(Connection connection, Statement statement, ResultSet resultSet) {
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

  /** Connect message */
  public static class ConnectMessage {
    public String host;

    public Integer port;

    public String username;

    public String password;

    public Map<String, Object> extraParams;

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
