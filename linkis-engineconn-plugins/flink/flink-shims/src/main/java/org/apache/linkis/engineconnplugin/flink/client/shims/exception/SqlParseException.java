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

package org.apache.linkis.engineconnplugin.flink.client.shims.exception;

import org.apache.linkis.common.exception.ErrorException;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.SQL_PARSE_ID;

public class SqlParseException extends ErrorException {

  private static final long serialVersionUID = 1L;

  public SqlParseException(String message) {
    super(SQL_PARSE_ID.getErrorCode(), message);
  }

  public SqlParseException(String message, Throwable e) {
    super(SQL_PARSE_ID.getErrorCode(), message);
    this.initCause(e);
  }
}
