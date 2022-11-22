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

    int dataworkcloudMustVersionErrorCode =
        LinkisModuleErrorCodeSummary.DATAWORKCLOUD_MUST_VERSION.getErrorCode();
    int fetchMapcacheErrorErrorCode =
        LinkisModuleErrorCodeSummary.FETCH_MAPCACHE_ERROR.getErrorCode();
    int notExistsApplicationErrorCode =
        LinkisModuleErrorCodeSummary.NOT_EXISTS_APPLICATION.getErrorCode();
    int haveNotSetErrorCode = LinkisModuleErrorCodeSummary.HAVE_NOT_SET.getErrorCode();
    int verificationCannotEmptyErrorCode =
        LinkisModuleErrorCodeSummary.VERIFICATION_CANNOT_EMPTY.getErrorCode();
    int loggedIdErrorCode = LinkisModuleErrorCodeSummary.LOGGED_ID.getErrorCode();
    int notLoggedErrorCode = LinkisModuleErrorCodeSummary.NOT_LOGGED.getErrorCode();
    int illegalIdErrorCode = LinkisModuleErrorCodeSummary.ILLEGAL_ID.getErrorCode();
    int illegalUserTokenErrorCode = LinkisModuleErrorCodeSummary.ILLEGAL_USER_TOKEN.getErrorCode();
    int serverssocketNotExistErrorCode =
        LinkisModuleErrorCodeSummary.SERVERSSOCKET_NOT_EXIST.getErrorCode();
    int websocketIsFullErrorCode = LinkisModuleErrorCodeSummary.WEBSOCKET_IS_FULL.getErrorCode();
    int websocketStoppedErrorCode = LinkisModuleErrorCodeSummary.WEBSOCKET_STOPPED.getErrorCode();

    Assertions.assertTrue(10010 == dataworkcloudMustVersionErrorCode);
    Assertions.assertTrue(10021 == fetchMapcacheErrorErrorCode);
    Assertions.assertTrue(10050 == notExistsApplicationErrorCode);
    Assertions.assertTrue(11000 == haveNotSetErrorCode);
    Assertions.assertTrue(11001 == verificationCannotEmptyErrorCode);
    Assertions.assertTrue(11002 == loggedIdErrorCode);
    Assertions.assertTrue(11002 == notLoggedErrorCode);
    Assertions.assertTrue(11003 == illegalIdErrorCode);
    Assertions.assertTrue(11003 == illegalUserTokenErrorCode);
    Assertions.assertTrue(11004 == serverssocketNotExistErrorCode);
    Assertions.assertTrue(11005 == websocketIsFullErrorCode);
    Assertions.assertTrue(11035 == websocketStoppedErrorCode);
  }
}
