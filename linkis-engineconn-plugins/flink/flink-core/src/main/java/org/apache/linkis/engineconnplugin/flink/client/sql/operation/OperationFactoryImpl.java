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

package org.apache.linkis.engineconnplugin.flink.client.sql.operation;

import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlParseException;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.impl.*;
import org.apache.linkis.engineconnplugin.flink.client.sql.parser.SqlCommandCall;
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import org.apache.linkis.engineconnplugin.flink.util.ClassUtil;

import java.text.MessageFormat;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.ONLY_RESET_ALL;
import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.SUPPORTED_COMMAND_CALL;

public class OperationFactoryImpl implements OperationFactory {

  private OperationFactoryImpl() {}

  @Override
  public Operation createOperation(SqlCommandCall call, FlinkEngineConnContext context)
      throws SqlParseException {
    Operation operation;
    switch (call.command) {
      case SELECT:
        operation = new SelectOperation(context, call.operands[0]);
        break;
      case CREATE_VIEW:
        operation = new CreateViewOperation(context, call.operands[0], call.operands[1]);
        break;
      case DROP_VIEW:
        operation =
            new DropViewOperation(
                context, call.operands[0], Boolean.parseBoolean(call.operands[1]));
        break;
      case CREATE_TABLE:
      case DROP_TABLE:
      case ALTER_TABLE:
      case CREATE_CATALOG:
      case DROP_CATALOG:
      case CREATE_DATABASE:
      case DROP_DATABASE:
      case ALTER_DATABASE:
        operation = new DDLOperation(context, call.operands[0], call.command);
        break;
      case SET:
        // list all properties
        if (call.operands.length == 0) {
          operation = new SetOperation(context);
        } else {
          // set a property
          operation = new SetOperation(context, call.operands[0], call.operands[1]);
        }
        break;
      case RESET:
        if (call.operands.length > 0) {
          throw new SqlParseException(ONLY_RESET_ALL.getErrorDesc());
        }
        operation = new ResetOperation(context);
        break;
      case USE_CATALOG:
        operation = new UseCatalogOperation(context, call.operands[0]);
        break;
      case USE:
        operation = new UseDatabaseOperation(context, call.operands[0]);
        break;
      case INSERT_INTO:
      case INSERT_OVERWRITE:
        operation = new InsertOperation(context, call.operands[0], call.operands[1]);
        break;
      case SHOW_MODULES:
        operation = new ShowModulesOperation(context);
        break;
      case SHOW_CATALOGS:
        operation = new ShowCatalogsOperation(context);
        break;
      case SHOW_CURRENT_CATALOG:
        operation = new ShowCurrentCatalogOperation(context);
        break;
      case SHOW_DATABASES:
        operation = new ShowDatabasesOperation(context);
        break;
      case SHOW_CURRENT_DATABASE:
        operation = new ShowCurrentDatabaseOperation(context);
        break;
      case SHOW_TABLES:
        operation = new ShowTablesOperation(context);
        break;
      case SHOW_VIEWS:
        operation = new ShowViewsOperation(context);
        break;
      case SHOW_FUNCTIONS:
        operation = new ShowFunctionsOperation(context);
        break;
      case DESCRIBE_TABLE:
        operation = new DescribeTableOperation(context, call.operands[0]);
        break;
      case EXPLAIN:
        operation = new ExplainOperation(context, call.operands[0]);
        break;
      default:
        throw new SqlParseException(
            MessageFormat.format(SUPPORTED_COMMAND_CALL.getErrorDesc(), call));
    }
    return operation;
  }

  private static OperationFactory operationFactory;

  public static OperationFactory getInstance() {
    if (operationFactory == null) {
      synchronized (OperationFactory.class) {
        if (operationFactory == null) {
          operationFactory =
              ClassUtil.getInstance(OperationFactory.class, new OperationFactoryImpl());
        }
      }
    }
    return operationFactory;
  }
}
