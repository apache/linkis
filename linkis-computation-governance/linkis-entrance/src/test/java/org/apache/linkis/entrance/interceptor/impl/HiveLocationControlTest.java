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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HiveLocationControlTest - Unit tests for Hive LOCATION control feature
 *
 * <p>Tests the SQLExplain authPass method to ensure: - CREATE TABLE with LOCATION is blocked when
 * enabled - CREATE TABLE without LOCATION is allowed - ALTER TABLE SET LOCATION is NOT blocked (by
 * design) - Configuration toggle works correctly - Edge cases are handled properly
 */
class HiveLocationControlTest {

  private static final String CONFIG_KEY = "wds.linkis.hive.location.control.enable";

  @BeforeEach
  void setup() {
    // Reset configuration before each test
    BDPConfiguration.set(CONFIG_KEY, "false");
  }

  // ===== P0: Basic Interception Tests =====

  @Test
  void testBlockCreateTableWithLocationWhenEnabled() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test_table (id INT, name STRING) LOCATION '/user/hive/warehouse/test_table'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    String errorMsg = error.toString();
    Assertions.assertTrue(
        errorMsg.contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
    Assertions.assertTrue(
        errorMsg.contains("Please remove the LOCATION clause and retry"),
        "Error message should contain 'Please remove the LOCATION clause and retry'");
  }

  @Test
  void testAllowCreateTableWithoutLocationWhenEnabled() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test_table (id INT, name STRING)";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithLocationWhenDisabled() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test_table (id INT) LOCATION '/any/path'";

    BDPConfiguration.set(CONFIG_KEY, "false");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P0: EXTERNAL TABLE Tests =====

  @Test
  void testBlockCreateExternalTableWithLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE EXTERNAL TABLE external_table (id INT) LOCATION '/user/data/external'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  @Test
  void testAllowCreateExternalTableWithoutLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE EXTERNAL TABLE external_table (id INT)";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P0: ALTER TABLE Tests =====

  @Test
  void testAllowAlterTableSetLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "ALTER TABLE test_table SET LOCATION '/new/location'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    // ALTER TABLE SET LOCATION is NOT blocked by design
    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowAlterTableOtherOperations() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "ALTER TABLE test_table ADD COLUMNS (new_col INT)";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P1: Case Sensitivity Tests =====

