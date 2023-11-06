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

package org.apache.linkis.filesystem.entity

import org.apache.linkis.storage.fs.impl.LocalFileSystem

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class FSInfoTest {

  @Test
  @DisplayName("timeoutTest")
  def timeoutTest(): Unit = {

    val info = new FSInfo("1", new LocalFileSystem(), System.currentTimeMillis())
    val timeout = info.timeout
    Assertions.assertFalse(timeout)

  }

}
