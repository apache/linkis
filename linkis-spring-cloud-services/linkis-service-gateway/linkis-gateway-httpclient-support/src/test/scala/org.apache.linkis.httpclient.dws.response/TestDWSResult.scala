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

package org.apache.linkis.httpclient.dws.response

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class TestDWSResult extends DWSResult {

  @Test
  @DisplayName("httpClientResultExceptionTest")
  def httpClientResultExceptionTest(): Unit = {

    val url = "/api/rest_j/v1/bml/updateVersion"
    val responseBody =
      "{\"timestamp\":1670052820607,\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"\",\"path\":\"/api/rest_j/v1/bml/updateVersion\"}."
    try {
      new TestDWSResult().set(responseBody, 500, url, "application/json")
    } catch {
      case ioe: Exception =>
        Assertions.assertEquals(
          ioe.getMessage,
          "errCode: 10905 ,desc: URL /api/rest_j/v1/bml/updateVersion request failed! " +
            "ResponseBody is {\"timestamp\":1670052820607,\"status\":500,\"error\":\"Internal Server Error\"," +
            "\"message\":\"\",\"path\":\"/api/rest_j/v1/bml/updateVersion\"}.. ,ip: null ,port: 0 ,serviceKind: null"
        )
    }
  }

}
