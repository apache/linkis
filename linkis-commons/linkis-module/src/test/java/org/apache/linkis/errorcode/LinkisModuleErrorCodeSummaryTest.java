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

package org.apache.linkis.errorcode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkisModuleErrorCodeSummaryTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {
    Assertions.assertEquals(
        10010, LinkisModuleErrorCodeSummary.DATAWORKCLOUD_MUST_VERSION.getErrorCode());
    Assertions.assertEquals(
        10021, LinkisModuleErrorCodeSummary.FETCH_MAPCACHE_ERROR.getErrorCode());
    Assertions.assertEquals(
        10050, LinkisModuleErrorCodeSummary.NOT_EXISTS_APPLICATION.getErrorCode());
    Assertions.assertEquals(11000, LinkisModuleErrorCodeSummary.HAVE_NOT_SET.getErrorCode());
    Assertions.assertEquals(
        11001, LinkisModuleErrorCodeSummary.VERIFICATION_CANNOT_EMPTY.getErrorCode());
    Assertions.assertEquals(11002, LinkisModuleErrorCodeSummary.LOGGED_ID.getErrorCode());
    Assertions.assertEquals(11002, LinkisModuleErrorCodeSummary.NOT_LOGGED.getErrorCode());
    Assertions.assertEquals(11003, LinkisModuleErrorCodeSummary.ILLEGAL_ID.getErrorCode());
    Assertions.assertEquals(11003, LinkisModuleErrorCodeSummary.ILLEGAL_USER_TOKEN.getErrorCode());
    Assertions.assertEquals(
        11004, LinkisModuleErrorCodeSummary.SERVERSOCKET_NOT_EXIST.getErrorCode());
    Assertions.assertEquals(11005, LinkisModuleErrorCodeSummary.WEBSOCKET_IS_FULL.getErrorCode());
    Assertions.assertEquals(11035, LinkisModuleErrorCodeSummary.WEBSOCKET_STOPPED.getErrorCode());
  }
}
