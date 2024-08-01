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

package org.apache.linkis.errorcode.client.handler;

import org.apache.linkis.errorcode.common.ErrorCode;

import java.util.List;

public interface LogFileErrorCodeHandler extends ErrorCodeHandler {
  /**
   * Pass in a log path, and then read the file according to the log path and match the error code
   * at the same time, and then match the error code
   *
   * @param logFilePath Log file path
   * @param type Auxiliary parameters
   * @return
   */
  void handle(String logFilePath, int type);

  void handle(List<String> logFilePaths);

  /**
   * @param logFilePath File path
   * @param line Number of rows, if not passed, the default value is 1000
   * @return Error code information
   */
  List<ErrorCode> handleFileLines(String logFilePath, int line);
}
