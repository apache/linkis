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

package org.apache.linkis.engineplugin.presto.factory;

import org.apache.linkis.engineconn.common.creation.DefaultEngineCreationContext;
import org.apache.linkis.engineconn.common.creation.EngineCreationContext;
import org.apache.linkis.engineconn.common.engineconn.EngineConn;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPrestoEngineConnFactory {

  @Test
  public void testNewExecutor() {
    System.setProperty("prestoVersion", "presto");
    PrestoEngineConnFactory engineConnFactory = new PrestoEngineConnFactory();
    EngineCreationContext engineCreationContext = new DefaultEngineCreationContext();
    HashMap<String, String> jMap = new HashMap<>();
    jMap.put("presto.version", "presto");
    engineCreationContext.setOptions(jMap);
    EngineConn engineConn = engineConnFactory.createEngineConn(engineCreationContext);
    Object executor = engineConnFactory.newExecutor(1, engineCreationContext, engineConn);
    Assertions.assertNotNull(executor);
  }
}
