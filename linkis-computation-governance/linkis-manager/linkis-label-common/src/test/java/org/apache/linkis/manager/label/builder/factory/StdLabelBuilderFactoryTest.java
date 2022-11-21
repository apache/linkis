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

package org.apache.linkis.manager.label.builder.factory;

import org.apache.linkis.manager.label.builder.CombinedLabelBuilder;
import org.apache.linkis.manager.label.builder.LabelBuilder;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.*;

/** StdLabelBuilderFactory Tester */
@SpringBootTest(classes = {StdLabelBuilderFactory.class})
public class StdLabelBuilderFactoryTest {

  @Autowired StdLabelBuilderFactory stdLabelBuilderFactory;

  @Test
  public void testRegisterLabelBuilder() throws Exception {
    LabelBuilder labelBuilder = new CombinedLabelBuilder();
    stdLabelBuilderFactory.registerLabelBuilder(labelBuilder);
  }

  @Test
  public void testCreateLabelOutLabelClass() throws Exception {
    EMInstanceLabel emInstanceLabel = new EMInstanceLabel();
    emInstanceLabel.setLabelKey("testLabelKey");
    EMInstanceLabel emInstanceLabel1 = stdLabelBuilderFactory.createLabel(EMInstanceLabel.class);
    Assertions.assertTrue(emInstanceLabel1.getLabelKey().equals("emInstance"));
  }

  @Test
  public void testCreateLabelForOutLabelClassOutValueTypes() throws Exception {
    EMInstanceLabel emInstanceLabel = new EMInstanceLabel();
    emInstanceLabel.setLabelKey("testLabelKey");
    EMInstanceLabel emInstanceLabel1 =
        stdLabelBuilderFactory.createLabel(EMInstanceLabel.class, null);
    Assertions.assertTrue(emInstanceLabel1.getLabelKey().equals("emInstance"));
  }

  @Test
  public void testCreateLabelForInLabelKeyInValueObjOutLabelClass() throws Exception {
    EMInstanceLabel emInstanceLabel = new EMInstanceLabel();
    emInstanceLabel.setLabelKey("testLabelKey");
    EMInstanceLabel emInstanceLabel1 =
        stdLabelBuilderFactory.createLabel("testLabelKey", emInstanceLabel, EMInstanceLabel.class);
    Assertions.assertTrue(emInstanceLabel1.getLabelKey().equals("emInstance"));
  }

  @Test
  public void testCreateLabelForInLabelKeyInValueStreamOutLabelClassOutValueTypes()
      throws Exception {
    EMInstanceLabel emInstanceLabel = new EMInstanceLabel();
    emInstanceLabel.setLabelKey("testLabelKey");
    EMInstanceLabel emInstanceLabel1 =
        stdLabelBuilderFactory.createLabel("testLabelKey", null, EMInstanceLabel.class, null);
    Assertions.assertTrue(emInstanceLabel1.getLabelKey().equals("emInstance"));
  }
}
