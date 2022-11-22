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

package org.apache.linkis.cs.common.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextIDTypeTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    int commonIndex = ContextIDType.COMMON_CONTEXT_ID_TYPE.getIndex();
    String commonTypeName = ContextIDType.COMMON_CONTEXT_ID_TYPE.getTypeName();
    int linkisIndex = ContextIDType.LINKIS_WORKFLOW_CONTEXT_ID_TYPE.getIndex();
    String linkisTypeName = ContextIDType.LINKIS_WORKFLOW_CONTEXT_ID_TYPE.getTypeName();

    Assertions.assertTrue(0 == commonIndex);
    Assertions.assertTrue(1 == linkisIndex);

    Assertions.assertEquals(
        "org.apache.linkis.cs.common.entity.source.CommonContextID", commonTypeName);
    Assertions.assertEquals(
        "org.apache.linkis.cs.common.entity.source.LinkisWorkflowContextID", linkisTypeName);
  }
}
