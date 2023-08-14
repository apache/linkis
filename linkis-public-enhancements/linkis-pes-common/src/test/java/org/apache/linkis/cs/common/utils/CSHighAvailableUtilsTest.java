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

package org.apache.linkis.cs.common.utils;

import org.apache.linkis.cs.common.exception.CSErrorException;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CSHighAvailableUtilsTest {

  @Test
  @DisplayName("checkHAIDBasicFormatTest")
  public void checkHAIDBasicFormatTest() {

    String haid = "24--24--YmRwaGRwMTFpZGUwMTo5MTE2YmRwaGRwMTFpZGUwMTo5MTE084835";
    boolean checkHAIDBasicFormat = CSHighAvailableUtils.checkHAIDBasicFormat(haid);
    Assertions.assertFalse(checkHAIDBasicFormat);
  }

  @Test
  @DisplayName("encodeHAIDKeyTest")
  public void encodeHAIDKeyTest() throws CSErrorException {

    String id = "8798";
    String instance = "jslfjslfjlsdjfljsdf==+";
    String backupInstance = "sjljsljflsdjflsjd";
    List<String> list = new ArrayList<>();
    list.add(backupInstance);
    list.add(instance);
    String haid2 = CSHighAvailableUtils.encodeHAIDKey(id, instance, list);
    Assertions.assertNotNull(haid2);
  }

  @Test
  @DisplayName("decodeHAIDTest")
  public void decodeHAIDTest() throws CSErrorException {

    String haid3 = "24-24--YmRwaGRwMTFpZGUwMTo5MTE0YmRwaGRwMTFpZGUwMTo5MTE084855";
    String haidStr = new Gson().toJson(CSHighAvailableUtils.decodeHAID(haid3));

    Assertions.assertNotNull(haidStr);
  }
}
