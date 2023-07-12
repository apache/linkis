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

package org.apache.linkis.engineconnplugin.flink.client.sql.parser;

import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlParseException;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.OperationFactory;
import org.apache.linkis.engineconnplugin.flink.util.ClassUtil;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.flink.sql.parser.ddl.*;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.apache.flink.sql.parser.dql.*;
import org.apache.flink.sql.parser.impl.FlinkSqlParserImpl;
import org.apache.flink.sql.parser.validate.FlinkSqlConformance;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.*;

public class SqlCommandParserImpl implements SqlCommandParser {

  @Override
  public Optional<SqlCommandCall> parse(String stmt, boolean isBlinkPlanner)
      throws SqlParseException {
    // normalize
    String stmtForRegexMatch = stmt.trim();
    // remove ';' at the end
    if (stmtForRegexMatch.endsWith(";")) {
      stmtForRegexMatch = stmtForRegexMatch.substring(0, stmtForRegexMatch.length() - 1).trim();
    }

    // only parse gateway specific statements
    for (SqlCommand cmd : SqlCommand.values()) {
      if (cmd.hasPattern()) {
        final Matcher matcher = cmd.getPattern().matcher(stmtForRegexMatch);
        if (matcher.matches()) {
          final String[] groups = new String[matcher.groupCount()];
          for (int i = 0; i < groups.length; i++) {
            groups[i] = matcher.group(i + 1);
          }
          return cmd.getOperandConverter()
              .apply(groups)
              .map((operands) -> new SqlCommandCall(cmd, operands));
        }
      }
    }

    return parseStmt(stmt, isBlinkPlanner);
  }

