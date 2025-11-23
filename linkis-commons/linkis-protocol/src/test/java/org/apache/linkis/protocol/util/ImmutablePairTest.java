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

package org.apache.linkis.protocol.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ImmutablePairTest {

  @Test
  @DisplayName("getKeyTest")
  public void getKeyTest() {

    ImmutablePair<String, String> immutablePair =
        new ImmutablePair<String, String>("hadoop", "hadoop");
    String key = immutablePair.getKey();
    Assertions.assertEquals("hadoop", key);
  }

  @Test
  @DisplayName("getValueTest")
  public void getValueTest() {

    ImmutablePair<String, String> immutablePair =
        new ImmutablePair<String, String>("hadoop", "hadoop");
    String pairValue = immutablePair.getValue();
    Assertions.assertEquals("hadoop", pairValue);
  }

  @Test
  @DisplayName("equalsTest")
  public void equalsTest() {

    ImmutablePair<String, String> immutablePair1 =
        new ImmutablePair<String, String>("hadoop", "hadoop");
    ImmutablePair<String, String> immutablePair2 =
        new ImmutablePair<String, String>("hadoop", "hadoop");

    boolean equals = immutablePair1.equals(immutablePair2);
    Assertions.assertTrue(equals);
  }
}
