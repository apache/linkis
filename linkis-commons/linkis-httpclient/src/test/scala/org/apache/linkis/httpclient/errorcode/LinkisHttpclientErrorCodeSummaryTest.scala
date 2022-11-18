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

package org.apache.linkis.httpclient.errorcode

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class LinkisHttpclientErrorCodeSummaryTest {

  @Test
  @DisplayName("enumTest")
  def enumTest(): Unit = {

    val connectToServerurlCode = LinkisHttpclientErrorCodeSummary.CONNECT_TO_SERVERURL.getErrorCode
    val requestFailedHttpCode = LinkisHttpclientErrorCodeSummary.REQUEST_FAILED_HTTP.getErrorCode
    val retryExceptionCode = LinkisHttpclientErrorCodeSummary.RETRY_EXCEPTION.getErrorCode
    val messagePareseExceptionCode =
      LinkisHttpclientErrorCodeSummary.MESSAGE_PARSE_EXCEPTION.getErrorCode
    val methodNotSupportExceptionCode =
      LinkisHttpclientErrorCodeSummary.METHOD_NOT_SUPPORT_EXCEPTION.getErrorCode

    Assertions.assertTrue(10901 == connectToServerurlCode.intValue())
    Assertions.assertTrue(10905 == requestFailedHttpCode.intValue())
    Assertions.assertTrue(10900 == retryExceptionCode.intValue())
    Assertions.assertTrue(10900 == messagePareseExceptionCode.intValue())
    Assertions.assertTrue(10902 == methodNotSupportExceptionCode.intValue())

  }

}
