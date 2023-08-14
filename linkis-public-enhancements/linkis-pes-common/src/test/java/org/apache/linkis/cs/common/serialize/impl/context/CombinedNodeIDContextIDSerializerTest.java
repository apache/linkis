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

import org.apache.linkis.cs.common.entity.source.CombinedNodeIDContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CombinedNodeIDContextIDSerializerTest {

  @Test
  @DisplayName("getTypeTest")
  public void getTypeTest() {

    CombinedNodeIDContextIDSerializer serializer = new CombinedNodeIDContextIDSerializer();
    String type = serializer.getType();
    Assertions.assertNotNull(type);
  }

  @Test
  @DisplayName("acceptsTest")
  public void acceptsTest() {

    CombinedNodeIDContextID contextID = new CombinedNodeIDContextID();
    CombinedNodeIDContextIDSerializer serializer = new CombinedNodeIDContextIDSerializer();
    boolean accepts = serializer.accepts(contextID);
    Assertions.assertTrue(accepts);
  }

  @Test
  @DisplayName("fromJsonTest")
  public void fromJsonTest() throws CSErrorException {

    CombinedNodeIDContextID combinedNodeIDContextID = new CombinedNodeIDContextID();
    combinedNodeIDContextID.setNodeID("node123");
    CombinedNodeIDContextIDSerializer serializer = new CombinedNodeIDContextIDSerializer();
    CombinedNodeIDContextID json = serializer.fromJson(new Gson().toJson(combinedNodeIDContextID));
    Assertions.assertNotNull(json);
  }
}
