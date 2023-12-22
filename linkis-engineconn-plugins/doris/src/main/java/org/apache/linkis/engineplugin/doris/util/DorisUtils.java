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

package org.apache.linkis.engineplugin.doris.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DorisUtils {

  private static final Logger logger = LoggerFactory.getLogger(DorisUtils.class);

  private static final String JDBC_URL = "jdbc:mysql://%s:%s/%s";

  public static List<String> getDorisCloumns(
      String host, Integer port, String username, String password, String database, String table) {
    String url = String.format(JDBC_URL, host, port, database);

    Connection connecion = getConnecion(username, password, url);
    if (connecion == null) {
      return Collections.emptyList();
    }

    String columnSql = "SELECT * FROM `" + database + "`.`" + table + "` WHERE 1 = 2";
    PreparedStatement ps = null;
    ResultSet rs = null;
    ResultSetMetaData meta = null;
    List<String> columns = new ArrayList<>();

    try {
      ps = connecion.prepareStatement(columnSql);
      rs = ps.executeQuery();
      meta = rs.getMetaData();
      int columnCount = meta.getColumnCount();
      for (int i = 1; i < columnCount + 1; i++) {
        columns.add(meta.getColumnName(i));
      }
    } catch (SQLException e) {
      logger.error("getDorisCloumns failed", e);
      return columns;
    } finally {
      closeResource(connecion, ps, rs);
    }
    return columns;
  }

  public static void closeResource(
      Connection connection, Statement statement, ResultSet resultSet) {
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
      logger.warn("Fail to release resource [" + e.getMessage() + "]", e);
    }
  }

  private static Connection getConnecion(String username, String password, String url) {
    Connection connection = null;

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException ex) {
        logger.warn(
            "The mysql driver does not exist, mysql driver is used to fetch the table column. If you need to use this feature, you need to place the mysql jar into the doris ec lib.  ClassNotFoundException: {}",
            ex.getMessage());
        return connection;
      }
    }

    try {
      connection = DriverManager.getConnection(url, username, password);
    } catch (Exception e) {
      logger.warn(
          "getConnecion failed,please check whether the connection parameters are correct.", e);
    }
    return connection;
  }
}
