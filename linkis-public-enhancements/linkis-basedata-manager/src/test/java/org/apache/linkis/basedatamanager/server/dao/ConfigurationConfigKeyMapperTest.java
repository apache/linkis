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

package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigKey;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationConfigKeyMapperTest extends BaseDaoTest {

  @Autowired ConfigurationConfigKeyMapper configurationConfigKeyMapper;

  private ConfigurationConfigKey insert() {
    ConfigurationConfigKey configKey = new ConfigurationConfigKey();
    configKey.setKey("key");
    configKey.setAdvanced(1);
    configKey.setDefaultValue("defaultValue");
    configKey.setDescription("desc");
    configKey.setEngineConnType("engineConnType");
    configKey.setHidden(1);
    configKey.setLevel(1);
    configKey.setTreeName("treeName");
    configKey.setName("name");
    configKey.setValidateRange("validateRange");
    configKey.setValidateType("validateType");
    int insert = configurationConfigKeyMapper.insert(configKey);
    assertTrue(insert > 0);
    return configKey;
  }

  @Test
  void testInsert() {
    insert();
  }

  @Test
  void testUpdate() {
    ConfigurationConfigKey insert = insert();
    ConfigurationConfigKey configKey = new ConfigurationConfigKey();
    configKey.setKey("key2");
    configKey.setAdvanced(0);
    configKey.setDefaultValue("defaultValue2");
    configKey.setDescription("desc2");
    configKey.setEngineConnType("engineConnType2");
    configKey.setHidden(0);
    configKey.setLevel(2);
    configKey.setTreeName("treeName2");
    configKey.setName("name2");
    configKey.setValidateRange("validateRange2");
    configKey.setValidateType("validateType2");
    configKey.setId(insert.getId());
    int updateKey = configurationConfigKeyMapper.updateById(configKey);
    assertTrue(updateKey > 0);
  }

  @Test
  void getTemplateListByLabelId() {
    configurationConfigKeyMapper.getTemplateListByLabelId("6");
  }

  @Test
  void deleteById() {
    ConfigurationConfigKey insert = insert();
    int deleteKey = configurationConfigKeyMapper.deleteById(insert.getId());
    assertTrue(deleteKey > 0);
  }
}
