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

public class LinkisStorageErrorCodeSummaryTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {
    Assertions.assertEquals(50000, LinkisStorageErrorCodeSummary.UNSUPPORTED_FILE.getErrorCode());
    Assertions.assertEquals(50000, LinkisStorageErrorCodeSummary.UNSUPPORTED_RESULT.getErrorCode());
    Assertions.assertEquals(
        50001, LinkisStorageErrorCodeSummary.CONFIGURATION_NOT_READ.getErrorCode());
    Assertions.assertEquals(
        51000, LinkisStorageErrorCodeSummary.FAILED_TO_READ_INTEGER.getErrorCode());
    Assertions.assertEquals(51000, LinkisStorageErrorCodeSummary.THE_FILE_IS_EMPTY.getErrorCode());
    Assertions.assertEquals(51001, LinkisStorageErrorCodeSummary.TO_BE_UNKNOW.getErrorCode());
    Assertions.assertEquals(
        52000, LinkisStorageErrorCodeSummary.FSN_NOT_INIT_EXCEPTION.getErrorCode());
    Assertions.assertEquals(
        52001, LinkisStorageErrorCodeSummary.PARSING_METADATA_FAILED.getErrorCode());
    Assertions.assertEquals(
        52002, LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorCode());
    Assertions.assertEquals(52004, LinkisStorageErrorCodeSummary.MUST_REGISTER_TOC.getErrorCode());
    Assertions.assertEquals(52004, LinkisStorageErrorCodeSummary.MUST_REGISTER_TOM.getErrorCode());
    Assertions.assertEquals(
        54001, LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode());
    Assertions.assertEquals(
        65000, LinkisStorageErrorCodeSummary.INVALID_CUSTOM_PARAMETER.getErrorCode());
  }
}
