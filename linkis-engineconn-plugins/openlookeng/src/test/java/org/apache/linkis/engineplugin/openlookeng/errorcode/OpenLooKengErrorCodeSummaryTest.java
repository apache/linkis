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

package org.apache.linkis.engineplugin.openlookeng.errorcode;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.engineplugin.openlookeng.errorcode.OpenLooKengErrorCodeSummary.OPENLOOKENG_CLIENT_ERROR;
import static org.apache.linkis.engineplugin.openlookeng.errorcode.OpenLooKengErrorCodeSummary.OPENLOOKENG_STATUS_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenLooKengErrorCodeSummaryTest {
  @Test
  void testGetErrorCode() {
    assertEquals(26030, OPENLOOKENG_CLIENT_ERROR.getErrorCode());
    assertEquals(26031, OPENLOOKENG_STATUS_ERROR.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "openlookeng client error(openlookeng客户端异常)", OPENLOOKENG_CLIENT_ERROR.getErrorDesc());
    assertEquals(
        "openlookeng status error,statement is not finished(openlookeng状态异常, 查询语句未完成)",
        OPENLOOKENG_STATUS_ERROR.getErrorDesc());
  }
}
