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

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigMapperTest extends BaseDaoTest {
    @Autowired ConfigMapper configMapper;

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

    @Test
    void testInsertValue() {
        ConfigValue configValue = new ConfigValue();
        configValue.setId(8L);
        configValue.setConfigKeyId(8L);
        configValue.setConfigValue("100G");
        configValue.setConfigLabelId(1);
        configMapper.insertValue(configValue);
        ConfigValue result = configMapper.getConfigValueById(8L);
        assertEquals("100G", result.getConfigValue());
    }

    @Test
    void testGetConfigValueById() {
        ConfigValue configValue = configMapper.getConfigValueById(6L);
        assertEquals("1", configValue.getConfigValue());
    }

    @Test
    void testInsertValueList() throws InterruptedException {
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
        assertEquals("130G", configMapper.getConfigValueById(10L).getConfigValue());
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
        assertEquals("50G", configMapper.getConfigValueById(5L).getConfigValue());
    }

    @Test
    void testSelectKeyByKeyID() {
        ConfigKey configKey = configMapper.selectKeyByKeyID(1L);
        assertEquals("wds.linkis.rm.yarnqueue", configKey.getKey());
    }

    @Test
    void testSeleteKeyByKeyName() {
        // TODO 查询结果转换异常
        //        ConfigKey configKey = configMapper.seleteKeyByKeyName("wds.linkis.rm.yarnqueue");
        //        assertEquals("ide", configKey.getDefaultValue());
        //        System.out.println(configKey.getDefaultValue());
    }

    @Test
    void testListKeyByStringValue() {
        List<ConfigKey> configKeyList = configMapper.listKeyByStringValue("*-*,*-*");
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
