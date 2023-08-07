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

package org.apache.linkis.cs.common.serialize.impl.context;

import org.apache.linkis.cs.common.entity.source.CommonContextKey;
import org.apache.linkis.cs.common.exception.CSErrorException;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommonContextKeySerializerTest {

  @Test
  @DisplayName("getTypeTest")
  public void getTypeTest() {

    CommonContextKeySerializer commonContextKeySerializer = new CommonContextKeySerializer();
    String type = commonContextKeySerializer.getType();
    Assertions.assertNotNull(type);
  }

  @Test
  @DisplayName("acceptsTest")
  public void acceptsTest() {

    CommonContextKey commonContextKey = new CommonContextKey();
    CommonContextKeySerializer commonContextKeySerializer = new CommonContextKeySerializer();
    boolean accepts = commonContextKeySerializer.accepts(commonContextKey);
    Assertions.assertTrue(accepts);
  }

  @Test
  @DisplayName("fromJsonTest")
  public void fromJsonTest() throws CSErrorException {

    CommonContextKey commonContextKey = new CommonContextKey();
    commonContextKey.setKey("key");

    CommonContextKeySerializer serializer = new CommonContextKeySerializer();
    CommonContextKey json = serializer.fromJson(new Gson().toJson(commonContextKey));
    Assertions.assertNotNull(json);
  }
}
