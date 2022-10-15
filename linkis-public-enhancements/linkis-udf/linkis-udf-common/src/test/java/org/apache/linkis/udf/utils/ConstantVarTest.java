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

package org.apache.linkis.udf.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConstantVarTest {

  @Test
  @DisplayName("staticConstTest")
  public void staticConstTest() {

    Assertions.assertTrue(ConstantVar.UDF_JAR == 0);
    Assertions.assertTrue(ConstantVar.UDF_PY == 1);
    Assertions.assertTrue(ConstantVar.UDF_SCALA == 2);
    Assertions.assertTrue(ConstantVar.FUNCTION_PY == 3);
    Assertions.assertTrue(ConstantVar.FUNCTION_SCALA == 4);

    Assertions.assertEquals("function", ConstantVar.FUNCTION);
    Assertions.assertEquals("udf", ConstantVar.UDF);
    Assertions.assertEquals("all", ConstantVar.ALL);

    Assertions.assertEquals("sys", ConstantVar.SYS_USER);
    Assertions.assertEquals("bdp", ConstantVar.BDP_USER);
    Assertions.assertEquals("self", ConstantVar.SELF_USER);
    Assertions.assertEquals("share", ConstantVar.SHARE_USER);
    Assertions.assertEquals("expire", ConstantVar.EXPIRE_USER);

    Assertions.assertTrue(ConstantVar.specialTypes.length == 4);
  }
}
