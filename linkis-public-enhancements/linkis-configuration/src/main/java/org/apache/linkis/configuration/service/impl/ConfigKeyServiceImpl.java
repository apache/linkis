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

package org.apache.linkis.configuration.service.impl;

import org.apache.linkis.configuration.dao.ConfigMapper;
import org.apache.linkis.configuration.dao.LabelMapper;
import org.apache.linkis.configuration.entity.*;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.ConfigKeyService;
import org.apache.linkis.configuration.util.LabelEntityParser;
import org.apache.linkis.configuration.util.LabelParameterParser;
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder;
import org.apache.linkis.manager.label.entity.CombinedLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.utils.LabelUtil;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.configuration.errorcode.LinkisConfigurationErrorCodeSummary.*;

@Service
public class ConfigKeyServiceImpl implements ConfigKeyService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigKeyServiceImpl.class);

  @Autowired private ConfigMapper configMapper;

  @Autowired private LabelMapper labelMapper;

  private CombinedLabelBuilder combinedLabelBuilder = new CombinedLabelBuilder();

  @Override
  public ConfigValue saveConfigValue(ConfigKeyValue configKeyValue, List<Label<?>> labelList)
      throws ConfigurationException {

    if (StringUtils.isBlank(configKeyValue.getKey())) {
      throw new ConfigurationException(KEY_CANNOT_EMPTY.getErrorDesc());
    }

    LabelParameterParser.labelCheck(labelList);
    List<ConfigKey> configKeys = configMapper.selectKeyByKeyName(configKeyValue.getKey());
    if (null == configKeys || configKeys.isEmpty()) {
      throw new ConfigurationException(
          MessageFormat.format(CONFIG_KEY_NOT_EXISTS.getErrorDesc(), configKeyValue.getKey()));
    }
    ConfigKey configKey = configKeys.get(0);
    EngineTypeLabel engineTypeLabel = LabelUtil.getEngineTypeLabel(labelList);
    for (ConfigKey key : configKeys) {
      if (engineTypeLabel.getEngineType().equalsIgnoreCase(key.getEngineType())) {
        logger.info("config key:{} will be use engineType {}", key.getKey(), key.getEngineType());
        configKey = key;
      }
    }
    CombinedLabel combinedLabel = getCombinedLabel(labelList);

    ConfigLabel configLabel =
        labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey(), combinedLabel.getStringValue());
    if (null == configLabel || configLabel.getId() < 0) {
      configLabel = LabelEntityParser.parseToConfigLabel(combinedLabel);
      labelMapper.insertLabel(configLabel);
      logger.info("succeed to create label: {}", configLabel.getStringValue());
    }
    List<ConfigValue> configValues = getConfigValue(configKeyValue.getKey(), labelList);
    ConfigValue configValue = null;
    if (configValues.size() > 1) {
      throw new ConfigurationException(
          combinedLabel.getStringValue()
              + "There are multiple values for the corresponding Key： "
              + configKeyValue.getKey());
    } else if (configValues.size() == 1) {
      configValue = configValues.get(0);
      configValue.setConfigValue(configKeyValue.getConfigValue());
    } else {
      configValue = new ConfigValue();
      configValue.setConfigKeyId(configKey.getId());
      configValue.setConfigValue(configKeyValue.getConfigValue());
      configValue.setConfigLabelId(configLabel.getId());
    }
    configMapper.insertValue(configValue);
    logger.info(
        "succeed to save key: {} by label: {} value: {} ",
        configKeyValue.getKey(),
        combinedLabel.getStringValue(),
        configKeyValue.getConfigValue());
    return configValue;
  }

  private CombinedLabel getCombinedLabel(List<Label<?>> labelList) throws ConfigurationException {
    CombinedLabel combinedLabel = null;
    try {
      combinedLabel = (CombinedLabel) combinedLabelBuilder.build("", labelList);
    } catch (LabelErrorException e) {
      throw new ConfigurationException(FAILED_TO_BUILD_LABEL.getErrorDesc(), e);
    }
    if (null == combinedLabel) {
      throw new ConfigurationException(BUILD_LABEL_IS_NULL.getErrorDesc());
    }
    return combinedLabel;
  }

  @Override
  public List<ConfigValue> getConfigValue(String key, List<Label<?>> labelList)
      throws ConfigurationException {
    if (StringUtils.isBlank(key)) {
      throw new ConfigurationException(CONFIGKEY_CANNOT_BE_NULL.getErrorDesc());
    }
    LabelParameterParser.labelCheck(labelList);
    List<ConfigKey> configKeys = configMapper.selectKeyByKeyName(key);

    if (null == configKeys || configKeys.isEmpty()) {
      throw new ConfigurationException(
          MessageFormat.format(CONFIG_KEY_NOT_EXISTS.getErrorDesc(), key));
    }
    CombinedLabel combinedLabel = getCombinedLabel(labelList);

    ConfigLabel configLabel =
        labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey(), combinedLabel.getStringValue());
    if (null == configLabel || configLabel.getId() < 0) {
      throw new ConfigurationException(
          MessageFormat.format(LABEL_NOT_EXISTS.getErrorDesc(), combinedLabel.getStringValue()));
    }
    List<ConfigValue> configValues = new ArrayList<>();
    for (ConfigKey configKey : configKeys) {
      ConfigValue configValue = new ConfigValue();
      configValue.setConfigKeyId(configKey.getId());
      configValue.setConfigLabelId(configLabel.getId());
      ConfigValue configValueByKeyAndLabel = configMapper.getConfigValueByKeyAndLabel(configValue);
      if (null != configValueByKeyAndLabel) {
        configValues.add(configValueByKeyAndLabel);
      }
    }
    return configValues;
  }

  @Override
  public List<ConfigKey> getConfigKeyList(String engineType) throws ConfigurationException {
    return configMapper.selectKeyByEngineType(engineType);
  }

  @Override
  public List<ConfigValue> deleteConfigValue(String key, List<Label<?>> labelList)
      throws ConfigurationException {
    CombinedLabel combinedLabel = getCombinedLabel(labelList);
    List<ConfigValue> configValues = getConfigValue(key, labelList);
    for (ConfigValue configValue : configValues) {
      configMapper.deleteConfigKeyValue(configValue);
    }
    logger.info("succeed to remove key: {} by label:{} ", key, combinedLabel.getStringValue());
    return configValues;
  }

  @Override
  public List<ConfigKey> getConfigBykey(String engineType, String key, String language) {
    List<ConfigKey> configkeyList;
    if ("en".equals(language)) {
      configkeyList = configMapper.getConfigEnBykey(engineType, key);
    } else {
      configkeyList = configMapper.getConfigBykey(engineType, key);
    }
    return configkeyList;
  }

  @Override
  public void deleteConfigById(Integer id) {
    configMapper.deleteConfigKey(id);
  }

  @Override
  public ConfigKey saveConfigKey(ConfigKey configKey) {
    configMapper.insertKeyByBase(configKey);
    return null;
  }

  @Override
  public List<ConfigUserValue> getUserConfigValue(
      String engineType, String key, String creator, String user) {
    return configMapper.getUserConfigValue(key, user, creator, engineType);
  }

  @Override
  public void updateConfigKey(ConfigKey configKey) {
    configMapper.updateConfigKey(configKey);
  }
}
