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

package org.apache.linkis.gateway.springcloud.errorcode;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.gateway.springcloud.errorcode.GatewayErrorCodeSummary.WEBSOCKET_CONNECT_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayErrorCodeSummaryTest {

  @Test
  void testGetErrorCode() {
    assertEquals(13001, WEBSOCKET_CONNECT_ERROR.getErrorCode());
  }

  @Test
  void testSetErrorCode() {
    WEBSOCKET_CONNECT_ERROR.setErrorCode(1);
    assertEquals(1, WEBSOCKET_CONNECT_ERROR.getErrorCode());
    WEBSOCKET_CONNECT_ERROR.setErrorCode(13001);
    assertEquals(13001, WEBSOCKET_CONNECT_ERROR.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "Repeatedly creating a WebSocket connection(重复创建WebSocket连接)",
        WEBSOCKET_CONNECT_ERROR.getErrorDesc());
  }

  @Test
  void testSetErrorDesc() {
    WEBSOCKET_CONNECT_ERROR.setErrorDesc("test");
    assertEquals("test", WEBSOCKET_CONNECT_ERROR.getErrorDesc());
    WEBSOCKET_CONNECT_ERROR.setErrorDesc(
        "Repeatedly creating a WebSocket connection(重复创建WebSocket连接)");
    assertEquals(
        "Repeatedly creating a WebSocket connection(重复创建WebSocket连接)",
        WEBSOCKET_CONNECT_ERROR.getErrorDesc());
  }

  @Test
  void testGetComment() {
    assertEquals(
        "The service instance has created a WebSocket connection before, and cannot be created repeatedly(服务实例之前已经创建过WebSocket连接, 不能重复创建)",
        WEBSOCKET_CONNECT_ERROR.getComment());
  }

  @Test
  void testSetComment() {
    WEBSOCKET_CONNECT_ERROR.setComment("test");
    assertEquals("test", WEBSOCKET_CONNECT_ERROR.getComment());
    WEBSOCKET_CONNECT_ERROR.setComment(
        "The service instance has created a WebSocket connection before, and cannot be created repeatedly(服务实例之前已经创建过WebSocket连接, 不能重复创建)");
    assertEquals(
        "The service instance has created a WebSocket connection before, and cannot be created repeatedly(服务实例之前已经创建过WebSocket连接, 不能重复创建)",
        WEBSOCKET_CONNECT_ERROR.getComment());
  }

  @Test
  void testGetModule() {
    assertEquals("Websocket", WEBSOCKET_CONNECT_ERROR.getModule());
  }

  @Test
  void testSetModule() {
    WEBSOCKET_CONNECT_ERROR.setModule("test");
    assertEquals("test", WEBSOCKET_CONNECT_ERROR.getModule());
    WEBSOCKET_CONNECT_ERROR.setModule("Websocket");
    assertEquals("Websocket", WEBSOCKET_CONNECT_ERROR.getModule());
  }
}
