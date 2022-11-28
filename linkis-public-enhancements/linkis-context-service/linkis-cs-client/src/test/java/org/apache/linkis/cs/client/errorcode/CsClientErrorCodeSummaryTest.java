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

package org.apache.linkis.cs.client.errorcode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CsClientErrorCodeSummaryTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    String contextFailedErrorDesc = CsClientErrorCodeSummary.CREATE_CONTEXT_FAILED.getErrorDesc();
    String getContextValueFailedErrorDesc =
        CsClientErrorCodeSummary.GET_CONTEXT_VALUE_FAILED.getErrorDesc();
    String updateContextFailedErrorDesc =
        CsClientErrorCodeSummary.UPDATE_CONTEXT_FAILED.getErrorDesc();
    String resetContextFailedErrorDesc =
        CsClientErrorCodeSummary.RESET_CONTEXT_FAILED.getErrorDesc();
    String removeContextFailedErrorDesc =
        CsClientErrorCodeSummary.REMOVE_CONTEXT_FAILED.getErrorDesc();
    String bindContextidFailedErrorDesc =
        CsClientErrorCodeSummary.BIND_CONTEXTID_FAILED.getErrorDesc();
    String searchConditionFailedErrorDesc =
        CsClientErrorCodeSummary.SEARCH_CONDITION_FAILED.getErrorDesc();
    String executeFaliedErrorDesc = CsClientErrorCodeSummary.EXECUTE_FALIED.getErrorDesc();
    String haidbytimeFailedErrorDesc = CsClientErrorCodeSummary.HAIDBYTIME_FAILED.getErrorDesc();
    String clearContextHaidFailedErrorDesc =
        CsClientErrorCodeSummary.CLEAR_CONTEXT_HAID_FAILED.getErrorDesc();

    Assertions.assertNotNull(contextFailedErrorDesc);
    Assertions.assertNotNull(getContextValueFailedErrorDesc);
    Assertions.assertNotNull(updateContextFailedErrorDesc);
    Assertions.assertNotNull(resetContextFailedErrorDesc);
    Assertions.assertNotNull(removeContextFailedErrorDesc);
    Assertions.assertNotNull(bindContextidFailedErrorDesc);
    Assertions.assertNotNull(searchConditionFailedErrorDesc);
    Assertions.assertNotNull(executeFaliedErrorDesc);
    Assertions.assertNotNull(haidbytimeFailedErrorDesc);
    Assertions.assertNotNull(clearContextHaidFailedErrorDesc);
  }
}
