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

package org.apache.linkis.cs.common.entity.enumeration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextTypeTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    String metaData = ContextType.METADATA.toString();
    String data = ContextType.DATA.toString();
    String resource = ContextType.RESOURCE.toString();
    String object = ContextType.OBJECT.toString();
    String env = ContextType.ENV.toString();
    String cost = ContextType.COST.toString();
    String udf = ContextType.UDF.toString();
    String variable = ContextType.Variable.toString();

    Assertions.assertEquals("METADATA", metaData);
    Assertions.assertEquals("DATA", data);
    Assertions.assertEquals("RESOURCE", resource);
    Assertions.assertEquals("OBJECT", object);
    Assertions.assertEquals("ENV", env);
    Assertions.assertEquals("COST", cost);
    Assertions.assertEquals("UDF", udf);
    Assertions.assertEquals("Variable", variable);
  }
}
