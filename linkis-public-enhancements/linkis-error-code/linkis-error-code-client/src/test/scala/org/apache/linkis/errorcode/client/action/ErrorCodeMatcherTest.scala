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

package org.apache.linkis.errorcode.client.action

import org.apache.linkis.errorcode.client.utils.ErrorCodeMatcher
import org.apache.linkis.errorcode.common.LinkisErrorCode

import java.util

import org.junit.jupiter.api
import org.junit.jupiter.api.{Assertions, DisplayName}

class ErrorCodeMatcherTest {

  @api.Test
  @DisplayName("errorMatchTest")
  def errorMatchTest(): Unit = {
    val errorCodes = new util.ArrayList[LinkisErrorCode]
    errorCodes.add(new LinkisErrorCode("01105", "机器内存不足，请联系管理员扩容", "Cannot allocate memory", 0))
    val test = ErrorCodeMatcher.errorMatch(errorCodes, "Cannot allocate memory")
    if (test.isDefined) {
      val errorDesc = test.get._2
      Assertions.assertEquals("机器内存不足，请联系管理员扩容", errorDesc)
    }
  }

}
