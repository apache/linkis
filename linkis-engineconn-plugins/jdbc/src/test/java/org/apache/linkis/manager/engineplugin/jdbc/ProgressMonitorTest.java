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

package org.apache.linkis.manager.engineplugin.jdbc;

import org.apache.linkis.manager.engineplugin.jdbc.monitor.ProgressMonitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import io.trino.jdbc.QueryStats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProgressMonitorTest {
  @Test
  @DisplayName("testProgressMonitor")
  public void testProgressMonitor() throws SQLException {
    ProgressMonitor<?> monitor = ProgressMonitor.attachMonitor(null);
    Assertions.assertNull(monitor);

    String url = "jdbc:trino://127.0.0.1:8080/hive/test";
    Properties properties = new Properties();
    properties.setProperty("user", "test");
    Connection connection = DriverManager.getConnection(url, properties);
    monitor = ProgressMonitor.attachMonitor(connection.createStatement());
    Assertions.assertNotNull(monitor);

    AtomicBoolean callbackFlag = new AtomicBoolean(false);
    monitor.callback(() -> callbackFlag.set(true));
    Assertions.assertFalse(callbackFlag.get());

    ProgressMonitor<QueryStats> trinoMonitor = (ProgressMonitor<QueryStats>) monitor;
    trinoMonitor.accept(
        new QueryStats(
            "testId",
            "testState",
            false,
            false,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            Optional.empty()));
    Assertions.assertTrue(callbackFlag.get());
  }
}
