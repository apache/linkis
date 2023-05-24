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

import org.apache.linkis.configuration.entity.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigMapperTest extends BaseDaoTest {
  @Autowired ConfigMapper configMapper;

  private ConfigValue insertConfigValue() {
    ConfigValue configValue = new ConfigValue();
    configValue.setId(8L);
    configValue.setConfigKeyId(8L);
    configValue.setConfigValue("100G");
    configValue.setConfigLabelId(1);
    configMapper.insertValue(configValue);
    return configValue;
  }

  private List<ConfigValue> insertConfigValueList() {
    ConfigValue configValue = new ConfigValue();
    configValue.setConfigKeyId(9L);
    configValue.setConfigValue("100G");
    configValue.setConfigLabelId(1);
    List<ConfigValue> configValues = new ArrayList<>();
    ConfigValue configValue2 = new ConfigValue();
    configValue2.setConfigKeyId(10L);
    configValue2.setConfigValue("130G");
    configValue2.setConfigLabelId(1);
    configValues.add(configValue);
    configValues.add(configValue2);
    configMapper.insertValueList(configValues);
    return configValues;
  }

  @Test
  void testGetConfigKeyByLabelIds() {
    List<ConfigKeyValue> configKeyValueList =
        configMapper.getConfigKeyByLabelIds(Arrays.asList(1, 2, 3));
    assertEquals(7, configKeyValueList.size());
  }

  @Test
  void testGetConfigKeyValueByLabelId() {
    List<ConfigKeyValue> configKeyValueList = configMapper.getConfigKeyValueByLabelId(1);
    assertEquals(7, configKeyValueList.size());
  }

  /**
   * When using the h2 library for testing,if the function(on conflict) is not supported,an error
   * will be reported, and the pg physical library will not guarantee an error pg使用h2库测试时不支持函数（on
   * conflict）会报错，pg实体库不会报错
   */
  @Test
  void testInsertValue() {
    ConfigValue result = insertConfigValue();
    assertTrue(result.getId() > 0);
  }

  @Test
  void testGetConfigValueById() {
    ConfigValue configValue = configMapper.getConfigValueById(6L);
    assertEquals("1", configValue.getConfigValue());
  }

  @Test
  void testInsertValueList() throws InterruptedException {
    List<ConfigValue> result = insertConfigValueList();
    assertTrue(result.get(0).getId() > 0 && result.get(1).getId() > 0);
  }

  @Test
  void testUpdateUserValue() {
    configMapper.updateUserValue("10", 1L);
    assertEquals("10", configMapper.getConfigValueById(1L).getConfigValue());
  }

  @Test
  void testUpdateUserValueList() {
    List<ConfigValue> configValueList = new ArrayList<>();
    ConfigValue configValue = new ConfigValue();
    configValue.setId(5L);
    configValue.setConfigKeyId(5L);
    configValue.setConfigValue("50G");
    configValue.setConfigLabelId(1);
    List<ConfigValue> configValues = new ArrayList<>();
    ConfigValue configValue2 = new ConfigValue();
    configValue2.setId(6L);
    configValue2.setConfigKeyId(6L);
    configValue2.setConfigValue("60G");
    configValue2.setConfigLabelId(1);
    configValueList.add(configValue);
    configValueList.add(configValue2);
    configMapper.updateUserValueList(configValueList);
    ConfigValue result = configMapper.getConfigValueById(5L);
    assertEquals("50G", result.getConfigValue());
  }

  @Test
  void testSelectKeyByKeyID() {
    ConfigKey configKey = configMapper.selectKeyByKeyID(1L);
    assertEquals("wds.linkis.rm.yarnqueue", configKey.getKey());
  }

  @Test
  void testSelectKeyByKeyName() {
    // TODO 查询结果转换异常
    //                ConfigKey configKey =
    // configMapper.selectKeyByKeyName("wds.linkis.rm.yarnqueue");
    //                assertEquals("ide", configKey.getDefaultValue());
    //                System.out.println(configKey.getDefaultValue());
  }

  @Test
  void testListKeyByStringValue() {
    //        List<ConfigKey> configKeyList = configMapper.listKeyByStringValue("*-*,*-*");
    //        assertEquals(7, configKeyList.size());
  }

  @Test
  void testInsertCreator() {
    // mapper方法没有对应的实现类
    //        configMapper.insertCreator("tom");
  }

  @Test
  void testGetCategory() {
    List<CategoryLabel> categoryLabelList = configMapper.getCategory();
    assertEquals(3, categoryLabelList.size());
  }

  @Test
  void testGetCategoryById() {
    CategoryLabel categoryLabel = configMapper.getCategoryById(1);
    //        assertEquals(1, categoryLabel.getLevel());
  }

  @Test
  void testInsertCategory() {
    CategoryLabel categoryLabel = new CategoryLabel();
    categoryLabel.setId(4);
    categoryLabel.setLevel(1);
    configMapper.insertCategory(categoryLabel);
    List<CategoryLabel> categoryLabelList = configMapper.getCategory();
    assertEquals(4, categoryLabelList.size());
  }

  @Test
  void testDeleteCategory() {
    configMapper.deleteCategory(Arrays.asList(1, 3));
    List<CategoryLabel> categoryLabelList = configMapper.getCategory();
    //        assertEquals(1, categoryLabelList.size());
  }

  @Test
  void testUpdateCategory() {
    CategoryLabel categoryLabel = new CategoryLabel();
    categoryLabel.setCategoryId(3);
    categoryLabel.setDescription("取值范围：1-555，单位：个");
    configMapper.updateCategory(categoryLabel);
    CategoryLabel result = configMapper.getCategoryById(3);
    assertEquals("取值范围：1-555，单位：个", result.getDescription());
  }

  @Test
  void testInsertKey() {
    ConfigKey configKey = new ConfigKey();
    configKey.setKey("wds.linkis.rm.instance.max.max");
    configMapper.insertKey(configKey);
    ConfigKey result = configMapper.selectKeyByKeyID(8L);
    //        assertEquals("wds.linkis.rm.instance.max.max", result.getKey());
  }
}
