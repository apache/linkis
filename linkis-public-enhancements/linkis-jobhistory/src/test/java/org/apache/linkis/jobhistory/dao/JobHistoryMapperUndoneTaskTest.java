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

package org.apache.linkis.jobhistory.dao;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.InputStream;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers the queries used by {@code DefaultQueryCacheManager#refreshUndoneTask} with both the MySQL
 * and the PostgreSQL mapper. The Spring based DAO tests only load the MySQL mapper, so a plain
 * MyBatis session is used here.
 */
class JobHistoryMapperUndoneTaskTest {

  private static final Date START = Timestamp.valueOf("2026-01-01 00:00:00");
  private static final Date END = Timestamp.valueOf("2026-01-02 00:00:00");

  @Test
  void searchWithIdOrderAscOnPostgresql() throws Exception {
    try (SqlSession session = openSession("postgresql", "PostgreSQL")) {
      List<Integer> ids =
          session
              .getMapper(JobHistoryMapper.class)
              .searchWithIdOrderAsc(
                  START, END, 3L, Arrays.asList("Running", "Inited", "Scheduled"));
      assertEquals(Arrays.asList(4, 5), ids);
    }
  }

  @ParameterizedTest
  @CsvSource({"mysql, MySQL", "postgresql, PostgreSQL"})
  void maxID(String dialect, String h2Mode) throws Exception {
    try (SqlSession session = openSession(dialect, h2Mode)) {
      assertEquals(
          Integer.valueOf(6), session.getMapper(JobHistoryMapper.class).maxID(START, END, 3L));
    }
  }

  private static SqlSession openSession(String dialect, String h2Mode) throws Exception {
    UnpooledDataSource dataSource =
        new UnpooledDataSource(
            "org.h2.Driver", "jdbc:h2:mem:" + dialect + ";MODE=" + h2Mode, "sa", "");
    Configuration configuration =
        new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
    String resource = "mapper/" + dialect + "/JobHistoryMapper.xml";
    try (InputStream input = Resources.getResourceAsStream(resource)) {
      new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
    }
    SqlSession session = new SqlSessionFactoryBuilder().build(configuration).openSession(true);
    try (Statement statement = session.getConnection().createStatement()) {
      statement.execute(
          "CREATE TABLE linkis_ps_job_history_group_history "
              + "(id BIGINT, status VARCHAR(50), created_time TIMESTAMP)");
      // Inserted in descending id order so that a missing ORDER BY shows up.
      statement.execute(
          "INSERT INTO linkis_ps_job_history_group_history VALUES "
              + "(7, 'Running', '2026-01-02 01:00:00'), "
              + "(6, 'Running', '2026-01-01 13:00:00'), "
              + "(5, 'Inited', '2026-01-01 12:00:00'), "
              + "(4, 'Scheduled', '2026-01-01 11:00:00'), "
              + "(3, 'Succeed', '2026-01-01 10:00:00'), "
              + "(2, 'Running', '2026-01-01 09:00:00'), "
              + "(1, 'Running', '2025-12-31 23:00:00')");
    }
    return session;
  }
}
