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

import org.apache.linkis.configuration.entity.TemplateConfigKey;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateConfigKeyMapperTest extends BaseDaoTest {

  @Autowired TemplateConfigKeyMapper templateConfigKeyMapper;
  String uuid = UUID.randomUUID().toString();
  String name = "for-test";

  private List<TemplateConfigKey> initData() {
    List<TemplateConfigKey> list =
        Instancio.ofList(TemplateConfigKey.class)
            .size(9)
            .generate(Select.field(TemplateConfigKey::getIsValid), gen -> gen.oneOf("Y", "N"))
            .create();

    TemplateConfigKey templateConfigKey = new TemplateConfigKey();
    templateConfigKey.setTemplateName(name);
    templateConfigKey.setTemplateUuid(uuid);
    templateConfigKey.setKeyId(1L);
    templateConfigKey.setConfigValue("3");
    templateConfigKey.setMaxValue("8");
    templateConfigKey.setCreateBy("test");
    templateConfigKey.setUpdateBy("test");
    list.add(templateConfigKey);
    templateConfigKeyMapper.batchInsertList(list);
    return list;
  }

  @Test
  void selectListByTemplateUuid() {
    initData();
    List<TemplateConfigKey> res = templateConfigKeyMapper.selectListByTemplateUuid(uuid);
    assertEquals(res.size(), 1);
    assertEquals(res.get(0).getTemplateName(), name);
  }

  @Test
  void deleteByTemplateUuidAndKeyIdList() {
    List<TemplateConfigKey> list = initData();
    List<Long> KeyIdList = new ArrayList<>();
    KeyIdList.add(1L);
    int num = templateConfigKeyMapper.deleteByTemplateUuidAndKeyIdList(uuid, KeyIdList);
    assertEquals(num, 1);
  }

  @Test
  void batchInsertOrUpdateList() {
    List<TemplateConfigKey> list = initData();
    list.get(1).setConfigValue("20");
    int isOK = templateConfigKeyMapper.batchInsertOrUpdateList(list);
    Assertions.assertTrue(isOK >= 1);
  }

  @Test
  void selectListByTemplateUuidList() {
    List<TemplateConfigKey> list = initData();
    List<String> templateUuidList = new ArrayList<>();
    templateUuidList.add(uuid);
    templateUuidList.add("123456");
    List<TemplateConfigKey> res =
        templateConfigKeyMapper.selectListByTemplateUuidList(templateUuidList);
    Assertions.assertTrue(res.size() == 1);
  }
}
