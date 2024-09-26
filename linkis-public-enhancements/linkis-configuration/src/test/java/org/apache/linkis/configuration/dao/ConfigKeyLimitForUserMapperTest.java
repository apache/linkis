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

import org.apache.linkis.configuration.entity.ConfigKeyLimitForUser;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigKeyLimitForUserMapperTest extends BaseDaoTest {

  @Autowired ConfigKeyLimitForUserMapper configKeyLimitForUserMapper;

  String uuid = UUID.randomUUID().toString();
  String name = "for-test";

  private List<ConfigKeyLimitForUser> initData() {
    List<ConfigKeyLimitForUser> list =
        Instancio.ofList(ConfigKeyLimitForUser.class)
            .generate(Select.field(ConfigKeyLimitForUser::getIsValid), gen -> gen.oneOf("Y", "N"))
            .create();
    ConfigKeyLimitForUser configKeyLimitForUser = new ConfigKeyLimitForUser();
    configKeyLimitForUser.setUserName("testuser");
    configKeyLimitForUser.setCombinedLabelValue("IDE-hadoop,spark-2.3.3");
    configKeyLimitForUser.setKeyId(1L);
    configKeyLimitForUser.setLatestUpdateTemplateUuid(uuid);
    configKeyLimitForUser.setCreateBy("test");
    configKeyLimitForUser.setUpdateBy("test");
    list.add(configKeyLimitForUser);
    configKeyLimitForUserMapper.batchInsertList(list);
    return list;
  }

  @Test
  void batchInsertOrUpdateListTest() {
    List<ConfigKeyLimitForUser> list = initData();
    list.get(1).setLatestUpdateTemplateUuid("123456");
    int isOk = configKeyLimitForUserMapper.batchInsertOrUpdateList(list);
    Assertions.assertTrue(isOk > 1);
  }
}
