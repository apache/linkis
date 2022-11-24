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

package org.apache.linkis.rpc.errorcode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkisRpcErrorCodeSummaryTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    int methodCallFailedCode = LinkisRpcErrorCodeSummary.METHON_CALL_FAILED.getErrorCode();
    int transmittedBeanIsNull = LinkisRpcErrorCodeSummary.TRANSMITTED_BEAN_IS_NULL.getErrorCode();
    int timeoutPeriodErrorCode = LinkisRpcErrorCodeSummary.TIMEOUT_PERIOD.getErrorCode();
    int correspondingNotFoundErrorCode =
        LinkisRpcErrorCodeSummary.CORRESPONDING_NOT_FOUND.getErrorCode();
    int correspondingToInitializeErrorCode =
        LinkisRpcErrorCodeSummary.CORRESPONDING_TO_INITIALIZE.getErrorCode();
    int applicationIsNotExistsErrorCode =
        LinkisRpcErrorCodeSummary.APPLICATION_IS_NOT_EXISTS.getErrorCode();

    Assertions.assertTrue(10000 == methodCallFailedCode);
    Assertions.assertTrue(10001 == transmittedBeanIsNull);
    Assertions.assertTrue(10002 == timeoutPeriodErrorCode);
    Assertions.assertTrue(10003 == correspondingNotFoundErrorCode);
    Assertions.assertTrue(10004 == correspondingToInitializeErrorCode);
    Assertions.assertTrue(10051 == applicationIsNotExistsErrorCode);
  }
}
