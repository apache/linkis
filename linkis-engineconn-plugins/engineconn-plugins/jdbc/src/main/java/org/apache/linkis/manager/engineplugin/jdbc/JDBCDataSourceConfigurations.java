/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

public class JDBCDataSourceConfigurations {
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
    }

    public void saveStatement(String taskId, Statement statement) {
        taskIdStatementMap.put(taskId, statement);
    }

    public void cancelStatement(String taskId) throws SQLException {
        Statement statement = taskIdStatementMap.get(taskId);
        if (statement != null) {
            statement.cancel();
        }
    }

    public void removeStatement(String taskId) {
        taskIdStatementMap.remove(taskId);
    }
}
