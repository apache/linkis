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

package org.apache.linkis.configuration.service;

import org.apache.linkis.configuration.entity.ConfigKey;
import org.apache.linkis.configuration.entity.ConfigKeyValue;
import org.apache.linkis.configuration.entity.ConfigUserValue;
import org.apache.linkis.configuration.entity.ConfigValue;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.manager.label.entity.Label;

import java.util.List;

public interface ConfigKeyService {

  ConfigValue saveConfigValue(ConfigKeyValue configKeyValue, List<Label<?>> labelList)
      throws ConfigurationException;

  List<ConfigValue> getConfigValue(String configKey, List<Label<?>> labelList)
      throws ConfigurationException;

  List<ConfigKey> getConfigKeyList(String engineType) throws ConfigurationException;

  List<ConfigValue> deleteConfigValue(String configKey, List<Label<?>> labelList)
      throws ConfigurationException;

  List<ConfigKey> getConfigBykey(String engineType, String key, String language);

  void deleteConfigById(Integer id);

  ConfigKey saveConfigKey(ConfigKey configKey);

  List<ConfigUserValue> getUserConfigValue(
      String engineType, String key, String creator, String user);

  void updateConfigKey(ConfigKey configKey);
}
