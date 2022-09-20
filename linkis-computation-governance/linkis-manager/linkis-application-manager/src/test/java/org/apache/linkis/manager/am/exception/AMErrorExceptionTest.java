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

package org.apache.linkis.manager.am.exception;

import org.apache.linkis.manager.common.constant.AMConstant;

import org.junit.jupiter.api.*;

import static org.apache.linkis.manager.am.exception.AMErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** AMErrorException Tester */
public class AMErrorExceptionTest {

  @BeforeEach
  @DisplayName("Each unit test method is executed once before execution")
  public void before() throws Exception {}

  @AfterEach
  @DisplayName("Each unit test method is executed once before execution")
  public void after() throws Exception {}

  @Test
  public void testSetAMErrorCode() {
    QUERY_PARAM_NULL.setCode(0);
    assertEquals(0, QUERY_PARAM_NULL.getCode());
    UNSUPPORT_VALUE.setCode(0);
    assertEquals(0, UNSUPPORT_VALUE.getCode());
    PARAM_ERROR.setCode(0);
    assertEquals(0, PARAM_ERROR.getCode());
    NOT_EXISTS_ENGINE_CONN.setCode(0);
    assertEquals(0, NOT_EXISTS_ENGINE_CONN.getCode());
    AM_CONF_ERROR.setCode(0);
    assertEquals(0, AM_CONF_ERROR.getCode());
  }

  @Test
  public void testSetAMErrorMessage() {
    QUERY_PARAM_NULL.setMessage("test");
    assertEquals("test", QUERY_PARAM_NULL.getMessage());
    UNSUPPORT_VALUE.setMessage("test");
    assertEquals("test", UNSUPPORT_VALUE.getMessage());
    PARAM_ERROR.setMessage("test");
    assertEquals("test", PARAM_ERROR.getMessage());
    NOT_EXISTS_ENGINE_CONN.setMessage("test");
    assertEquals("test", NOT_EXISTS_ENGINE_CONN.getMessage());
    AM_CONF_ERROR.setMessage("test");
    assertEquals("test", AM_CONF_ERROR.getMessage());
  }

  @Test
  public void testGetAMErrorMessage() {
    assertEquals("query param cannot be null(请求参数不能为空)", QUERY_PARAM_NULL.getMessage());
    assertEquals("unsupport value(不支持的值类型)", UNSUPPORT_VALUE.getMessage());
    assertEquals("param error(参数错误)", PARAM_ERROR.getMessage());
    assertEquals("Not exists EngineConn(不存在的引擎)", NOT_EXISTS_ENGINE_CONN.getMessage());
    assertEquals("AM configuration error(AM配置错误)", AM_CONF_ERROR.getMessage());
  }

  @Test
  public void testGetAMErrorCode() {
    assertEquals(21001, QUERY_PARAM_NULL.getCode());
    assertEquals(21002, UNSUPPORT_VALUE.getCode());
    assertEquals(210003, PARAM_ERROR.getCode());
    assertEquals(210003, NOT_EXISTS_ENGINE_CONN.getCode());
    assertEquals(210003, AM_CONF_ERROR.getCode());
  }

  @Test
  public void testAMErrorException() {
    String msg = "Failed to execute ECM operation.";
    AMErrorException amErrorException = new AMErrorException(AMConstant.ENGINE_ERROR_CODE, msg);
    assertEquals(amErrorException.getErrCode(), AMConstant.ENGINE_ERROR_CODE);
    assertEquals(amErrorException.getDesc(), msg);
  }

  @Test
  public void testAMRetryException() {
    String msg = "testAMRetryException";
    AMRetryException amRetryException = new AMRetryException(0, msg);
    assertEquals(amRetryException.getErrCode(), 0);
    assertEquals(amRetryException.getDesc(), msg);
    AMRetryException amRetryException1 = new AMRetryException(0, msg, new RuntimeException());
    assertEquals(amRetryException1.getErrCode(), 0);
    assertEquals(amRetryException1.getDesc(), msg);
  }
}
