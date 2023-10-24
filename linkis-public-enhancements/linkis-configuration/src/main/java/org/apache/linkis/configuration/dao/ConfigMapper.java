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

import org.apache.linkis.configuration.entity.*;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfigMapper {

  List<ConfigKeyValue> getConfigKeyByLabelIds(@Param("ids") List<Integer> ids);

  List<ConfigKeyValue> getConfigKeyValueByLabelId(@Param("labelId") Integer labelId);

  void insertValue(ConfigValue configValue);

  int batchInsertOrUpdateValueList(List<ConfigValue> list);

  ConfigValue getConfigValueById(@Param("id") Long id);

  ConfigValue getConfigValueByKeyAndLabel(ConfigValue configValue);

  void deleteConfigKeyValue(ConfigValue configValue);

  void insertValueList(@Param("configValues") List<ConfigValue> configValues);

  void updateUserValue(@Param("value") String value, @Param("id") Long id);

  void updateUserValueList(List<ConfigValue> configValueList);

  ConfigKey selectKeyByKeyID(@Param("id") Long keyID);

  List<ConfigKey> selectKeyByKeyName(@Param("keyName") String keyName);

  List<ConfigKey> selectKeyByEngineType(@Param("engineType") String engineType);

  List<ConfigKey> selectKeyByEngineTypeAndKeyList(
      @Param("engineType") String engineType, @Param("keyList") List<String> keyList);

  List<ConfigKey> selectKeyByKeyIdList(@Param("keyIdList") List<Long> keyList);

  List<ConfigKey> listKeyByStringValue(@Param("stringValue") String stringValue);

  List<CategoryLabel> getCategory();

  CategoryLabel getCategoryById(@Param("id") Integer id);

  void insertCategory(CategoryLabel categoryLabel);

  void deleteCategory(@Param("ids") List<Integer> ids);

  void updateCategory(CategoryLabel categoryLabel);

  void insertKey(ConfigKey key);

  List<ConfigKeyValue> getConfigEnKeyValueByLabelId(@Param("labelId") Integer labelId);

  void deleteConfigKey(@Param("id") Integer id);

  List<ConfigKey> getConfigBykey(@Param("engineType") String engineType, @Param("key") String key);

  List<ConfigKey> getConfigEnBykey(
      @Param("engineType") String engineType, @Param("key") String key);

  List<ConfigUserValue> getUserConfigValue(
      @Param("key") String key,
      @Param("user") String user,
      @Param("creator") String creator,
      @Param("engineType") String engineType);

  void insertKeyByBase(ConfigKey configKey);

  void updateConfigKey(ConfigKey configKey);
}
