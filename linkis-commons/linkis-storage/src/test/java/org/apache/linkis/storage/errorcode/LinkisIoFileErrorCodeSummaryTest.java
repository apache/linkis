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

package org.apache.linkis.storage.errorcode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkisIoFileErrorCodeSummaryTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    int cannotBeEmptyErrorCode = LinkisIoFileErrorCodeSummary.CANNOT_BE_EMPTY.getErrorCode();
    int fsCanNotProxyToErrorCode = LinkisIoFileErrorCodeSummary.FS_CAN_NOT_PROXY_TO.getErrorCode();
    int notExistsMethodErrorCode = LinkisIoFileErrorCodeSummary.NOT_EXISTS_METHOD.getErrorCode();
    int parameterCallsErrorCode = LinkisIoFileErrorCodeSummary.PARAMETER_CALLS.getErrorCode();

    Assertions.assertTrue(53002 == cannotBeEmptyErrorCode);
    Assertions.assertTrue(52002 == fsCanNotProxyToErrorCode);
    Assertions.assertTrue(53003 == notExistsMethodErrorCode);
    Assertions.assertTrue(53003 == parameterCallsErrorCode);
  }
}
