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

package org.apache.linkis.manager.label.utils;

import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;

/** LabelUtils Tester */
public class LabelUtilsTest {

  @Test
  public void testIsBasicType() throws Exception {
    boolean flag = LabelUtils.isBasicType(String.class);
    Assertions.assertTrue(flag);
  }

  @Test
  public void testGetOrderedValueNameInLabelClass() throws Exception {
    String[] VALUE_METHOD_PREFIX = new String[] {"is", "get", "set"};
    List<String> list =
        LabelUtils.getOrderedValueNameInLabelClass(this.getClass(), VALUE_METHOD_PREFIX);
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void testDistinctLabel() throws Exception {
    UserCreatorLabel userCreatorLabel1 = new UserCreatorLabel();
    userCreatorLabel1.setLabelKey("testLabelKey1");
    UserCreatorLabel userCreatorLabel2 = new UserCreatorLabel();
    userCreatorLabel2.setLabelKey("testLabelKey1");
    List<Label<?>> labels1 = new ArrayList<>();
    labels1.add(userCreatorLabel1);
    List<Label<?>> labels2 = new ArrayList<>();
    labels2.add(userCreatorLabel2);
    List<Label<?>> labels3 = LabelUtils.distinctLabel(labels1, labels2);
    Assertions.assertTrue(labels3.size() == 1);
  }
}
