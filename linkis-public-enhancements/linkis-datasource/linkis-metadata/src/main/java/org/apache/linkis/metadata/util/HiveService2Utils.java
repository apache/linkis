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

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** @author Qin* */
public class HiveService2Utils {

  private static final String driverName = "org.apache.hive.jdbc.HiveDriver";

  private static final String defaultDb = "default";
  private static Connection conn = null;
  private static Statement stat = null;
  private static ResultSet rs = null;

  static ObjectMapper jsonMapper = new ObjectMapper();
  static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

  /** 判断是否启动hiveServer2查询左侧菜单栏 */
  public static Boolean checkHiveServer2Enable() {
    return DWSConfig.HIVE_SERVER2_ENABLE.getValue();
  }

  static String hiveServer2Address = DWSConfig.HIVE_SERVER2_URL.getValue();
  static String hiveServer2Username = DWSConfig.HIVE_SERVER2_USERNAME.getValue();
  static String hiveServer2Password = DWSConfig.HIVE_SERVER2_PASSWORD.getValue();

  /**
   * 获取链接
   *
   * @param username 用户名
   */
  private static void getConn(String username, String db) throws Exception {
    Class.forName(driverName);
    String url =
        hiveServer2Address.endsWith("/") ? hiveServer2Address + db : hiveServer2Address + "/" + db;
    if (StringUtils.isNotBlank(hiveServer2Username)) {
      username = hiveServer2Username;
    }
    conn = DriverManager.getConnection(url, username, hiveServer2Password);
    stat = conn.createStatement();
  }

  /** 获取数据库 */
  public static JsonNode getDbs(String username) throws Exception {
    ArrayNode dbsNode = jsonMapper.createArrayNode();
    List<String> dbs = new CopyOnWriteArrayList<>();
    try {
      getConn(username, defaultDb);
      rs = stat.executeQuery("show databases");
      while (rs.next()) {
        dbs.add(rs.getString(1));
      }
    } finally {
      destroy();
    }
    for (String db : dbs) {
      ObjectNode dbNode = jsonMapper.createObjectNode();
      dbNode.put("dbName", db);
      dbsNode.add(dbNode);
    }
    return dbsNode;
  }

  /**
   * 获取指定数据库的所有表
   *
   * @param dbName 数据库
   */
  public static JsonNode getTables(String username, String dbName) throws Exception {
    ArrayNode tablesNode = jsonMapper.createArrayNode();
    try {
      List<String> tableNames = new ArrayList<>();
      getConn(username, dbName);
      rs = stat.executeQuery("show tables");
      while (rs.next()) {
        String tableName = rs.getString(1);
        tableNames.add(tableName);
      }

      //  获取每个表的详细信息
      for (String tableName : tableNames) {
        ObjectNode tableNode = jsonMapper.createObjectNode();
        // 获取表详细信息
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
      destroy();
    }

    return tablesNode;
  }

  /**
   * 获取指定表所有字段信息
   *
   * @param dbName 数据库
   * @param tbName 数据表
   */
  public static JsonNode getColumns(String username, String dbName, String tbName)
      throws Exception {
    ArrayNode columnsNode = jsonMapper.createArrayNode();
    List<Map<String, Object>> columnMapList = new ArrayList<>();
    List<String> partitionColumnList = new ArrayList<>();
    try {
      getConn(username, dbName);
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

        // 判断获取分区字段
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
      destroy();
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

  // 释放资源
  private static void destroy() throws SQLException {
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
