/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.linkis.basedatamanager.server.service.impl;

import org.apache.linkis.basedatamanager.server.dao.CgManagerLabelMapper;
import org.apache.linkis.basedatamanager.server.dao.ConfigurationConfigKeyMapper;
import org.apache.linkis.basedatamanager.server.dao.ConfigurationConfigValueMapper;
import org.apache.linkis.basedatamanager.server.dao.ConfigurationKeyEngineRelationMapper;
import org.apache.linkis.basedatamanager.server.domain.CgManagerLabel;
import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigKey;
import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigValue;
import org.apache.linkis.basedatamanager.server.domain.ConfigurationKeyEngineRelation;
import org.apache.linkis.basedatamanager.server.request.ConfigurationTemplateSaveRequest;
import org.apache.linkis.basedatamanager.server.service.ConfigurationTemplateService;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/** This module is designed to manage configuration parameter templates */
@Service
public class ConfigurationTemplateServiceImpl implements ConfigurationTemplateService {
  @Resource ConfigurationConfigKeyMapper configKeyMapper;
  @Resource ConfigurationKeyEngineRelationMapper relationMapper;
  @Resource CgManagerLabelMapper managerLabelMapper;
  @Resource ConfigurationConfigValueMapper configValueMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean saveConfigurationTemplate(ConfigurationTemplateSaveRequest request) {
    // check label is exist
    CgManagerLabel labelQuery = new CgManagerLabel();
    labelQuery.setLabelKey("combined_userCreator_engineType");
    labelQuery.setLabelValue("*-*," + request.getCategoryName());
    CgManagerLabel label = managerLabelMapper.selectOne(new QueryWrapper<>(labelQuery));

    if (Objects.isNull(label)) {
      labelQuery.setLabelFeature("OPTIONAL");
      labelQuery.setLabelValueSize(2);
      managerLabelMapper.insert(labelQuery);
      label = managerLabelMapper.selectOne(new QueryWrapper<>(labelQuery));
    }

    // build key&value
    ConfigurationConfigKey configKey = new ConfigurationConfigKey();
    BeanUtils.copyProperties(request, configKey);
    Long keyId = request.getId();
    ConfigurationConfigValue configValue = new ConfigurationConfigValue();
    configValue.setConfigLabelId(label.getId());
    configValue.setConfigValue(request.getDefaultValue());

    // update
    if (!StringUtils.isEmpty(keyId)) {
      int updateKey = configKeyMapper.updateById(configKey);
      configValue.setConfigKeyId(configKey.getId());
      int updateValue = configValueMapper.updateByKeyId(configValue);
      return updateKey > 0 && updateValue > 0;
    }

    // 1.insert into ps_configuration_config_key
    int insertKey = configKeyMapper.insert(configKey);
    // 2.insert into ps_configuration_key_engine_relation
    ConfigurationKeyEngineRelation relation = new ConfigurationKeyEngineRelation();
    relation.setConfigKeyId(configKey.getId());
    relation.setEngineTypeLabelId(label.getId().longValue());
    int insertRelation = relationMapper.insert(relation);
    // 3.insert into ps_configuration_config_value
    configValue.setConfigKeyId(configKey.getId());
    int insertValue = configValueMapper.insert(configValue);

    return insertKey > 0 & insertRelation > 0 & insertValue > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean deleteConfigurationTemplate(Long keyId) {
    // 1.delete ps_configuration_config_value by keyId
    int deleteValue = configValueMapper.deleteByKeyId(keyId);

    // 2.delete ps_configuration_key_engine_relation by keyId
    int deleteRelation = relationMapper.deleteByKeyId(keyId);

    // 3.delete ps_configuration_config_key by id
    int deleteKey = configKeyMapper.deleteById(keyId);

    return deleteValue > 0 & deleteRelation > 0 & deleteKey > 0;
  }
}
