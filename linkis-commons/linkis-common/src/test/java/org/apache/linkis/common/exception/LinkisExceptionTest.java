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

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkisExceptionTest {

  public static LinkisException linkisException =
      new LinkisException(1, "test", "127.0.0.1", 1, "test") {
        @Override
        ExceptionLevel getLevel() {
          return ExceptionLevel.ERROR;
        }
      };

  @Test
  void testGetErrCode() {
    assertEquals(1, linkisException.getErrCode());
  }

  @Test
  void testSetErrCode() {
    linkisException.setErrCode(123);
    assertEquals(123, linkisException.getErrCode());
    linkisException.setErrCode(1);
    assertEquals(1, linkisException.getErrCode());
  }

  @Test
  void testGetDesc() {
    assertEquals("test", linkisException.getDesc());
  }

  @Test
  void testSetDesc() {
    linkisException.setDesc("test2");
    assertEquals("test2", linkisException.getDesc());
    linkisException.setDesc("test");
    assertEquals("test", linkisException.getDesc());
  }

  @Test
  void testGetIp() {
    assertEquals("127.0.0.1", linkisException.getIp());
  }

  @Test
  void testSetIp() {
    linkisException.setIp("0.0.0.0");
    assertEquals("0.0.0.0", linkisException.getIp());
    linkisException.setIp("127.0.0.1");
    assertEquals("127.0.0.1", linkisException.getIp());
  }

  @Test
  void testGetPort() {
    assertEquals(1, linkisException.getPort());
  }

  @Test
  void testSetPort() {
    linkisException.setPort(11);
    assertEquals(11, linkisException.getPort());
    linkisException.setPort(1);
    assertEquals(1, linkisException.getPort());
  }

  @Test
  void testGetServiceKind() {
    assertEquals("test", linkisException.getServiceKind());
  }

  @Test
  void testSetServiceKind() {
    linkisException.setServiceKind("test2");
    assertEquals("test2", linkisException.getServiceKind());
    linkisException.setServiceKind("test");
    assertEquals("test", linkisException.getServiceKind());
  }

  @Test
  void toMap() {
    Map<String, Object> map = new java.util.HashMap<String, Object>();
    map.put("level", ExceptionLevel.ERROR.getLevel());
    map.put("errCode", 1);
    map.put("desc", "test");
    map.put("ip", "127.0.0.1");
    map.put("port", 1);
    map.put("serviceKind", "test");
    assertEquals(map, linkisException.toMap());
  }

  @Test
  void testGetLevel() {
    assertEquals(ExceptionLevel.ERROR, linkisException.getLevel());
  }

  @Test
  void testToString() {
    assertEquals(
        "LinkisException{errCode=1, desc='test', ip='127.0.0.1', port=1, serviceKind='test'}",
        linkisException.toString());
  }
}
