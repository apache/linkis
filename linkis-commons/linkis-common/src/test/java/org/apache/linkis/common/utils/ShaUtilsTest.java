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

package org.apache.linkis.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ShaUtilsTest {
  @Test
  void encryptTest() throws UnsupportedEncodingException {
    String applicationId = "test_application_id";
    String app_id = "test_app_id";
    String token = "test_token";
    String nonce = "123456";
    if (StringUtils.isBlank(applicationId)) {
      throw new LinkageError("Invalid applicationId cannot be empty");
    }
    if (StringUtils.isBlank(app_id)) {
      throw new LinkageError("Invalid app_id cannot be empty");
    }
    if (StringUtils.isBlank(token)) {
      throw new LinkageError("Invalid token cannot be empty");
    }
    if (StringUtils.isBlank(nonce)) {
      throw new LinkageError("Invalid nonce cannot be empty");
    }
    Map<String, String> parms = new HashMap<>();
    String timestampStr = String.valueOf(System.currentTimeMillis());
    parms.put("applicationId", applicationId);
    parms.put("app_id", app_id);
    parms.put("timestamp", timestampStr);
    parms.put("nonce", nonce);
    if (StringUtils.isNotBlank(token)) {
      String signature =
          SHAUtils.Encrypt(
              SHAUtils.Encrypt(parms.get("app_id") + nonce + timestampStr, null) + token, null);
      parms.put("signature", signature);
    }
  }
}
