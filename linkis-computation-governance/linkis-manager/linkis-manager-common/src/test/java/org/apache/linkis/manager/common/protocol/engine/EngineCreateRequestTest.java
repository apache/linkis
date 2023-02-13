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

package org.apache.linkis.manager.common.protocol.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineCreateRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testTimeout() {
    ObjectNode jNode = objectMapper.createObjectNode();
    // "timeout", "properties", "createService", "user", "description", "labels", "ignoreTimeout"
    jNode.put("timeout", 1000);
    jNode.put("user", "hadoop");
    jNode.put("description", "test for node");
    jNode.put("ignoreTimeout", false);

    try {
      EngineCreateRequest engineCreateRequest =
          objectMapper.treeToValue(jNode, EngineCreateRequest.class);
      assertEquals(engineCreateRequest.getTimeout(), jNode.get("timeout").asLong());
    } catch (JsonProcessingException e) {
      fail("Should not have thrown any exception", e);
    }
  }

  @Test
  void testTimeOut() {
    ObjectNode jNode = objectMapper.createObjectNode();
    // "timeout", "properties", "createService", "user", "description", "labels", "ignoreTimeout"
    jNode.put("timeOut", 1000);
    jNode.put("user", "hadoop");
    jNode.put("description", "test for node");
    jNode.put("ignoreTimeout", false);

    try {
      EngineCreateRequest engineCreateRequest =
          objectMapper.treeToValue(jNode, EngineCreateRequest.class);
      assertEquals(engineCreateRequest.getTimeout(), jNode.get("timeOut").asLong());
    } catch (JsonProcessingException e) {
      fail("Should not have thrown any exception", e);
    }
  }
}
