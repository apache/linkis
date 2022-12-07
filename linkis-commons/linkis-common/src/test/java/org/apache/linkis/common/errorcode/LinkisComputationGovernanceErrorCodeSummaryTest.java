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

import static org.apache.linkis.common.errorcode.LinkisComputationGovernanceErrorCodeSummary.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** LinkisComputationGovernanceErrorCodeSummary Tester */
class LinkisComputationGovernanceErrorCodeSummaryTest {

  @Test
  void testGetErrorCode() {
    assertEquals(20000, ENGINE_LAUNCH_REQUEST_USER_BLANK.getErrorCode());
    assertEquals(20001, ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.getErrorCode());
    assertEquals(20002, ENGINE_INIT_FAILED.getErrorCode());
    assertEquals(20000, ENGINE_REQUEST_USER_BLANK.getErrorCode());
    assertEquals(20100, AM_EM_NOT_FOUND.getErrorCode());
  }

  @Test
  void testSetErrorCode() {
    ENGINE_LAUNCH_REQUEST_USER_BLANK.setErrorCode(1);
    assertEquals(1, ENGINE_LAUNCH_REQUEST_USER_BLANK.getErrorCode());
    ENGINE_LAUNCH_REQUEST_USER_BLANK.setErrorCode(20000);
    assertEquals(20000, ENGINE_LAUNCH_REQUEST_USER_BLANK.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "User is null in the parameters of the request engine(请求引擎的参数中user为空)",
        ENGINE_LAUNCH_REQUEST_USER_BLANK.getErrorDesc());
    assertEquals(
        "The creator in the parameters of the request to start the engine is null(请求启动引擎的参数中creator为空)",
        ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.getErrorDesc());
    assertEquals("Engine initialization failed(引擎初始化失败)", ENGINE_INIT_FAILED.getErrorDesc());
    assertEquals(
        "User is null in the parameters of the request engine(请求引擎的参数中user为空)",
        ENGINE_REQUEST_USER_BLANK.getErrorDesc());
    assertEquals(
        "User is null in the parameters of the request engine(请求引擎的参数中user为空)",
        AM_EM_NOT_FOUND.getErrorDesc());
  }

  @Test
  void testSetErrorDesc() {
    ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.setErrorDesc("Test SetErrorDesc");
    assertEquals("Test SetErrorDesc", ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.getErrorDesc());
    ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.setErrorDesc(
        "The creator in the parameters of the request to start the engine is null(请求启动引擎的参数中creator为空)");
    assertEquals(
        "The creator in the parameters of the request to start the engine is null(请求启动引擎的参数中creator为空)",
        ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.getErrorDesc());
  }

  @Test
  void testGetComment() {
    assertEquals(
        "User is null in the parameters of the request engine(请求引擎的参数中user为空)",
        ENGINE_LAUNCH_REQUEST_USER_BLANK.getComment());
    assertEquals(
        "The creator in the parameters of the request to start the engine is null(请求启动引擎的参数中creator为空)",
        ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.getComment());
    assertEquals("Engine initialization failed(引擎初始化失败)", ENGINE_INIT_FAILED.getComment());
    assertEquals(
        "User is null in the parameters of the request engine(请求引擎的参数中user为空)",
        ENGINE_REQUEST_USER_BLANK.getComment());
    assertEquals(
        "User is null in the parameters of the request engine(请求引擎的参数中user为空)",
        AM_EM_NOT_FOUND.getComment());
  }

  @Test
  void testSetComment() {
    ENGINE_INIT_FAILED.setComment("Test SetComment");
    assertEquals("Test SetComment", ENGINE_INIT_FAILED.getComment());
    ENGINE_INIT_FAILED.setComment("Engine initialization failed(引擎初始化失败)");
    assertEquals("Engine initialization failed(引擎初始化失败)", ENGINE_INIT_FAILED.getComment());
  }

  @Test
  void testGetModule() {
    assertEquals("EngineConnManager", ENGINE_LAUNCH_REQUEST_USER_BLANK.getModule());
    assertEquals("EngineConnManager", ENGINE_LAUNCH_REQUEST_CREATOR_BLANK.getModule());
    assertEquals("EngineConnManager", ENGINE_INIT_FAILED.getModule());
    assertEquals("EngineConnManager", ENGINE_REQUEST_USER_BLANK.getModule());
    assertEquals("EngineConnManager", AM_EM_NOT_FOUND.getModule());
  }

  @Test
  void testSetModule() {
    ENGINE_REQUEST_USER_BLANK.setModule("Test SetModule");
    assertEquals("Test SetModule", ENGINE_REQUEST_USER_BLANK.getModule());
    ENGINE_REQUEST_USER_BLANK.setModule("EngineConnManager");
    assertEquals("EngineConnManager", ENGINE_REQUEST_USER_BLANK.getModule());
  }

  @Test
  void testToString() {
    assertEquals(
        "errorCode: 20100, errorDesc:User is null in the parameters of the request engine(请求引擎的参数中user为空)",
        AM_EM_NOT_FOUND.toString());
  }
}
