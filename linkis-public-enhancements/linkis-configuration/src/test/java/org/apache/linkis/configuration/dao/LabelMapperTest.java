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

package org.apache.linkis.configuration.dao;

import org.apache.linkis.configuration.entity.ConfigLabel;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LabelMapperTest extends BaseDaoTest {
  @Autowired private LabelMapper labelMapper;

  public ConfigLabel insertConfigLabel() {
    ConfigLabel configLabel = new ConfigLabel();
    configLabel.setLabelKey("h");
    configLabel.setStringValue("spark");
    configLabel.setCreator("linkis");
    configLabel.setLabelValueSize(2);
    configLabel.setUpdator("tom");
    configLabel.setCreateTime(new Date());
    configLabel.setUpdateTime(new Date());
    labelMapper.insertLabel(configLabel);
    return configLabel;
  }

  @Test
  void testGetLabelByKeyValue() {
    ConfigLabel configLabel =
        labelMapper.getLabelByKeyValue("combined_userCreator_engineType", "*-IDE,*-*");
    assertEquals(configLabel.getId(), 2);
  }

  @Test
  void testInsertLabel() {
    ConfigLabel configLabel = insertConfigLabel();
    assertTrue(configLabel.getId() > 0);
  }

  @Test
  void testDeleteLabel() {
    labelMapper.deleteLabel(Arrays.asList(1, 2));
    assertEquals(labelMapper.getLabelById(1), null);
    assertEquals(labelMapper.getLabelById(2), null);
  }

  @Test
  void testGetLabelById() {
    ConfigLabel configLabel = labelMapper.getLabelById(1);
    assertEquals(configLabel.getStringValue(), "*-*,*-*");
  }
}
