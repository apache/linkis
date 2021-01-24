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

package com.webank.wedatasphere.linkis.engine.flink.client.utils;

import com.webank.wedatasphere.linkis.engine.flink.exception.SqlParseException;
import org.apache.flink.sql.parser.ddl.SqlAlterDatabase;
import org.apache.flink.sql.parser.ddl.SqlAlterTable;
import org.apache.flink.sql.parser.ddl.SqlCreateDatabase;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlCreateView;
import org.apache.flink.sql.parser.ddl.SqlDropDatabase;
import org.apache.flink.sql.parser.ddl.SqlDropTable;
import org.apache.flink.sql.parser.ddl.SqlDropView;
import org.apache.flink.sql.parser.ddl.SqlUseCatalog;
import org.apache.flink.sql.parser.ddl.SqlUseDatabase;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.apache.flink.sql.parser.dql.SqlRichDescribeTable;
import org.apache.flink.sql.parser.dql.SqlShowCatalogs;
import org.apache.flink.sql.parser.dql.SqlShowDatabases;
import org.apache.flink.sql.parser.dql.SqlShowFunctions;
import org.apache.flink.sql.parser.dql.SqlShowTables;
import org.apache.flink.sql.parser.impl.FlinkSqlParserImpl;
import org.apache.flink.sql.parser.validate.FlinkSqlConformance;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDrop;
import org.apache.calcite.sql.SqlExplain;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSetOption;
import org.apache.calcite.sql.parser.SqlParser;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple parser for determining the type of command and its parameters.
 */
public final class SqlCommandParser {

	private SqlCommandParser() {
		// private
	}

	/**
	 * Parse the given statement and return corresponding SqlCommandCall.
	 *
	 * <p>only `set`, `show modules`, `show current catalog` and `show current database`
	 * are parsed through regex matching, other commands are parsed through sql parser.
	 *
	 * <p>throw {@link SqlParseException} if the statement contains multiple sub-statements separated by semicolon
	 * or there is a parse error.
	 *
	 * <p>NOTE: sql parser only parses the statement to get the corresponding SqlCommand,
	 * do not check whether the statement is valid here.
	 */
	public static Optional<SqlCommandCall> parse(String stmt, boolean isBlinkPlanner) {
		// normalize
		String stmtForRegexMatch = stmt.trim();
		// remove ';' at the end
		if (stmtForRegexMatch.endsWith(";")) {
			stmtForRegexMatch = stmtForRegexMatch.substring(0, stmtForRegexMatch.length() - 1).trim();
		}

		// only parse gateway specific statements
		for (SqlCommand cmd : SqlCommand.values()) {
			if (cmd.hasPattern()) {
				final Matcher matcher = cmd.pattern.matcher(stmtForRegexMatch);
				if (matcher.matches()) {
					final String[] groups = new String[matcher.groupCount()];
					for (int i = 0; i < groups.length; i++) {
						groups[i] = matcher.group(i + 1);
					}
					return cmd.operandConverter.apply(groups)
						.map((operands) -> new SqlCommandCall(cmd, operands));
				}
			}
		}

		return parseStmt(stmt, isBlinkPlanner);
	}

