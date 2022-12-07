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

import static org.apache.linkis.common.errorcode.LinkisErrorCodeSummary.EngineManagerErrorException;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkisErrorCodeSummaryTest {

  @Test
  void testGetErrorCode() {
    assertEquals(321, EngineManagerErrorException.getErrorCode());
  }

  @Test
  void testSetErrorCode() {
    EngineManagerErrorException.setErrorCode(1);
    assertEquals(1, EngineManagerErrorException.getErrorCode());
    EngineManagerErrorException.setErrorCode(321);
    assertEquals(321, EngineManagerErrorException.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals("Engine start failed(引擎启动失败)", EngineManagerErrorException.getErrorDesc());
  }

  @Test
  void testSetErrorDesc() {
    EngineManagerErrorException.setErrorDesc("test");
    assertEquals("test", EngineManagerErrorException.getErrorDesc());
    EngineManagerErrorException.setErrorDesc("Engine start failed(引擎启动失败)");
    assertEquals("Engine start failed(引擎启动失败)", EngineManagerErrorException.getErrorDesc());
  }

  @Test
  void testGetComment() {
    assertEquals(
        "Failed to start under certain circumstances(在某种情况下启动失败)",
        EngineManagerErrorException.getComment());
  }

  @Test
  void testSetComment() {
    EngineManagerErrorException.setComment("test");
    assertEquals("test", EngineManagerErrorException.getComment());
    EngineManagerErrorException.setComment(
        "Failed to start under certain circumstances(在某种情况下启动失败)");
    assertEquals(
        "Failed to start under certain circumstances(在某种情况下启动失败)",
        EngineManagerErrorException.getComment());
  }

  @Test
  void testGetCreator() {
    assertEquals("hadoop", EngineManagerErrorException.getCreator());
  }

  @Test
  void testSetCreator() {
    EngineManagerErrorException.setCreator("test");
    assertEquals("test", EngineManagerErrorException.getCreator());
    EngineManagerErrorException.setCreator("hadoop");
    assertEquals("hadoop", EngineManagerErrorException.getCreator());
  }

  @Test
  void testGetModule() {
    assertEquals("EngineConnManager", EngineManagerErrorException.getModule());
  }

  @Test
  void testSetModule() {
    EngineManagerErrorException.setModule("test");
    assertEquals("test", EngineManagerErrorException.getModule());
    EngineManagerErrorException.setModule("EngineConnManager");
    assertEquals("EngineConnManager", EngineManagerErrorException.getModule());
  }
}
