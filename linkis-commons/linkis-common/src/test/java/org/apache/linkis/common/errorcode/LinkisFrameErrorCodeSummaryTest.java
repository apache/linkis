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

package org.apache.linkis.common.errorcode;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.common.errorcode.LinkisFrameErrorCodeSummary.VALIDATE_ERROR_CODE_FAILED;
import static org.junit.jupiter.api.Assertions.*;

/** LinkisFrameErrorCodeSummary Tester */
class LinkisFrameErrorCodeSummaryTest {

  @Test
  void testGetErrorCode() {
    assertEquals(10000, VALIDATE_ERROR_CODE_FAILED.getErrorCode());
  }

  @Test
  void testSetErrorCode() {
    VALIDATE_ERROR_CODE_FAILED.setErrorCode(-1);
    assertEquals(-1, VALIDATE_ERROR_CODE_FAILED.getErrorCode());
    VALIDATE_ERROR_CODE_FAILED.setErrorCode(10000);
    assertEquals(10000, VALIDATE_ERROR_CODE_FAILED.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "Error code definition is incorrect(错误码定义有误)", VALIDATE_ERROR_CODE_FAILED.getErrorDesc());
  }

  @Test
  void testSetErrorDesc() {
    VALIDATE_ERROR_CODE_FAILED.setErrorDesc("test");
    assertEquals("test", VALIDATE_ERROR_CODE_FAILED.getErrorDesc());
    VALIDATE_ERROR_CODE_FAILED.setErrorDesc("Error code definition is incorrect(错误码定义有误)");
    assertEquals(
        "Error code definition is incorrect(错误码定义有误)", VALIDATE_ERROR_CODE_FAILED.getErrorDesc());
  }

  @Test
  void testGetComment() {
    assertEquals(
        "Error code definition exceeds the maximum value or is less than the minimum value(错误码定义超过最大值或者小于最小值)",
        VALIDATE_ERROR_CODE_FAILED.getComment());
  }

  @Test
  void testSetComment() {
    VALIDATE_ERROR_CODE_FAILED.setComment("test");
    assertEquals("test", VALIDATE_ERROR_CODE_FAILED.getComment());
    VALIDATE_ERROR_CODE_FAILED.setComment(
        "Error code definition exceeds the maximum value or is less than the minimum value(错误码定义超过最大值或者小于最小值)");
    assertEquals(
        "Error code definition exceeds the maximum value or is less than the minimum value(错误码定义超过最大值或者小于最小值)",
        VALIDATE_ERROR_CODE_FAILED.getComment());
  }

  @Test
  void testGetModule() {
    assertEquals("linkis-frame", VALIDATE_ERROR_CODE_FAILED.getModule());
  }

  @Test
  void testSetModule() {
    VALIDATE_ERROR_CODE_FAILED.setModule("test");
    assertEquals("test", VALIDATE_ERROR_CODE_FAILED.getModule());
    VALIDATE_ERROR_CODE_FAILED.setModule("linkis-frame");
    assertEquals("linkis-frame", VALIDATE_ERROR_CODE_FAILED.getModule());
  }

  @Test
  void testToString() {
    assertEquals(
        "errorCode: 10000, errorDesc:Error code definition is incorrect(错误码定义有误)",
        VALIDATE_ERROR_CODE_FAILED.toString());
  }
}