  @Test
  void testCaseInsensitiveForCreateTable() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "create table test (id int) location '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  @Test
  void testCaseInsensitiveForLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) location '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  @Test
  void testCaseInsensitiveMixed() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CrEaTe TaBlE test (id INT) LoCaTiOn '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  // ===== P1: Multi-line SQL Tests =====

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
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  @Test
  void testMultiLineCreateTableWithComplexSchema() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE complex_table (\n"
            + "  id INT COMMENT 'Primary key',\n"
            + "  name STRING COMMENT 'User name',\n"
            + "  age INT COMMENT 'User age',\n"
            + "  created_date TIMESTAMP COMMENT 'Creation date'\n"
            + ")\n"
            + "COMMENT 'This is a complex table'\n"
            + "PARTITIONED BY (year INT, month INT)\n"
            + "STORED AS PARQUET\n"
            + "LOCATION '/user/hive/warehouse/complex_table'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  // ===== P1: Different Quote Types Tests =====

  @Test
  void testHandleLocationWithDoubleQuotes() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) LOCATION \"/user/data\"";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  @Test
  void testHandleLocationWithBackticks() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) LOCATION `/user/data`";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  @Test
  void testHandleLocationWithMixedQuotes() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    // Test with escaped quotes
    String sql = "CREATE TABLE test (id INT) LOCATION '/user/data\\'s'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
  }

  // ===== P1: Comment Handling Tests =====

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
  void testIgnoreLocationInMultiLineComments() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "/* CREATE TABLE test LOCATION '/path' */\n"
            + "CREATE TABLE test (id INT) -- Another comment\n"
            + "STORED AS PARQUET";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testBlockLocationAfterComments() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "-- This is a comment\n"
            + "CREATE TABLE test (id INT)\n"
            + "-- Another comment\n"
            + "LOCATION '/user/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  // ===== P2: Edge Cases Tests =====

  @Test
  void testHandleEmptySQL() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    // Empty SQL should be allowed (fail-open)
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
  void testHandleWhitespaceOnlySQL() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "   \n\t  \r\n  ";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    // Whitespace-only SQL should be allowed
    Assertions.assertTrue(result);
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
    // The original SQL should be truncated in error message
    Assertions.assertFalse(
        error.toString().contains(longSql), "Error message should not contain the full long SQL");
    Assertions.assertTrue(
        error.toString().contains("..."), "Error message should contain truncation indicator");
  }

  // ===== P2: Other Statement Types Tests =====

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

  @Test
  void testNotBlockTruncateTableStatements() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "TRUNCATE TABLE test";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P2: Multiple Statements Tests =====

  @Test
  void testHandleMultipleStatements() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test1 (id INT); "
            + "CREATE TABLE test2 (id INT) LOCATION '/user/data'; "
            + "SELECT * FROM test1";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    // Should block because one statement contains LOCATION
    Assertions.assertFalse(result);
  }

  // ===== P2: Complex Table Definitions Tests =====

  @Test
  void testAllowCreateTableWithPartitionedBy() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test (id INT, name STRING) PARTITIONED BY (dt STRING) STORED AS PARQUET";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithClusteredBy() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test (id INT, name STRING) CLUSTERED BY (id) INTO 32 BUCKETS STORED AS ORC";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithSortedBy() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT, name STRING) SORTED BY (id ASC) STORED AS PARQUET";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P2: CTAS (Create Table As Select) Tests =====

  @Test
  void testBlockCTASWithLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE new_table LOCATION '/user/data' AS SELECT * FROM source_table";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertFalse(result);
    Assertions.assertTrue(
        error.toString().contains("LOCATION clause is not allowed"),
        "Error message should contain 'LOCATION clause is not allowed'");
  }

  @Test
  void testAllowCTASWithoutLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE new_table AS SELECT * FROM source_table";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P2: Temporary Tables Tests =====

  @Test
  void testAllowCreateTemporaryTable() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TEMPORARY TABLE temp_table (id INT)";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTemporaryTableWithLocation() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TEMPORARY TABLE temp_table (id INT) LOCATION '/tmp/data'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    // Temporary tables with LOCATION should be allowed
    Assertions.assertTrue(result);
  }

  // ===== P2: LIKE and SERDE Tests =====

  @Test
  void testAllowCreateTableLike() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE new_table LIKE existing_table";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithRowFormat() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test (id INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithSerde() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test (id INT) ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerde'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P2: Skewed and Stored As Tests =====

  @Test
  void testAllowCreateTableWithSkewedBy() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) SKEWED BY (id) ON (1, 10, 100) STORED AS DIRECTORIES";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithVariousStorageFormats() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) STORED AS PARQUET";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithStorageFormat() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test (id INT) STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P2: Location in String Constants Tests =====

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
  void testAllowLocationInFunctionParameters() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "SELECT concat('location: ', '/user/data') as path FROM test WHERE id = "
            + "(SELECT id FROM other_table WHERE location_type = 'local')";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  // ===== P2: Table Properties Tests =====

  @Test
  void testAllowCreateTableWithTblproperties() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql =
        "CREATE TABLE test (id INT) TBLPROPERTIES ('comment'='This is a test table', 'author'='test')";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }

  @Test
  void testAllowCreateTableWithExternalFalse() {
    scala.collection.mutable.StringBuilder error = new scala.collection.mutable.StringBuilder();
    String sql = "CREATE TABLE test (id INT) EXTERNAL FALSE";

    BDPConfiguration.set(CONFIG_KEY, "true");
    boolean result = SQLExplain.authPass(sql, error);

    Assertions.assertTrue(result);
    Assertions.assertEquals("", error.toString());
  }
}
