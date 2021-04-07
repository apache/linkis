/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.impl;

import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;
import org.apache.flink.table.client.config.entries.*;
import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.SessionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.NonJobOperation;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.OperationUtil;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlExecutionException;

import org.apache.flink.table.api.TableEnvironment;

/**
 * Operation for CREATE VIEW command.
 */
public class CreateViewOperation implements NonJobOperation {
	private final ExecutionContext<?> context;
	private final String viewName;
	private final String query;

	public CreateViewOperation(SessionContext context, String viewName, String query) {
		this.context = context.getExecutionContext();
		this.viewName = viewName;
		this.query = query;
	}

	@Override
	public ResultSet execute() {
		Environment env = context.getEnvironment();
		TableEntry tableEntry = env.getTables().get(viewName);
		if (tableEntry instanceof ViewEntry) {
			throw new SqlExecutionException("'" + viewName + "' has already been defined in the current session.");
		}

		// TODO check the logic
		TableEnvironment tableEnv = context.getTableEnvironment();
		try {
			context.wrapClassLoader(() -> {
				tableEnv.createTemporaryView(viewName, tableEnv.sqlQuery(query));
				return null;
			});
		} catch (Throwable t) {
			// catch everything such that the query does not crash the executor
			throw new SqlExecutionException("Invalid SQL statement.", t);
		}
		// Also attach the view to ExecutionContext#environment.
		env.getTables().put(viewName, ViewEntry.create(viewName, query));
		return OperationUtil.OK;
	}
}
