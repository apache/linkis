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

package org.apache.linkis.protocol.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EngineStateTest {

  @Test
  @DisplayName("isCompletedTest")
  public void isCompletedTest() {

    boolean successCompleted = EngineState.isCompleted(EngineState.Success);
    boolean errorCompleted = EngineState.isCompleted(EngineState.Error);
    boolean deadCompleted = EngineState.isCompleted(EngineState.Dead);
    boolean idleCompleted = EngineState.isCompleted(EngineState.Idle);

    Assertions.assertTrue(successCompleted);
    Assertions.assertTrue(errorCompleted);
    Assertions.assertTrue(deadCompleted);
    Assertions.assertFalse(idleCompleted);
  }

  @Test
  @DisplayName("isAvailableTest")
  public void isAvailableTest() {

    boolean idleAvailable = EngineState.isAvailable(EngineState.Idle);
    boolean busyAvailable = EngineState.isAvailable(EngineState.Busy);
    boolean deadAvailable = EngineState.isAvailable(EngineState.Dead);

    Assertions.assertTrue(idleAvailable);
    Assertions.assertTrue(busyAvailable);
    Assertions.assertFalse(deadAvailable);
  }
}
