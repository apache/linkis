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

package org.apache.linkis.engineconnplugin.flink.client.sql.operation.impl;

import org.apache.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import org.apache.linkis.engineconnplugin.flink.client.shims.config.Environment;
import org.apache.linkis.engineconnplugin.flink.client.shims.config.entries.TableEntry;
import org.apache.linkis.engineconnplugin.flink.client.shims.config.entries.ViewEntry;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlExecutionException;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.NonJobOperation;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.OperationUtil;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.NOT_EXIST_SESSION;

/** Operation for DROP VIEW command. */
public class DropViewOperation implements NonJobOperation {
  private final FlinkEngineConnContext context;
  private final String viewName;
  private final boolean ifExists;

  public DropViewOperation(FlinkEngineConnContext context, String viewName, boolean ifExists) {
    this.context = context;
    this.viewName = viewName;
    this.ifExists = ifExists;
  }

  @Override
  public ResultSet execute() throws SqlExecutionException {
    Environment env = context.getExecutionContext().getEnvironment();
    TableEntry tableEntry = env.getTables().get(viewName);
    if (!(tableEntry instanceof ViewEntry) && !ifExists) {
      throw new SqlExecutionException("'" + viewName + "' " + NOT_EXIST_SESSION.getErrorDesc());
    }

    // Here we rebuild the ExecutionContext because we want to ensure that all the remaining
    // views can work fine.
    // Assume the case:
    //   view1=select 1;
    //   view2=select * from view1;
    // If we delete view1 successfully, then query view2 will throw exception because view1 does
    // not exist. we want
    // all the remaining views are OK, so do the ExecutionContext rebuilding to avoid breaking
    // the view dependency.
    Environment newEnv = env.clone();
    if (newEnv.getTables().remove(viewName) != null) {
      ExecutionContext oldExecutionContext = context.getExecutionContext();
      oldExecutionContext.wrapClassLoader(tableEnv -> tableEnv.dropTemporaryView(viewName));
      // Renew the ExecutionContext.
      ExecutionContext.Builder builder =
          context
              .newExecutionContextBuilder(context.getEnvironmentContext().getDefaultEnv())
              .env(newEnv)
              .sessionState(context.getExecutionContext().getSessionState());
      context.setExecutionContext(context.getExecutionContext().cloneExecutionContext(builder));
    }

    return OperationUtil.OK;
  }
}
