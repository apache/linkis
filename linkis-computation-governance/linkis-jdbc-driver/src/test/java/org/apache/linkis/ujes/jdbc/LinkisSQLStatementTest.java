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

package org.apache.linkis.ujes.jdbc;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Notice:
 * if you want to test this module,you must rewrite default parameters and SQL we used for local test
 * */

public class LinkisSQLStatementTest {
  private static LinkisSQLConnection conn;
  private static LinkisSQLStatement statement;
  private static int maxRows;
  private static int queryTimeout;
  private static String sql;
  private static String sqlCreate;
  private static String sqlInsert;
  private static String sqlSelect;
  private static String sqlDrop;

  @BeforeAll
  public static void createConnection() {
    try {
      conn = CreateConnection.getConnection();
      statement = (LinkisSQLStatement) conn.createStatement();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (Exception e) {
      statement = null;
    }
  }

  @BeforeEach
  public void setParams() {
    sql = "show tables";
    sqlCreate = "CREATE TABLE if not exists db.test1236 as select * from ai_fmi_ods.1000_10";
    sqlInsert = "insert into db.test1236 select * from ai_fmi_ods.1000_10 limit 10";
    sqlSelect = "select * from db.test1236";
    sqlDrop = "drop table db.test1236";
    maxRows = 100;
    queryTimeout = 10000;
  }

  @Test
  public void execute() {
    if (statement != null) {
      assertTrue(statement.execute(sql));
    }
  }

  @Test
  public void executeQuery() {
    if (statement != null) {
      UJESSQLResultSet resultSet = statement.executeQuery(sql);
      assertTrue(resultSet.next());
    }
  }

  @Test
  public void crud() {
    if (statement != null) {
      statement.executeQuery(sqlCreate);
      statement.executeQuery(sqlInsert);
      UJESSQLResultSet resultSet = statement.executeQuery(sqlSelect);
      int columnCount = 0;
      while (resultSet.next()) {
        UJESSQLResultSetMetaData rsmd = resultSet.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
          System.out.print(
              rsmd.getColumnName(i)
                  + ":"
                  + rsmd.getColumnTypeName(i)
                  + ":"
                  + resultSet.getObject(i)
                  + "   ");
          columnCount = i;
        }
      }
      System.out.println(columnCount);
      assertTrue(resultSet.isAfterLast());
      statement.executeQuery(sqlDrop);
    }
  }

  @Test
  public void setMaxRows() {
    if (statement != null) {
      statement.setMaxRows(maxRows);
      assertEquals(maxRows, statement.getMaxRows());
    }
  }

  @Test
  public void setQueryTimeout() {
    if (statement != null) {
      statement.setQueryTimeout(queryTimeout);
      assertEquals(statement.getQueryTimeout(), queryTimeout * 1000);
    }
  }

  @Test
  public void cancel() {
    if (statement != null) {
      statement.executeQuery(sql);
      statement.cancel();
      assertNull(statement.getResultSet());
      assertNull(statement.getJobExcuteResult());
    }
  }

  @Test
  public void getConnWhenIsClosed() {
    if (statement != null) {
      assertEquals(statement.getConnection(), conn);
    }
  }

  @AfterAll
  public static void closeStateAndConn() {
    if (statement != null) {
      statement.close();
    }
    if (conn != null) {
      conn.close();
    }
  }
}
