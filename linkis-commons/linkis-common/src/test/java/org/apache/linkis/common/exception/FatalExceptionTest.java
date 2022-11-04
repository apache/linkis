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

class FatalExceptionTest {

  @Test
  void testFatalException() {
    FatalException fatalException1 = new FatalException(1, "test");
    assertEquals(ExceptionLevel.FATAL, fatalException1.getLevel());
    assertEquals("test", fatalException1.getDesc());
    FatalException fatalException2 =
        new FatalException(3, "test", "127.0.0.1", 1234, "serviceKind");
    assertEquals(ExceptionLevel.FATAL, fatalException2.getLevel());
    assertEquals("test", fatalException2.getDesc());
    assertEquals("127.0.0.1", fatalException2.getIp());
    assertEquals(1234, fatalException2.getPort());
    assertEquals("serviceKind", fatalException2.getServiceKind());
  }

  @Test
  void getLevel() {
    FatalException fatalException = new FatalException(1, "test");
    assertEquals(ExceptionLevel.FATAL, fatalException.getLevel());
  }
}
