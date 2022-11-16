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

package org.apache.linkis.cs.client.utils;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.CombinedNodeIDContextID;
import org.apache.linkis.cs.common.entity.source.CommonContextKey;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SerializeHelperTest {

  @Test
  @DisplayName("serializeContextIDTest")
  public void serializeContextIDTest() throws ErrorException {

    ContextID contextID = new CombinedNodeIDContextID();
    contextID.setContextId("90a6ee94-4bd6-47d9-a536-f92660c4c052");
    contextID.setContextIDType(1);

    String serializeContextID = SerializeHelper.serializeContextID(contextID);
    Assertions.assertNotNull(serializeContextID);
  }

  @Test
  @DisplayName("serializeContextKeyTest")
  public void serializeContextKeyTest() throws ErrorException {

    ContextKey contextKey = new CommonContextKey();
    contextKey.setType(1);
    contextKey.setKey("key");
    contextKey.setKeywords("keywords");
    contextKey.setContextScope(ContextScope.PUBLIC);
    contextKey.setContextType(ContextType.DATA);

    String serializeContextKey = SerializeHelper.serializeContextKey(contextKey);
    Assertions.assertNotNull(serializeContextKey);
  }
}
