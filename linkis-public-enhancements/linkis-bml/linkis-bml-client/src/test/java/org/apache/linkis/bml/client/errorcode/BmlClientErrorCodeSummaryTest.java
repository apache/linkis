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

package org.apache.linkis.bml.client.errorcode;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.bml.client.errorcode.BmlClientErrorCodeSummary.BML_CLIENT_FAILED;
import static org.apache.linkis.bml.client.errorcode.BmlClientErrorCodeSummary.POST_REQUEST_RESULT_NOT_MATCH;
import static org.apache.linkis.bml.client.errorcode.BmlClientErrorCodeSummary.SERVER_URL_NOT_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BmlClientErrorCodeSummaryTest {
  @Test
  void testGetErrorCode() {
    assertEquals(20060, POST_REQUEST_RESULT_NOT_MATCH.getErrorCode());
    assertEquals(20061, BML_CLIENT_FAILED.getErrorCode());
    assertEquals(20062, SERVER_URL_NOT_NULL.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "the result returned by the repository client POST request does not match(物料库客户端POST请求返回的result不匹配)",
        POST_REQUEST_RESULT_NOT_MATCH.getErrorDesc());
    assertEquals(
        "failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)",
        BML_CLIENT_FAILED.getErrorDesc());
    assertEquals("serverUrl cannot be null(serverUrl 不能为空)", SERVER_URL_NOT_NULL.getErrorDesc());
  }
}
