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

package org.apache.linkis.governance.common.exception.engineconn;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EngineConnExecutorErrorCodeTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    int initExecutorFailed = EngineConnExecutorErrorCode.INIT_EXECUTOR_FAILED;
    int invalidEngineType = EngineConnExecutorErrorCode.INVALID_ENGINE_TYPE;
    int invalidLock = EngineConnExecutorErrorCode.INVALID_LOCK;
    int invalidMethod = EngineConnExecutorErrorCode.INVALID_METHOD;
    int invalidParams = EngineConnExecutorErrorCode.INVALID_PARAMS;

    Assertions.assertTrue(40106 == initExecutorFailed);
    Assertions.assertTrue(40100 == invalidEngineType);
    Assertions.assertTrue(40103 == invalidLock);
    Assertions.assertTrue(40101 == invalidMethod);
    Assertions.assertTrue(40102 == invalidParams);
  }
}