	/**
	 * Flink Parser only supports partial Operations, so we directly use Calcite Parser here.
	 * Once Flink Parser supports all Operations, we should use Flink Parser instead of Calcite Parser.
	 */
	private static Optional<SqlCommandCall> parseStmt(String stmt, boolean isBlinkPlanner) {
		SqlParser.Config config = createSqlParserConfig(isBlinkPlanner);
		SqlParser sqlParser = SqlParser.create(stmt, config);
		SqlNodeList sqlNodes;
		try {
			sqlNodes = sqlParser.parseStmtList();
			// no need check the statement is valid here
		} catch (org.apache.calcite.sql.parser.SqlParseException e) {
			throw new SqlParseException("Failed to parse statement.", e);
		}
		if (sqlNodes.size() != 1) {
			throw new SqlParseException("Only single statement is supported now");
		}

		final String[] operands;
		final SqlCommand cmd;
		SqlNode node = sqlNodes.get(0);
		if (node.getKind().belongsTo(SqlKind.QUERY)) {
			cmd = SqlCommand.SELECT;
			operands = new String[] { stmt };
		} else if (node instanceof RichSqlInsert) {
			RichSqlInsert insertNode = (RichSqlInsert) node;
			cmd = insertNode.isOverwrite() ? SqlCommand.INSERT_OVERWRITE : SqlCommand.INSERT_INTO;
			operands = new String[] { stmt, insertNode.getTargetTable().toString() };
		} else if (node instanceof SqlShowTables) {
			cmd = SqlCommand.SHOW_TABLES;
			operands = new String[0];
		} else if (node instanceof SqlCreateTable) {
			cmd = SqlCommand.CREATE_TABLE;
			operands = new String[] { stmt };
		} else if (node instanceof SqlDropTable) {
			cmd = SqlCommand.DROP_TABLE;
			operands = new String[] { stmt };
		} else if (node instanceof SqlAlterTable) {
			cmd = SqlCommand.ALTER_TABLE;
			operands = new String[] { stmt };
		} else if (node instanceof SqlCreateView) {
			// TableEnvironment currently does not support creating view
			// so we have to perform the modification here
			SqlCreateView createViewNode = (SqlCreateView) node;
			cmd = SqlCommand.CREATE_VIEW;
			operands = new String[] {
				createViewNode.getViewName().toString(),
				createViewNode.getQuery().toString()
			};
		} else if (node instanceof SqlDropView) {
			// TableEnvironment currently does not support dropping view
			// so we have to perform the modification here
			SqlDropView dropViewNode = (SqlDropView) node;

			// TODO: we can't get this field from SqlDropView normally until FLIP-71 is implemented
			Field ifExistsField;
			try {
				ifExistsField = SqlDrop.class.getDeclaredField("ifExists");
			} catch (NoSuchFieldException e) {
				throw new SqlParseException("Failed to parse drop view statement.", e);
			}
			ifExistsField.setAccessible(true);
			boolean ifExists;
			try {
				ifExists = ifExistsField.getBoolean(dropViewNode);
			} catch (IllegalAccessException e) {
				throw new SqlParseException("Failed to parse drop view statement.", e);
			}

			cmd = SqlCommand.DROP_VIEW;
			operands = new String[] { dropViewNode.getViewName().toString(), String.valueOf(ifExists) };
		} else if (node instanceof SqlShowDatabases) {
			cmd = SqlCommand.SHOW_DATABASES;
			operands = new String[0];
		} else if (node instanceof SqlCreateDatabase) {
			cmd = SqlCommand.CREATE_DATABASE;
			operands = new String[] { stmt };
		} else if (node instanceof SqlDropDatabase) {
			cmd = SqlCommand.DROP_DATABASE;
			operands = new String[] { stmt };
		} else if (node instanceof SqlAlterDatabase) {
			cmd = SqlCommand.ALTER_DATABASE;
			operands = new String[] { stmt };
		} else if (node instanceof SqlShowCatalogs) {
			cmd = SqlCommand.SHOW_CATALOGS;
			operands = new String[0];
		} else if (node instanceof SqlShowFunctions) {
			cmd = SqlCommand.SHOW_FUNCTIONS;
			operands = new String[0];
		} else if (node instanceof SqlUseCatalog) {
			cmd = SqlCommand.USE_CATALOG;
			operands = new String[] { ((SqlUseCatalog) node).getCatalogName() };
		} else if (node instanceof SqlUseDatabase) {
			cmd = SqlCommand.USE;
			operands = new String[] { ((SqlUseDatabase) node).getDatabaseName().toString() };
		} else if (node instanceof SqlRichDescribeTable) {
			cmd = SqlCommand.DESCRIBE_TABLE;
			// TODO support describe extended
			String[] fullTableName = ((SqlRichDescribeTable) node).fullTableName();
			String escapedName =
				Stream.of(fullTableName).map(s -> "`" + s + "`").collect(Collectors.joining("."));
			operands = new String[] { escapedName };
		} else if (node instanceof SqlExplain) {
			cmd = SqlCommand.EXPLAIN;
			// TODO support explain details
			operands = new String[] { ((SqlExplain) node).getExplicandum().toString() };
		} else if (node instanceof SqlSetOption) {
			SqlSetOption setNode = (SqlSetOption) node;
			// refer to SqlSetOption#unparseAlterOperation
			if (setNode.getValue() != null) {
				cmd = SqlCommand.SET;
				operands = new String[] { setNode.getName().toString(), setNode.getValue().toString() };
			} else {
				cmd = SqlCommand.RESET;
				if (setNode.getName().toString().toUpperCase().equals("ALL")) {
					operands = new String[0];
				} else {
					operands = new String[] { setNode.getName().toString() };
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
	private static SqlParser.Config createSqlParserConfig(boolean isBlinkPlanner) {
		if (isBlinkPlanner) {
			return SqlParser
				.configBuilder()
				.setParserFactory(FlinkSqlParserImpl.FACTORY)
				.setConformance(FlinkSqlConformance.DEFAULT)
				.setLex(Lex.JAVA)
				.setIdentifierMaxLength(256)
				.build();
		} else {
			return SqlParser
				.configBuilder()
				.setParserFactory(FlinkSqlParserImpl.FACTORY)
				.setConformance(FlinkSqlConformance.DEFAULT)
				.setLex(Lex.JAVA)
				.build();
		}
	}

	// --------------------------------------------------------------------------------------------

	private static final Function<String[], Optional<String[]>> NO_OPERANDS =
		(operands) -> Optional.of(new String[0]);

	private static final int DEFAULT_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;

	/**
	 * Supported SQL commands.
	 */
	public enum SqlCommand {
		SELECT,

		INSERT_INTO,

		INSERT_OVERWRITE,

		CREATE_TABLE,

		ALTER_TABLE,

		DROP_TABLE,

		CREATE_VIEW,

		DROP_VIEW,

		CREATE_DATABASE,

		ALTER_DATABASE,

		DROP_DATABASE,

		USE_CATALOG,

		USE,

		SHOW_CATALOGS,

		SHOW_DATABASES,

		SHOW_TABLES,

		SHOW_FUNCTIONS,

		EXPLAIN,

		DESCRIBE_TABLE,

		RESET,

		// the following commands are not supported by SQL parser but are needed by users

		SET(
			"SET",
			// `SET` with operands can be parsed by SQL parser
			// we keep `SET` with no operands here to print all properties
			NO_OPERANDS),

		// the following commands will be supported by SQL parser in the future
		// remove them once they're supported

		// FLINK-17396
		SHOW_MODULES(
			"SHOW\\s+MODULES",
			NO_OPERANDS),

		// FLINK-17111
		SHOW_VIEWS(
			"SHOW\\s+VIEWS",
			NO_OPERANDS),

		// the following commands are not supported by SQL parser but are needed by JDBC driver
		// these should not be exposed to the user and should be used internally

		SHOW_CURRENT_CATALOG(
			"SHOW\\s+CURRENT\\s+CATALOG",
			NO_OPERANDS),

		SHOW_CURRENT_DATABASE(
			"SHOW\\s+CURRENT\\s+DATABASE",
			NO_OPERANDS);

		public final Pattern pattern;
		public final Function<String[], Optional<String[]>> operandConverter;

		SqlCommand(String matchingRegex, Function<String[], Optional<String[]>> operandConverter) {
			this.pattern = Pattern.compile(matchingRegex, DEFAULT_PATTERN_FLAGS);
			this.operandConverter = operandConverter;
		}

		SqlCommand() {
			this.pattern = null;
			this.operandConverter = null;
		}

		@Override
		public String toString() {
			return super.toString().replace('_', ' ');
		}

		boolean hasPattern() {
			return pattern != null;
		}
	}

	/**
	 * Call of SQL command with operands and command type.
	 */
	public static class SqlCommandCall {
		public final SqlCommand command;
		public final String[] operands;

		public SqlCommandCall(SqlCommand command, String[] operands) {
			this.command = command;
			this.operands = operands;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			SqlCommandCall that = (SqlCommandCall) o;
			return command == that.command && Arrays.equals(operands, that.operands);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(command);
			result = 31 * result + Arrays.hashCode(operands);
			return result;
		}

		@Override
		public String toString() {
			return command + "(" + Arrays.toString(operands) + ")";
		}
	}
}
