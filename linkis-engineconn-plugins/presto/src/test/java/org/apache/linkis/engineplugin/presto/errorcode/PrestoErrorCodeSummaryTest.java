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

package org.apache.linkis.engineplugin.presto.errorcode;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.engineplugin.presto.errorcode.PrestoErrorCodeSummary.PRESTO_CLIENT_ERROR;
import static org.apache.linkis.engineplugin.presto.errorcode.PrestoErrorCodeSummary.PRESTO_STATE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrestoErrorCodeSummaryTest {
  @Test
  void testGetErrorCode() {
    assertEquals(26001, PRESTO_STATE_INVALID.getErrorCode());
    assertEquals(26002, PRESTO_CLIENT_ERROR.getErrorCode());
  }

  @Test
  void testSetErrorCode() {
    PRESTO_STATE_INVALID.setErrorCode(1);
    assertEquals(1, PRESTO_STATE_INVALID.getErrorCode());
    PRESTO_STATE_INVALID.setErrorCode(26001);
    assertEquals(26001, PRESTO_STATE_INVALID.getErrorCode());

    PRESTO_CLIENT_ERROR.setErrorCode(1);
    assertEquals(1, PRESTO_CLIENT_ERROR.getErrorCode());
    PRESTO_CLIENT_ERROR.setErrorCode(26002);
    assertEquals(26002, PRESTO_CLIENT_ERROR.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "Presto status error,statement is not finished(Presto服务状态异常, 查询语句没有执行结束)",
        PRESTO_STATE_INVALID.getErrorDesc());
    assertEquals("Presto client error(Presto客户端异常)", PRESTO_CLIENT_ERROR.getErrorDesc());
  }

  @Test
  void testSetErrorDesc() {
    PRESTO_STATE_INVALID.setErrorDesc("test");
    assertEquals("test", PRESTO_STATE_INVALID.getErrorDesc());
    PRESTO_STATE_INVALID.setErrorDesc(
        "Presto status error,statement is not finished(Presto服务状态异常, 查询语句没有执行结束)");
    assertEquals(
        "Presto status error,statement is not finished(Presto服务状态异常, 查询语句没有执行结束)",
        PRESTO_STATE_INVALID.getErrorDesc());

    PRESTO_CLIENT_ERROR.setErrorDesc("test");
    assertEquals("test", PRESTO_CLIENT_ERROR.getErrorDesc());
    PRESTO_CLIENT_ERROR.setErrorDesc("Presto client error(Presto客户端异常)");
    assertEquals("Presto client error(Presto客户端异常)", PRESTO_CLIENT_ERROR.getErrorDesc());
  }
}
