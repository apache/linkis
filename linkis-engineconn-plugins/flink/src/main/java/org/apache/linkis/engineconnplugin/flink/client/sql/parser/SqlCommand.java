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

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public enum SqlCommand {
  LINKIS_GRAMMAR,

  SELECT,

  INSERT_INTO,

  INSERT_OVERWRITE,

  CREATE_TABLE,

  ALTER_TABLE,

  DROP_TABLE,

  CREATE_CATALOG,

  DROP_CATALOG,

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

  SET("SET", Inner_Config.NO_OPERANDS),

  SHOW_MODULES("SHOW\\s+MODULES", Inner_Config.NO_OPERANDS),

  SHOW_VIEWS("SHOW\\s+VIEWS", Inner_Config.NO_OPERANDS),

  SHOW_CURRENT_CATALOG("SHOW\\s+CURRENT\\s+CATALOG", Inner_Config.NO_OPERANDS),

  SHOW_CURRENT_DATABASE("SHOW\\s+CURRENT\\s+DATABASE", Inner_Config.NO_OPERANDS);

  private final Pattern pattern;
  private final Function<String[], Optional<String[]>> operandConverter;

  SqlCommand(String matchingRegex, Function<String[], Optional<String[]>> operandConverter) {
    this.pattern = Pattern.compile(matchingRegex, Inner_Config.DEFAULT_PATTERN_FLAGS);
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
    return pattern != null && operandConverter != null;
  }

  Pattern getPattern() {
    return pattern;
  }

  Function<String[], Optional<String[]>> getOperandConverter() {
    return operandConverter;
  }

  static class Inner_Config {
    private static final Function<String[], Optional<String[]>> NO_OPERANDS =
        (operands) -> Optional.of(new String[0]);

    private static final int DEFAULT_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;
  }
}
