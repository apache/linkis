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

    int unsupportedFileErrorCode = LinkisStorageErrorCodeSummary.UNSUPPORTED_FILE.getErrorCode();
    int unsupportedResultErrorCode =
        LinkisStorageErrorCodeSummary.UNSUPPORTED_RESULT.getErrorCode();
    int configurationNotReadErrorCode =
        LinkisStorageErrorCodeSummary.CONFIGURATION_NOT_READ.getErrorCode();
    int failedToReadIntegerErrorCode =
        LinkisStorageErrorCodeSummary.FAILED_TO_READ_INTEGER.getErrorCode();
    int theFileIsEmptyErrorCode = LinkisStorageErrorCodeSummary.THE_FILE_IS_EMPTY.getErrorCode();
    int toBeUnknowErrorCode = LinkisStorageErrorCodeSummary.TO_BE_UNKNOW.getErrorCode();
    int fsnNotInitExceptionErrorCode =
        LinkisStorageErrorCodeSummary.FSN_NOT_INIT_EXCEPTION.getErrorCode();
    int parsingMetadataFailedErrorCode =
        LinkisStorageErrorCodeSummary.PARSING_METADATA_FAILED.getErrorCode();
    int tableAreNotSupportedErrorCode =
        LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorCode();
    int mustRegisterTocErrorCode = LinkisStorageErrorCodeSummary.MUST_REGISTER_TOC.getErrorCode();
    int mustRegisterTomErrorCode = LinkisStorageErrorCodeSummary.MUST_REGISTER_TOM.getErrorCode();
    int unsupportedOpenFileTypeErrorCode =
        LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode();
    int incalidCustomParameterErrorCode =
        LinkisStorageErrorCodeSummary.INCALID_CUSTOM_PARAMETER.getErrorCode();

    Assertions.assertTrue(50000 == unsupportedFileErrorCode);
    Assertions.assertTrue(50000 == unsupportedResultErrorCode);
    Assertions.assertTrue(50001 == configurationNotReadErrorCode);
    Assertions.assertTrue(51000 == failedToReadIntegerErrorCode);
    Assertions.assertTrue(51000 == theFileIsEmptyErrorCode);
    Assertions.assertTrue(51001 == toBeUnknowErrorCode);
    Assertions.assertTrue(52000 == fsnNotInitExceptionErrorCode);
    Assertions.assertTrue(52001 == parsingMetadataFailedErrorCode);
    Assertions.assertTrue(52002 == tableAreNotSupportedErrorCode);
    Assertions.assertTrue(52004 == mustRegisterTocErrorCode);
    Assertions.assertTrue(52004 == mustRegisterTomErrorCode);
    Assertions.assertTrue(54001 == unsupportedOpenFileTypeErrorCode);
    Assertions.assertTrue(65000 == incalidCustomParameterErrorCode);
  }
}
