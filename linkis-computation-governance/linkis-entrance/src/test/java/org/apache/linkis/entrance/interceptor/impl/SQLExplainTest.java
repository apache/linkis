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

package org.apache.linkis.entrance.interceptor.impl;

import org.apache.linkis.common.conf.BDPConfiguration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SQLExplainTest {

  private static final String CONFIG_KEY = "wds.linkis.hive.location.control.enable";

  @Test
  void isSelectCmdNoLimit() {

    String code = "SELECT * from dual WHERE (1=1)LIMIT 1;";
    boolean res = SQLExplain.isSelectCmdNoLimit(code);
    Assertions.assertEquals(false, res);

    code = "SELECT * from dual";
    res = SQLExplain.isSelectCmdNoLimit(code);
    Assertions.assertEquals(true, res);

    code = "SELECT * from dual LIMIT 1;";
    res = SQLExplain.isSelectCmdNoLimit(code);
    Assertions.assertEquals(false, res);
  }

  @Test
  void isSelectOverLimit() {
    String code = "SELECT * from dual WHERE (1=1)LIMIT 5001;";
    boolean res = SQLExplain.isSelectOverLimit(code);
    Assertions.assertEquals(true, res);

    code = "SELECT * from dual";
    res = SQLExplain.isSelectOverLimit(code);
    Assertions.assertEquals(false, res);

    code = "SELECT * from dual LIMIT 4000;";
    res = SQLExplain.isSelectOverLimit(code);
    Assertions.assertEquals(false, res);
  }

  // ===== Hive Location Control Tests =====

  @Test
  void testBlockCreateTableWithLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) LOCATION '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(error.toString().contains("LOCATION clause is not allowed"));
  }

  @Test
  void testAllowCreateTableWithoutLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT)";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowAlterTableSetLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "ALTER TABLE test SET LOCATION '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowWhenConfigDisabled() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) LOCATION '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "false");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testBlockExternalTableWithLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE EXTERNAL TABLE test (id INT) LOCATION '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(error.toString().contains("LOCATION clause is not allowed"));
  }

  @Test
  void testIgnoreLocationInComments() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "-- CREATE TABLE test LOCATION '/path'\nCREATE TABLE test (id INT)";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowLocationInStringConstants() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "SELECT * FROM test WHERE comment = 'this location is ok'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testHandleEmptySQL() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
  }

  @Test
  void testHandleNullSQL() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = null;

    BDPConfiguration.set(CONFIG_KEY, "true");
    // Should not throw exception and should return true (fail-open)
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
  }

  @Test
  void testCaseInsensitiveForCreateTable() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "create table test (id int) location '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(error.toString().contains("LOCATION clause is not allowed"));
  }

  @Test
  void testCaseInsensitiveForLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) location '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(error.toString().contains("LOCATION clause is not allowed"));
  }

  @Test
  void testMultiLineCreateTableWithLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test (\n"
            + "  id INT,\n"
            + "  name STRING\n"
            + ")\n"
            + "LOCATION '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(error.toString().contains("LOCATION clause is not allowed"));
  }

  @Test
  void testAllowCreateTableWithOtherClauses() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) PARTITIONED BY (dt STRING) STORED AS PARQUET";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testHandleLocationWithDoubleQuotes() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) LOCATION \"/user/data\"";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(error.toString().contains("LOCATION clause is not allowed"));
  }

  @Test
  void testHandleLocationWithBackticks() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) LOCATION `/user/data`";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(error.toString().contains("LOCATION clause is not allowed"));
  }

  @Test
  void testTruncateLongSQLErrorMessage() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String longSql =
        "CREATE TABLE test (id INT) LOCATION '/user/very/long/path/"
            + "that/keeps/going/on/and/on/forever/and/ever/because/it/is/just/so/long/"
            + "and/needs/to/be/truncated/in/the/error/message'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(longSql, error);

    Assertions.assertFalse(result);
    Assertions.assertFalse(error.toString().contains(longSql));
    Assertions.assertTrue(error.toString().contains("..."));
  }

  @Test
  void testNotBlockInsertStatements() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "INSERT INTO TABLE test VALUES (1, 'test')";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testNotBlockSelectStatements() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "SELECT * FROM test WHERE id > 100";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testNotBlockDropTableStatements() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "DROP TABLE test";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }
}
