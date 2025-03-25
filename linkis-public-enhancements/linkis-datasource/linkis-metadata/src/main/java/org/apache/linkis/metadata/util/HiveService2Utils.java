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

package org.apache.linkis.metadata.util;

import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HiveService2Utils {

  private static final String driverName = "org.apache.hive.jdbc.HiveDriver";

  private static final String defaultDb = "default";
  private static final String defaultPassword = "123456";

  static ObjectMapper jsonMapper = new ObjectMapper();
  static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

  /** Determine whether to start hiveServer2 Query the left menu bar */
  public static Boolean checkHiveServer2Enable() {
    return DWSConfig.HIVE_SERVER2_ENABLE.getValue();
  }

  /** Get connection */
  private static Connection getConn(String username, String db) throws Exception {
    Class.forName(driverName);
    String hiveServer2Address = DWSConfig.HIVE_SERVER2_URL.getValue();
    String url =
        hiveServer2Address.endsWith("/") ? hiveServer2Address + db : hiveServer2Address + "/" + db;
    return DriverManager.getConnection(url, username, defaultPassword);
  }

  /** Get database */
  public static JsonNode getDbs(String username) throws Exception {
    ArrayNode dbsNode = jsonMapper.createArrayNode();
    List<String> dbs = new ArrayList<>();
    Connection conn = null;
    Statement stat = null;
    ResultSet rs = null;
    try {
      conn = getConn(username, defaultDb);
      stat = conn.createStatement();
      rs = stat.executeQuery("show databases");
      while (rs.next()) {
        dbs.add(rs.getString(1));
      }
    } finally {
      close(conn, stat, rs);
    }
    for (String db : dbs) {
      ObjectNode dbNode = jsonMapper.createObjectNode();
      dbNode.put("dbName", db);
      dbsNode.add(dbNode);
    }
    return dbsNode;
  }

  /** Gets all tables for the specified database */
  public static JsonNode getTables(String username, String dbName) throws Exception {
    ArrayNode tablesNode = jsonMapper.createArrayNode();
    Connection conn = null;
    Statement stat = null;
    ResultSet rs = null;
    try {
      List<String> tableNames = new ArrayList<>();
      conn = getConn(username, dbName);
      stat = conn.createStatement();
      rs = stat.executeQuery("show tables");
      while (rs.next()) {
        String tableName = rs.getString(1);
        tableNames.add(tableName);
      }

      //  Get detailed information about each table
      for (String tableName : tableNames) {
        ObjectNode tableNode = jsonMapper.createObjectNode();
        ResultSet describeRs = stat.executeQuery("DESCRIBE FORMATTED " + tableName);
        while (describeRs.next()) {
          String columnName = describeRs.getString(1);
          String dataType = describeRs.getString(2);
          if (null != columnName) {
            columnName = columnName.trim();
          }
          if (null != dataType) {
            dataType = dataType.trim();
          }
          if (columnName.contains("Owner:")) {
            tableNode.put("createdBy", dataType);
          }
          if (columnName.contains("CreateTime:")) {
            long createdAt = sdf.parse(dataType).getTime() / 1000;
            tableNode.put("createdAt", createdAt);
            break;
          }
        }
        describeRs.close();
        tableNode.put("databaseName", dbName);
        tableNode.put("tableName", tableName);
        tableNode.put("lastAccessAt", 0);
        tableNode.put("isView", false);
        tablesNode.add(tableNode);
      }
    } finally {
      close(conn, stat, rs);
    }

    return tablesNode;
  }

  /** Gets information about all fields of a specified table */
  public static JsonNode getColumns(String username, String dbName, String tbName)
      throws Exception {
    ArrayNode columnsNode = jsonMapper.createArrayNode();
    List<Map<String, Object>> columnMapList = new ArrayList<>();
    List<String> partitionColumnList = new ArrayList<>();
    Connection conn = null;
    Statement stat = null;
    ResultSet rs = null;
    try {
      conn = getConn(username, dbName);
      stat = conn.createStatement();
      rs = stat.executeQuery("desc " + tbName);
      while (rs.next()) {
        Map<String, Object> colum = new HashMap<>();
        String colName = rs.getString("col_name");
        String dataType = rs.getString("data_type");
        if (StringUtils.isNotBlank(colName)
            && StringUtils.isNotBlank(dataType)
            && !colName.contains("# Partition Information")
            && !colName.contains("# col_name")) {
          colum.put("columnName", rs.getString("col_name"));
          colum.put("columnType", rs.getString("data_type"));
          colum.put("columnComment", rs.getString("comment"));
          columnMapList.add(colum);
        }
      }

      boolean partition = false;
      boolean parColName = false;
      ResultSet describeRs = stat.executeQuery("DESCRIBE FORMATTED " + tbName);
      while (describeRs.next()) {
        String columnName = describeRs.getString(1);
        String dataType = describeRs.getString(2);
        if (null != columnName) {
          columnName = columnName.trim();
        }
        if (null != dataType) {
          dataType = dataType.trim();
        }

        // Partition field judgment
        if (columnName.contains("# Partition Information")) {
          partition = true;
          parColName = false;
          continue;
        }
        if (columnName.contains("# col_name")) {
          parColName = true;
          continue;
        }

        if (partition && parColName) {
          if ("".equals(columnName) && null == dataType) {
            partition = false;
            parColName = false;
          } else {
            partitionColumnList.add(columnName);
          }
        }
      }
      describeRs.close();
    } finally {
      close(conn, stat, rs);
    }

    for (Map<String, Object> map : columnMapList.stream().distinct().collect(Collectors.toList())) {
      ObjectNode fieldNode = jsonMapper.createObjectNode();
      String columnName = map.get("columnName").toString();
      fieldNode.put("columnName", columnName);
      fieldNode.put("columnType", map.get("columnType").toString());
      fieldNode.put("columnComment", map.get("columnComment").toString());
      fieldNode.put("partitioned", partitionColumnList.contains(columnName));
      columnsNode.add(fieldNode);
    }

    return columnsNode;
  }

  /** Close resource */
  private static void close(Connection conn, Statement stat, ResultSet rs) throws SQLException {
    if (rs != null) {
      rs.close();
    }
    if (stat != null) {
      stat.close();
    }
    if (conn != null) {
      conn.close();
    }
  }
}
