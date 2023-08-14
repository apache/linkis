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

import java.util.Optional;

public interface SqlCommandParser {

  /**
   * Parse the given statement and return corresponding SqlCommandCall.
   *
   * <p>only `set`, `show modules`, `show current catalog` and `show current database` are parsed
   * through regex matching, other commands are parsed through sql parser.
   *
   * <p>throw {@link SqlParseException} if the statement contains multiple sub-statements separated
   * by semicolon or there is a parse error.
   *
   * <p>NOTE: sql parser only parses the statement to get the corresponding SqlCommand, do not check
   * whether the statement is valid here.
   */
  Optional<SqlCommandCall> parse(String stmt, boolean isBlinkPlanner) throws SqlParseException;

  static SqlCommandParser getSqlCommandParser() {
    return SqlCommandParserImpl.getInstance();
  }
}
