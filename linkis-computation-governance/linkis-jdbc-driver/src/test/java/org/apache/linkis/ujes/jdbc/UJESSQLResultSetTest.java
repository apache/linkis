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

import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.request.ResultSetAction;
import org.apache.linkis.ujes.client.response.ResultSetResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/*
 * Notice:
 * if you want to test this module,you must rewrite default parameters and SQL we used for local test
 * */

public class UJESSQLResultSetTest {

  private static LinkisSQLConnection conn;
  private LinkisSQLPreparedStatement preStatement;
  private UJESSQLResultSet resultSet;
  private UJESSQLResultSetMetaData metaData;

  @BeforeAll
  public static void getConnection() {
    try {
      conn = CreateConnection.getConnection();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (Exception e) {
      conn = null;
    }
  }

  @AfterAll
  public static void closeConnection() {
    if (conn != null) {
      conn.close();
    }
  }

  @BeforeEach
  public void getResultSet() {
    if (conn != null) {
      preStatement = conn.prepareStatement("show tables");
      preStatement.execute();
      resultSet = preStatement.getResultSet();
    }
  }

  @AfterEach
  public void closeStatement() {
    if (preStatement != null) {
      preStatement.close();
    }
  }

  @Test
  public void getObject() {
    if (conn != null) {
      while (resultSet.next()) {
        metaData = resultSet.getMetaData();
        int columnTypeFromVal = UJESSQLTypeParser.parserFromVal(resultSet.getObject(1));
        int columnTypeFromMetaData = metaData.getColumnType(1);
        Assertions.assertEquals(columnTypeFromVal, columnTypeFromMetaData);
      }
    }
  }

  @Test
  public void first() {
    if (conn != null) {
      resultSet.next();
      Object oldColumnVal = resultSet.getObject(1);
      while (resultSet.next()) {} // move to the end
      Assertions.assertTrue(resultSet.first());
      Object newColumnVal = resultSet.getObject(1);
      Assertions.assertSame(oldColumnVal, newColumnVal);
    }
  }

  @Test
  public void afterLast() {
    if (conn != null) {
      resultSet.next();
      resultSet.afterLast();
      Assertions.assertTrue(resultSet.isAfterLast());
    }
  }

  @Test
  public void beforeFirst() {
    if (conn != null) {
      resultSet.next();
      resultSet.beforeFirst();
      Assertions.assertTrue(resultSet.isBeforeFirst());
    }
  }

  @Test
  public void getMetaData() {
    if (conn != null) {
      resultSet.next();
      Assertions.assertNotNull(resultSet.getMetaData());
    }
  }

  @Test
  public void next() {
    if (conn != null) {
      while (resultSet.next()) {
        metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          System.out.print(metaData.getColumnName(i) + ":" + resultSet.getObject(i) + "    ");
        }
        System.out.println();
      }
      Assertions.assertTrue(resultSet.isAfterLast());
    }
  }

  /** single query result with no multiple result set check point 1: nextResultSet is null */
  @Test
  public void singleQueryWithNoMoreResultSet() {
    Properties t = new Properties();
    t.put("user", "hiveUser");
    UJESClient ujesClient = Mockito.mock(UJESClient.class);
    Mockito.when(ujesClient.resultSet(any())).thenReturn(new ResultSetResult());

    LinkisSQLConnection linkisSQLConnection = new LinkisSQLConnection(ujesClient, t);

    UJESSQLResultSet ujessqlResultSet =
        new UJESSQLResultSet(
            new String[] {"path1"}, new LinkisSQLStatement(linkisSQLConnection), 0, 0);

    ujessqlResultSet.next();

    assertNull(ujessqlResultSet.getNextResultSet());
  }

  /**
   * multiple result set with multi result switch is Y check point 1: queryResult has two path,
   * return first path. check point 2: the second result set returned check point 3: the third
   * result set is null
   */
  @Test
  public void nultiQueryWithMoreResultSet() {
    Properties t = new Properties();
    t.put("user", "hiveUser");
    t.put(UJESSQLDriverMain.ENABLE_MULTI_RESULT(), "Y");
    UJESClient ujesClient = Mockito.mock(UJESClient.class);
    List<String> pathList = new ArrayList<>();
    Mockito.when(ujesClient.resultSet(any()))
        .thenAnswer(
            invocationOnMock -> {
              ResultSetAction argument = invocationOnMock.getArgument(0);
              String path = (String) argument.getParameters().get("path");
              if (pathList.isEmpty()) {
                assertEquals("path1", path);
              }
              pathList.add(path);

              return new ResultSetResult();
            });
    LinkisSQLConnection linkisSQLConnection = new LinkisSQLConnection(ujesClient, t);

    UJESSQLResultSet ujessqlResultSet =
        new UJESSQLResultSet(
            new String[] {"path1", "path2"}, new LinkisSQLStatement(linkisSQLConnection), 0, 0);

    // 查询
    ujessqlResultSet.next();

    // 存在下一个结果集
    UJESSQLResultSet nextResultSet = ujessqlResultSet.getNextResultSet();
    assertNotNull(nextResultSet);
    nextResultSet.next();

    // 不存在第三个结果集
    assertNull(nextResultSet.getNextResultSet());
  }

  /**
   * multiple result set with multi result switch not Y check point 1: queryResult has two path,
   * return last path. check point 2: the next result set is null
   */
  @Test
  public void nultiQueryWithNoMoreResultSet() {
    Properties t = new Properties();
    t.put("user", "hiveUser");
    UJESClient ujesClient = Mockito.mock(UJESClient.class);
    Mockito.when(ujesClient.resultSet(any()))
        .thenAnswer(
            invocationOnMock -> {
              ResultSetAction argument = invocationOnMock.getArgument(0);
              String path = (String) argument.getParameters().get("path");
              assertEquals("path4", path);

              return new ResultSetResult();
            });

    LinkisSQLConnection linkisSQLConnection = new LinkisSQLConnection(ujesClient, t);

    UJESSQLResultSet ujessqlResultSet =
        new UJESSQLResultSet(
            new String[] {"path1", "path2", "path3", "path4"},
            new LinkisSQLStatement(linkisSQLConnection),
            0,
            0);

    // 查询
    ujessqlResultSet.next();

    // 即使查询有多个结果集，也不会产生多个结果集返回
    UJESSQLResultSet nextResultSet = ujessqlResultSet.getNextResultSet();
    assertNull(nextResultSet);
  }
}
