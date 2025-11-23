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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCDataSourceConfigurations {
  private static final Logger LOG = LoggerFactory.getLogger(JDBCDataSourceConfigurations.class);
  private final Map<String, Statement> taskIdStatementMap;

  public JDBCDataSourceConfigurations() {
    taskIdStatementMap = new ConcurrentHashMap<>();
  }

  public void initTaskIdStatementMap() throws SQLException {
    for (Statement statement : taskIdStatementMap.values()) {
      if (statement != null && !statement.isClosed()) {
        statement.close();
      }
    }
    taskIdStatementMap.clear();
    LOG.info("The jdbc task statement map has be cleared successfully!");
  }

  public void saveStatement(String taskId, Statement statement) {
    taskIdStatementMap.put(taskId, statement);
  }

  public void cancelStatement(String taskId) throws SQLException {
    LOG.info("Starting to cancel the statement of task {} ...", taskId);
    Statement statement = taskIdStatementMap.get(taskId);
    if (statement != null) {
      statement.cancel();
    }
    LOG.info("Finished cancel the statement of task {}.", taskId);
  }

  public void removeStatement(String taskId) {
    taskIdStatementMap.remove(taskId);
    LOG.info("Finished remove the statement of task {}", taskId);
  }
}
