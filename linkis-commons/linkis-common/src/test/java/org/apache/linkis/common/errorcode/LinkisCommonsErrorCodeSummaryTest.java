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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** LinkisCommonsErrorCodeSummary Tester */
class LinkisCommonsErrorCodeSummaryTest {

  @ParameterizedTest
  @EnumSource(LinkisCommonsErrorCodeSummary.class)
  void testGetErrorCode(LinkisCommonsErrorCodeSummary linkisCommonsErrorCodeSummary) {
    assertEquals(11000, linkisCommonsErrorCodeSummary.getErrorCode());
  }

  @ParameterizedTest
  @EnumSource(LinkisCommonsErrorCodeSummary.class)
  void testSetErrorCode(LinkisCommonsErrorCodeSummary linkisCommonsErrorCodeSummary) {
    assertEquals(11000, linkisCommonsErrorCodeSummary.getErrorCode());
    linkisCommonsErrorCodeSummary.setErrorCode(1);
    assertEquals(1, linkisCommonsErrorCodeSummary.getErrorCode());
    linkisCommonsErrorCodeSummary.setErrorCode(2147483647);
    assertEquals(2147483647, linkisCommonsErrorCodeSummary.getErrorCode());
    linkisCommonsErrorCodeSummary.setErrorCode(11000);
  }

  @ParameterizedTest
  @EnumSource(LinkisCommonsErrorCodeSummary.class)
  void testGetErrorDesc(LinkisCommonsErrorCodeSummary linkisCommonsErrorCodeSummary) {
    assertEquals("Engine start failed(引擎启动失败)", linkisCommonsErrorCodeSummary.getErrorDesc());
  }

  @ParameterizedTest
  @EnumSource(LinkisCommonsErrorCodeSummary.class)
  void testSetErrorDesc(LinkisCommonsErrorCodeSummary linkisCommonsErrorCodeSummary) {
    assertEquals("Engine start failed(引擎启动失败)", linkisCommonsErrorCodeSummary.getErrorDesc());
    linkisCommonsErrorCodeSummary.setErrorDesc("testSetErrorDesc");
    assertEquals("testSetErrorDesc", linkisCommonsErrorCodeSummary.getErrorDesc());
    linkisCommonsErrorCodeSummary.setErrorDesc("Engine start failed(引擎启动失败)");
  }

  @ParameterizedTest
  @EnumSource(LinkisCommonsErrorCodeSummary.class)
  void testToString(LinkisCommonsErrorCodeSummary linkisCommonsErrorCodeSummary) {
    linkisCommonsErrorCodeSummary.setErrorCode(0);
    linkisCommonsErrorCodeSummary.setErrorDesc("testSetErrorDesc");
    assertEquals(
        "errorCode: 0, errorDesc:testSetErrorDesc", linkisCommonsErrorCodeSummary.toString());
    linkisCommonsErrorCodeSummary.setErrorCode(11000);
    linkisCommonsErrorCodeSummary.setErrorDesc("Engine start failed(引擎启动失败)");
    assertEquals(
        "errorCode: 11000, errorDesc:Engine start failed(引擎启动失败)",
        linkisCommonsErrorCodeSummary.toString());
  }
}
