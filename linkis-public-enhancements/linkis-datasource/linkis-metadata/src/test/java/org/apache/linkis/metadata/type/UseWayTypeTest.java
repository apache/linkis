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

package org.apache.linkis.metadata.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UseWayTypeTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    String onceWriteMultiReadName = UseWayType.OnceWriteMultiRead.getName();
    String multiOverwriteName = UseWayType.MultiOverwrite.getName();
    String crudName = UseWayType.CRUD.getName();
    String occasionllyReadName = UseWayType.OnceWriteOccasionllyRead.getName();

    Assertions.assertEquals("一次写入多次读", onceWriteMultiReadName);
    Assertions.assertEquals("多次覆盖写", multiOverwriteName);
    Assertions.assertEquals("增删改查", crudName);
    Assertions.assertEquals("一次写偶尔读", occasionllyReadName);
  }
}
