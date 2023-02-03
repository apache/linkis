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

package org.apache.linkis.manager.label.builder;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;

/** CombinedLabelBuilder Tester */
@SpringBootTest(classes = {CombinedLabelBuilder.class})
public class CombinedLabelBuilderTest {

  @Autowired CombinedLabelBuilder combinedLabelBuilder;

  @Test
  public void testCanBuild() throws Exception {
    boolean flag = combinedLabelBuilder.canBuild(LabelKeyConstant.COMBINED_LABEL_KEY_PREFIX);
    Assertions.assertTrue(flag);
  }

  @Test
  public void testBuild() throws Exception {
    UserCreatorLabel userCreatorLabel1 = new UserCreatorLabel();
    userCreatorLabel1.setLabelKey("testLabelKey1");
    List<Label<?>> labels1 = new ArrayList<>();
    labels1.add(userCreatorLabel1);
    Label<?> label = combinedLabelBuilder.build("testLabelKey", labels1);
    Assertions.assertTrue(label.getLabelKey().equals("combined_testLabelKey1"));
  }
}
