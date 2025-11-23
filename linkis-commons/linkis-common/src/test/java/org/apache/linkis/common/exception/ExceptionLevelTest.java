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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionLevelTest {

  @Test
  void testGetLevel() {
    assertEquals(1, ExceptionLevel.WARN.getLevel());
    assertEquals(2, ExceptionLevel.ERROR.getLevel());
    assertEquals(3, ExceptionLevel.FATAL.getLevel());
    assertEquals(4, ExceptionLevel.RETRY.getLevel());
  }

  @Test
  void testSetLevel() {
    ExceptionLevel.WARN.setLevel(-1);
    assertEquals(-1, ExceptionLevel.WARN.getLevel());
    ExceptionLevel.WARN.setLevel(1);
    assertEquals(1, ExceptionLevel.WARN.getLevel());
  }

  @Test
  void testGetName() {
    assertEquals("warn", ExceptionLevel.WARN.getName());
    assertEquals("error", ExceptionLevel.ERROR.getName());
    assertEquals("fatal", ExceptionLevel.FATAL.getName());
    assertEquals("retry", ExceptionLevel.RETRY.getName());
  }

  @Test
  void testSetName() {
    ExceptionLevel.ERROR.setName("testError");
    assertEquals("testError", ExceptionLevel.ERROR.getName());
    ExceptionLevel.ERROR.setName("error");
    assertEquals("error", ExceptionLevel.ERROR.getName());
  }

  @Test
  void testToString() {
    assertEquals("ExceptionLevel{level=1, name='warn'}", ExceptionLevel.WARN.toString());
    assertEquals("ExceptionLevel{level=2, name='error'}", ExceptionLevel.ERROR.toString());
    assertEquals("ExceptionLevel{level=3, name='fatal'}", ExceptionLevel.FATAL.toString());
    assertEquals("ExceptionLevel{level=4, name='retry'}", ExceptionLevel.RETRY.toString());
  }
}
