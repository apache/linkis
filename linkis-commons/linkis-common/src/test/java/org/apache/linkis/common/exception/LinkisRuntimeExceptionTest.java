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

package org.apache.linkis.common.exception;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkisRuntimeExceptionTest {

  public static LinkisRuntimeException linkisRuntimeException =
      new LinkisRuntimeException(1, "test", "127.0.0.1", 1, "serviceKind") {
        @Override
        public ExceptionLevel getLevel() {
          return ExceptionLevel.WARN;
        }
      };

  @Test
  void testGetErrCode() {
    assertEquals(1, linkisRuntimeException.getErrCode());
  }

  @Test
  void testSetErrCode() {
    linkisRuntimeException.setErrCode(123);
    assertEquals(123, linkisRuntimeException.getErrCode());
    linkisRuntimeException.setErrCode(1);
    assertEquals(1, linkisRuntimeException.getErrCode());
  }

  @Test
  void testGetDesc() {
    assertEquals("test", linkisRuntimeException.getDesc());
  }

  @Test
  void testSetDesc() {
    linkisRuntimeException.setDesc("test2");
    assertEquals("test2", linkisRuntimeException.getDesc());
    linkisRuntimeException.setDesc("test");
    assertEquals("test", linkisRuntimeException.getDesc());
  }

  @Test
  void testGetIp() {
    assertEquals("127.0.0.1", linkisRuntimeException.getIp());
  }

  @Test
  void testSetIp() {
    linkisRuntimeException.setIp("0.0.0.0");
    assertEquals("0.0.0.0", linkisRuntimeException.getIp());
    linkisRuntimeException.setIp("127.0.0.1");
    assertEquals("127.0.0.1", linkisRuntimeException.getIp());
  }

  @Test
  void testGetPort() {
    assertEquals(1, linkisRuntimeException.getPort());
  }

  @Test
  void testSetPort() {
    linkisRuntimeException.setPort(11);
    assertEquals(11, linkisRuntimeException.getPort());
    linkisRuntimeException.setPort(1);
    assertEquals(1, linkisRuntimeException.getPort());
  }

  @Test
  void testGetServiceKind() {
    assertEquals("test", linkisRuntimeException.getServiceKind());
  }

  @Test
  void testSetServiceKind() {
    linkisRuntimeException.setServiceKind("test2");
    assertEquals("test2", linkisRuntimeException.getServiceKind());
    linkisRuntimeException.setServiceKind("test");
    assertEquals("test", linkisRuntimeException.getServiceKind());
  }

  @Test
  void toMap() {
    Map<String, Object> map = new java.util.HashMap<String, Object>();
    map.put("level", ExceptionLevel.WARN.getLevel());
    map.put("errCode", 1);
    map.put("desc", "test");
    map.put("ip", "127.0.0.1");
    map.put("port", 1);
    map.put("serviceKind", "test");
    assertEquals(map, linkisRuntimeException.toMap());
  }

  @Test
  void testGetLevel() {
    assertEquals(ExceptionLevel.WARN, linkisRuntimeException.getLevel());
  }

  @Test
  void testToString() {
    assertEquals(
        "LinkisException{errCode=1, desc='test', ip='127.0.0.1', port=1, serviceKind='test'}",
        linkisRuntimeException.toString());
  }
}
