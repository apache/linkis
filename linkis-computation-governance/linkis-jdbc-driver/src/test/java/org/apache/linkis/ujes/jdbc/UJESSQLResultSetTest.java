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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
