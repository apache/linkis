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

package org.apache.linkis.cli.common.exception.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ErrorLevelTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    int infoLevel = ErrorLevel.INFO.getLevel();
    int warnLevel = ErrorLevel.WARN.getLevel();
    int errorLevel = ErrorLevel.ERROR.getLevel();
    int fatalLevel = ErrorLevel.FATAL.getLevel();
    int retryLevel = ErrorLevel.RETRY.getLevel();

    Assertions.assertTrue(0 == infoLevel);
    Assertions.assertTrue(1 == warnLevel);
    Assertions.assertTrue(2 == errorLevel);
    Assertions.assertTrue(3 == fatalLevel);
    Assertions.assertTrue(4 == retryLevel);
  }
}
