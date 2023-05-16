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

package org.apache.linkis.manager.label.entity.engine;

import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** EngineTypeLabel Tester */
public class EngineTypeLabelTest {

  @Test
  public void testSetStringValue() {
    LabelBuilderFactory labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();
    EngineTypeLabel engineTypeLabel = labelBuilderFactory.createLabel(EngineTypeLabel.class);

    // str value
    String hiveEngineType = "hive";
    String hiveVersion = "1.1.0-cdh5.12.0";
    engineTypeLabel.setStringValue(hiveEngineType + "-" + hiveVersion);
    Assertions.assertEquals(engineTypeLabel.getEngineType(), hiveEngineType);
    Assertions.assertEquals(engineTypeLabel.getVersion(), hiveVersion);

    // any value
    String anyEngineType = "*";
    String anyVersion = "*";
    engineTypeLabel.setStringValue(anyEngineType + "-" + anyVersion);
    Assertions.assertEquals(engineTypeLabel.getEngineType(), anyEngineType);
    Assertions.assertEquals(engineTypeLabel.getVersion(), anyVersion);

    // map value
    String mapStringValue = "{\"engineType\":\"shell\",\"version\":\"1\"}";
    engineTypeLabel.setStringValue(mapStringValue);
    Assertions.assertEquals(engineTypeLabel.getEngineType(), "shell");
    Assertions.assertEquals(engineTypeLabel.getVersion(), "1");

    // empty value will treat as *
    String emptyStringValue = "";
    engineTypeLabel.setStringValue(emptyStringValue);
    Assertions.assertEquals(engineTypeLabel.getEngineType(), "*");
    Assertions.assertEquals(engineTypeLabel.getVersion(), "*");

    // null value will treat as *
    engineTypeLabel.setStringValue(null);
    Assertions.assertEquals(engineTypeLabel.getEngineType(), "*");
    Assertions.assertEquals(engineTypeLabel.getVersion(), "*");
  }
}
