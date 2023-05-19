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
  private static LinkisSQLConnection conn;
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
    } catch (Exception e) {
      dbmd = null;
    }
  }

  @Test
  public void supportsMinimumSQLGrammar() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.supportsMinimumSQLGrammar());
    }
  }

  @Test
  public void getResultSetHoldability() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.getResultSetHoldability());
    }
  }

  @Test
  public void getMaxColumnsInGroupBy() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.getMaxColumnsInGroupBy());
    }
  }

  @Test
  public void supportsSubqueriesInComparisons() {
    if (dbmd != null) {
      Assertions.assertThrows(
          LinkisSQLException.class, () -> dbmd.supportsSubqueriesInComparisons());
    }
  }

  @Test
  public void getMaxColumnsInSelect() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.getMaxColumnsInSelect());
    }
  }

  @Test
  public void nullPlusNonNullIsNull() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.nullPlusNonNullIsNull());
    }
  }

  @Test
  public void supportsCatalogsInDataManipulation() {
    if (dbmd != null) {
      assertFalse(dbmd.supportsCatalogsInDataManipulation());
    }
  }

  @Test
  public void supportsDataDefinitionAndDataManipulationTransactions() {
    if (dbmd != null) {
      Assertions.assertThrows(
          LinkisSQLException.class,
          () -> dbmd.supportsDataDefinitionAndDataManipulationTransactions());
    }
  }

  @Test
  public void supportsTableCorrelationNames() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.supportsTableCorrelationNames());
    }
  }

  @Test
  public void getDefaultTransactionIsolation() {
    if (dbmd != null) {
      assertEquals(dbmd.getDefaultTransactionIsolation(), 0);
    }
  }

  @Test
  public void supportsFullOuterJoins() {
    if (dbmd != null) {
      assertTrue(dbmd.supportsFullOuterJoins());
    }
  }

  @Test
  public void supportsExpressionsInOrderBy() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.supportsExpressionsInOrderBy());
    }
  }

  @Test
  public void allProceduresAreCallable() {
    if (dbmd != null) {
      assertFalse(dbmd.allProceduresAreCallable());
    }
  }

  @Test
  public void getMaxTablesInSelect() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.getMaxTablesInSelect());
    }
  }

  @Test
  public void nullsAreSortedAtStart() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.nullsAreSortedAtStart());
    }
  }

  @Test
  public void supportsPositionedUpdate() {
    if (dbmd != null) {
      assertFalse(dbmd.supportsPositionedUpdate());
    }
  }

  @Test
  public void ownDeletesAreVisible() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.ownDeletesAreVisible(0));
    }
  }

  @Test
  public void supportsResultSetHoldability() {
    if (dbmd != null) {
      assertFalse(dbmd.supportsResultSetHoldability(0));
    }
  }

  @Test
  public void getMaxStatements() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.getMaxStatements());
    }
  }

  @Test
  public void getRowIdLifetime() {
    if (dbmd != null) {
      Assertions.assertThrows(LinkisSQLException.class, () -> dbmd.getRowIdLifetime());
    }
  }

  @Test
  public void getDriverVersion() {
    if (dbmd != null) {
      assertEquals(dbmd.getDriverVersion(), String.valueOf(UJESSQLDriverMain.DEFAULT_VERSION()));
    }
  }

  @AfterAll
  public static void closeStateAndConn() {
    if (conn != null) {
      conn.close();
    }
    dbmd = null;
  }
}
