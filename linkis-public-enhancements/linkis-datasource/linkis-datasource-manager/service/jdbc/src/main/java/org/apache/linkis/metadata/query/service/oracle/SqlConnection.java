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

package org.apache.linkis.metadata.query.service.oracle;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.service.AbstractSqlConnection;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlConnection extends AbstractSqlConnection {
  private static final Logger LOG = LoggerFactory.getLogger(SqlConnection.class);

  private static final CommonVars<String> SQL_DRIVER_CLASS =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.oracle.driver", "oracle.jdbc.driver.OracleDriver");

  private static final CommonVars<String> SQL_CONNECT_URL =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.oracle.sid.url", "jdbc:oracle:thin:@%s:%s:%s");

  private static final CommonVars<String> SQL_CONNECT_SERVICE_URL =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.oracle.service.url", "jdbc:oracle:thin:@//%s:%s/%s");

  private String serviceName;

  public SqlConnection(
      String host,
      Integer port,
      String username,
      String password,
      String database,
      String serviceName,
      Map<String, Object> extraParams)
      throws ClassNotFoundException, SQLException {
    super(host, port, username, password, database, extraParams);
    this.serviceName = serviceName;
  }

  public List<String> getAllDatabases() throws SQLException {
    List<String> dataBaseName = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs =
          stmt.executeQuery(
              "select username from sys.dba_users WHERE default_tablespace not in ('SYSTEM','SYSAUX') and ACCOUNT_STATUS = 'OPEN'\n");
      while (rs.next()) {
        dataBaseName.add(rs.getString("username"));
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
              "SELECT table_name FROM sys.dba_tables WHERE owner = '" + schemaname + "'");
      while (rs.next()) {
        tableNames.add(rs.getString("TABLE_NAME"));
      }
      return tableNames;
    } finally {
      closeResource(null, stmt, rs);
    }
  }

  @Override
  public Connection getDBConnection(ConnectMessage connectMessage, String database)
      throws ClassNotFoundException, SQLException {
    return getDBConnection(connectMessage, database, serviceName);
  }

  public List<MetaColumnInfo> getColumns(String schemaname, String table)
      throws SQLException, ClassNotFoundException {
    List<MetaColumnInfo> columns = new ArrayList<>();
    String columnSql = "SELECT * FROM " + schemaname + "." + table + " WHERE 1 = 2";
    PreparedStatement ps = null;
    ResultSet rs = null;
    ResultSetMetaData meta;
    try {
      List<String> primaryKeys = getPrimaryKeys(table);
      Map<String, String> columnCommentMap = getColumnComment(schemaname, table);
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
        String colComment = columnCommentMap.get(meta.getColumnName(i));
        if (StringUtils.isNotBlank(colComment)) {
          info.setColComment(colComment);
        } else {
          info.setColComment(StringUtils.EMPTY);
        }

        columns.add(info);
      }
    } finally {
      closeResource(null, ps, rs);
    }
    return columns;
  }

  /**
   * Get Column Comment
   *
   * @param table table name
   * @return
   * @throws SQLException
   */
  private Map<String, String> getColumnComment(String schema, String table) throws SQLException {
    ResultSet rs = null;
    Map<String, String> columnComment = new HashMap();

    DatabaseMetaData dbMeta = conn.getMetaData();
    rs = dbMeta.getColumns(null, schema, table, "%");
    while (rs.next()) {
      columnComment.put(rs.getString("COlUMN_NAME"), rs.getString("REMARKS"));
    }
    return columnComment;
  }

  /**
   * @param connectMessage
   * @param database
   * @return
   * @throws ClassNotFoundException
   */
  private Connection getDBConnection(
      ConnectMessage connectMessage, String database, String serviceName)
      throws ClassNotFoundException, SQLException {
    Class.forName(SQL_DRIVER_CLASS.getValue());
    String url = "";
    if (StringUtils.isNotBlank(database)) {
      url =
          String.format(
              SQL_CONNECT_URL.getValue(), connectMessage.host, connectMessage.port, database);
    } else if (StringUtils.isNotBlank(serviceName)) {
      url =
          String.format(
              SQL_CONNECT_SERVICE_URL.getValue(),
              connectMessage.host,
              connectMessage.port,
              serviceName);
    }

    if (MapUtils.isNotEmpty(connectMessage.extraParams)) {
      String extraParamString =
          connectMessage.extraParams.entrySet().stream()
              .map(e -> String.join("=", e.getKey(), String.valueOf(e.getValue())))
              .collect(Collectors.joining("&"));
      url += "?" + extraParamString;
    }
    Properties prop = new Properties();
    prop.put("user", connectMessage.username);
    prop.put("password", connectMessage.password);
    prop.put("remarksReporting", "true");
    return DriverManager.getConnection(url, prop);
  }

  public String getSqlConnectUrl() {
    return SQL_CONNECT_URL.getValue();
  }

  @Override
  public String generateJdbcDdlSql(String database, String table) {
    String columnSql =
        String.format(
            "SELECT DBMS_METADATA.GET_DDL('TABLE', '%s', '%s') AS DDL  FROM DUAL ",
            table, database);
    PreparedStatement ps = null;
    ResultSet rs = null;
    String ddl = "";
    try {
      ps = conn.prepareStatement(columnSql);
      rs = ps.executeQuery();
      if (rs.next()) {
        ddl = rs.getString("DDL");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      closeResource(null, ps, rs);
    }
    return ddl;
  }
}
