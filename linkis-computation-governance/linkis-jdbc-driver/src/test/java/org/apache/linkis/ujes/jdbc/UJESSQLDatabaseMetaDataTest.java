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

/*
 * Notice:
 * if you want to test this module,you must rewrite default parameters and SQL we used for local test
 * */

import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UJESSQLDatabaseMetaDataTest {
  private static UJESSQLConnection conn;
  private static UJESSQLDatabaseMetaData dbmd;

  @BeforeAll
  public static void preWork() {
    try {
      conn = CreateConnection.getConnection();
      dbmd = (UJESSQLDatabaseMetaData) conn.getMetaData();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void supportsMinimumSQLGrammar() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.supportsMinimumSQLGrammar());
  }

  @Test
  public void getResultSetHoldability() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.getResultSetHoldability());
  }

  @Test
  public void getMaxColumnsInGroupBy() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.getMaxColumnsInGroupBy());
  }

  @Test
  public void supportsSubqueriesInComparisons() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.supportsSubqueriesInComparisons());
  }

  @Test
  public void getMaxColumnsInSelect() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.getMaxColumnsInSelect());
  }

  @Test
  public void nullPlusNonNullIsNull() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.nullPlusNonNullIsNull());
  }

  @Test
  public void supportsCatalogsInDataManipulation() {
    assertFalse(dbmd.supportsCatalogsInDataManipulation());
  }

  @Test
  public void supportsDataDefinitionAndDataManipulationTransactions() {
    Assertions.assertThrows(
        UJESSQLException.class, () -> dbmd.supportsDataDefinitionAndDataManipulationTransactions());
  }

  @Test
  public void supportsTableCorrelationNames() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.supportsTableCorrelationNames());
  }

  @Test
  public void getDefaultTransactionIsolation() {
    assertEquals(dbmd.getDefaultTransactionIsolation(), 0);
  }

  @Test
  public void supportsFullOuterJoins() {
    assertTrue(dbmd.supportsFullOuterJoins());
  }

  @Test
  public void supportsExpressionsInOrderBy() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.supportsExpressionsInOrderBy());
  }

  @Test
  public void allProceduresAreCallable() {
    assertFalse(dbmd.allProceduresAreCallable());
  }

  @Test
  public void getMaxTablesInSelect() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.getMaxTablesInSelect());
  }

  @Test
  public void nullsAreSortedAtStart() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.nullsAreSortedAtStart());
  }

  @Test
  public void supportsPositionedUpdate() {
    assertFalse(dbmd.supportsPositionedUpdate());
  }

  @Test
  public void ownDeletesAreVisible() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.ownDeletesAreVisible(0));
  }

  @Test
  public void supportsResultSetHoldability() {
    assertFalse(dbmd.supportsResultSetHoldability(0));
  }

  @Test
  public void getMaxStatements() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.getMaxStatements());
  }

  @Test
  public void getRowIdLifetime() {
    Assertions.assertThrows(UJESSQLException.class, () -> dbmd.getRowIdLifetime());
  }

  @Test
  public void getDriverVersion() {
    assertEquals(dbmd.getDriverVersion(), String.valueOf(UJESSQLDriverMain.DEFAULT_VERSION()));
  }

  @AfterAll
  public static void closeStateAndConn() {
    conn.close();
    dbmd = null;
  }
}
