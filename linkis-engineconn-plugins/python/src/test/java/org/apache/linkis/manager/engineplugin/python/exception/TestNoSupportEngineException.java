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

package org.apache.linkis.manager.engineplugin.python.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestNoSupportEngineException {

  @Test
  public void testPythonSessionStartFailedException() {
    String errorMsg = "PythonSessionStartFailed";
    PythonSessionStartFailedExeception exception = new PythonSessionStartFailedExeception(errorMsg);
    Assertions.assertEquals(errorMsg, exception.getDesc());
  }

  @Test
  public void testPythonExecuteError() {
    String errorMsg = "PythonExecuteError";
    PythonExecuteError exception = new PythonExecuteError(60006, errorMsg);
    Assertions.assertEquals(60006, exception.getErrCode());
    Assertions.assertEquals(errorMsg, exception.getDesc());
  }

  @Test
  public void testExecuteException() {
    String errorMsg = "ExecuteException";
    ExecuteException exception = new ExecuteException(60003, errorMsg);
    Assertions.assertEquals(60003, exception.getErrCode());
    Assertions.assertEquals(errorMsg, exception.getDesc());
  }
}