  /**
   * Flink Parser only supports partial Operations, so we directly use Calcite Parser here. Once
   * Flink Parser supports all Operations, we should use Flink Parser instead of Calcite Parser.
   */
  private Optional<SqlCommandCall> parseStmt(String stmt, boolean isBlinkPlanner)
      throws SqlParseException {
    SqlParser.Config config = createSqlParserConfig(isBlinkPlanner);
    SqlParser sqlParser = SqlParser.create(stmt, config);
    SqlNodeList sqlNodes;
    try {
      sqlNodes = sqlParser.parseStmtList();
      // no need check the statement is valid here
    } catch (org.apache.calcite.sql.parser.SqlParseException e) {
      throw new SqlParseException(FAILED_PARSE_STATEMENT.getErrorDesc(), e);
    }
    if (sqlNodes.size() != 1) {
      throw new SqlParseException(ONLY_SINGLE_STATEMENT.getErrorDesc());
    }

    final String[] operands;
    final SqlCommand cmd;
    SqlNode node = sqlNodes.get(0);
    if (node.getKind().belongsTo(SqlKind.QUERY)) {
      cmd = SqlCommand.SELECT;
      operands = new String[] {stmt};
    } else if (node instanceof RichSqlInsert) {
      RichSqlInsert insertNode = (RichSqlInsert) node;
      cmd = insertNode.isOverwrite() ? SqlCommand.INSERT_OVERWRITE : SqlCommand.INSERT_INTO;
      operands = new String[] {stmt, insertNode.getTargetTable().toString()};
    } else if (node instanceof SqlShowTables) {
      cmd = SqlCommand.SHOW_TABLES;
      operands = new String[0];
    } else if (node instanceof SqlCreateTable) {
      cmd = SqlCommand.CREATE_TABLE;
      operands = new String[] {stmt};
    } else if (node instanceof SqlDropTable) {
      cmd = SqlCommand.DROP_TABLE;
      operands = new String[] {stmt};
    } else if (node instanceof SqlCreateCatalog) {
      cmd = SqlCommand.CREATE_CATALOG;
      operands = new String[] {stmt};
    } else if (node instanceof SqlDropCatalog) {
      cmd = SqlCommand.DROP_CATALOG;
      operands = new String[] {stmt};
    } else if (node instanceof SqlAlterTable) {
      cmd = SqlCommand.ALTER_TABLE;
      operands = new String[] {stmt};
    } else if (node instanceof SqlCreateView) {
      // TableEnvironment currently does not support creating view
      // so we have to perform the modification here
      SqlCreateView createViewNode = (SqlCreateView) node;
      cmd = SqlCommand.CREATE_VIEW;
      operands =
          new String[] {
            createViewNode.getViewName().toString(), createViewNode.getQuery().toString()
          };
    } else if (node instanceof SqlDropView) {
      // TableEnvironment currently does not support dropping view
      // so we have to perform the modification here
      SqlDropView dropViewNode = (SqlDropView) node;

      Field ifExistsField;
      try {
        ifExistsField = SqlDrop.class.getDeclaredField("ifExists");
      } catch (NoSuchFieldException e) {
        throw new SqlParseException(FAILED_DROP_STATEMENT.getErrorDesc(), e);
      }
      ifExistsField.setAccessible(true);
      boolean ifExists;
      try {
        ifExists = ifExistsField.getBoolean(dropViewNode);
      } catch (IllegalAccessException e) {
        throw new SqlParseException(FAILED_DROP_STATEMENT.getErrorDesc(), e);
      }

      cmd = SqlCommand.DROP_VIEW;
      operands = new String[] {dropViewNode.getViewName().toString(), String.valueOf(ifExists)};
    } else if (node instanceof SqlShowDatabases) {
      cmd = SqlCommand.SHOW_DATABASES;
      operands = new String[0];
    } else if (node instanceof SqlCreateDatabase) {
      cmd = SqlCommand.CREATE_DATABASE;
      operands = new String[] {stmt};
    } else if (node instanceof SqlDropDatabase) {
      cmd = SqlCommand.DROP_DATABASE;
      operands = new String[] {stmt};
    } else if (node instanceof SqlAlterDatabase) {
      cmd = SqlCommand.ALTER_DATABASE;
      operands = new String[] {stmt};
    } else if (node instanceof SqlShowCatalogs) {
      cmd = SqlCommand.SHOW_CATALOGS;
      operands = new String[0];
    } else if (node instanceof SqlShowFunctions) {
      cmd = SqlCommand.SHOW_FUNCTIONS;
      operands = new String[0];
    } else if (node instanceof SqlUseCatalog) {
      cmd = SqlCommand.USE_CATALOG;
      operands = new String[] {((SqlUseCatalog) node).getCatalogName().getSimple()};
    } else if (node instanceof SqlUseDatabase) {
      cmd = SqlCommand.USE;
      operands = new String[] {((SqlUseDatabase) node).getDatabaseName().toString()};
    } else if (node instanceof SqlRichDescribeTable) {
      cmd = SqlCommand.DESCRIBE_TABLE;
      // TODO support describe extended
      String[] fullTableName = ((SqlRichDescribeTable) node).fullTableName();
      String escapedName =
          Stream.of(fullTableName).map(s -> "`" + s + "`").collect(Collectors.joining("."));
      operands = new String[] {escapedName};
    } else if (node instanceof SqlExplain) {
      cmd = SqlCommand.EXPLAIN;
      // TODO support explain details
      operands = new String[] {((SqlExplain) node).getExplicandum().toString()};
    } else if (node instanceof SqlSetOption) {
      SqlSetOption setNode = (SqlSetOption) node;
      // refer to SqlSetOption#unparseAlterOperation
      if (setNode.getValue() != null) {
        cmd = SqlCommand.SET;
        operands = new String[] {setNode.getName().toString(), setNode.getValue().toString()};
      } else {
        cmd = SqlCommand.RESET;
        if ("ALL".equals(setNode.getName().toString().toUpperCase())) {
          operands = new String[0];
        } else {
          operands = new String[] {setNode.getName().toString()};
        }
      }
    } else {
      cmd = null;
      operands = new String[0];
    }

    if (cmd == null) {
      return Optional.empty();
    } else {
      // use the origin given statement to make sure
      // users can find the correct line number when parsing failed
      return Optional.of(new SqlCommandCall(cmd, operands));
    }
  }

  /**
   * A temporary solution. We can't get the default SqlParser config through table environment now.
   */
  private SqlParser.Config createSqlParserConfig(boolean isBlinkPlanner) {
    SqlParser.Config config =
        SqlParser.config()
            .withParserFactory(FlinkSqlParserImpl.FACTORY)
            .withConformance(FlinkSqlConformance.DEFAULT)
            .withLex(Lex.JAVA);
    if (isBlinkPlanner) {
      return config.withIdentifierMaxLength(256);
    } else {
      return config;
    }
  }

  private static SqlCommandParser sqlCommandParser;

  public static SqlCommandParser getInstance() {
    if (sqlCommandParser == null) {
      synchronized (OperationFactory.class) {
        if (sqlCommandParser == null) {
          sqlCommandParser =
              ClassUtil.getInstance(SqlCommandParser.class, new SqlCommandParserImpl());
        }
      }
    }
    return sqlCommandParser;
  }
}
