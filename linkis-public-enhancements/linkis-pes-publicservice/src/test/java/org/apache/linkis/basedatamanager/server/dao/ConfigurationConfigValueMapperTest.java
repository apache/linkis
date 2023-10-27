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

import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigValue;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationConfigValueMapperTest extends BaseDaoTest {

  @Autowired ConfigurationConfigValueMapper configValueMapper;

  ConfigurationConfigValue insert() {
    ConfigurationConfigValue configValue = new ConfigurationConfigValue();
    configValue.setConfigValue("configValue");
    configValue.setConfigKeyId(1L);
    configValue.setConfigLabelId(1);
    configValue.setCreateTime(LocalDateTime.now());
    configValue.setUpdateTime(LocalDateTime.now());
    int insert = configValueMapper.insert(configValue);
    System.out.println(insert > 0);
    return configValue;
  }

  @Test
  void testInsert() {
    insert();
  }

  @Test
  void updateByKeyId() {
    ConfigurationConfigValue insert = insert();
    insert.setConfigValue("configVaule2");
    int i = configValueMapper.updateByKeyId(insert);
    assertTrue(i > 0);
  }

  @Test
  void deleteByKeyId() {
    ConfigurationConfigValue insert = insert();
    int i = configValueMapper.deleteByKeyId(insert.getConfigKeyId());
    assertTrue(i > 0);
  }
}
