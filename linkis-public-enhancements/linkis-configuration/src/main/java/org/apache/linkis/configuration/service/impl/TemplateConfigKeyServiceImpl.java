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

import org.apache.linkis.configuration.dao.ConfigKeyLimitForUserMapper;
import org.apache.linkis.configuration.dao.ConfigMapper;
import org.apache.linkis.configuration.dao.LabelMapper;
import org.apache.linkis.configuration.dao.TemplateConfigKeyMapper;
import org.apache.linkis.configuration.entity.*;
import org.apache.linkis.configuration.enumeration.BoundaryTypeEnum;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.ConfigKeyService;
import org.apache.linkis.configuration.service.ConfigurationService;
import org.apache.linkis.configuration.service.TemplateConfigKeyService;
import org.apache.linkis.configuration.util.LabelEntityParser;
import org.apache.linkis.configuration.validate.ValidatorManager;
import org.apache.linkis.governance.common.entity.TemplateConfKey;
import org.apache.linkis.governance.common.protocol.conf.TemplateConfRequest;
import org.apache.linkis.governance.common.protocol.conf.TemplateConfResponse;
import org.apache.linkis.manager.label.entity.CombinedLabel;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TemplateConfigKeyServiceImpl implements TemplateConfigKeyService {

  private static final Logger logger = LoggerFactory.getLogger(TemplateConfigKeyServiceImpl.class);

  @Autowired private ConfigMapper configMapper;

  @Autowired private LabelMapper labelMapper;

  @Autowired private TemplateConfigKeyMapper templateConfigKeyMapper;

  @Autowired private ConfigurationService configurationService;

  @Autowired private ConfigKeyService configKeyService;

  @Autowired private ValidatorManager validatorManager;

  @Autowired private ConfigKeyLimitForUserMapper configKeyLimitForUserMapper;

  @Autowired private PlatformTransactionManager platformTransactionManager;

  @Override
  @Transactional
  public Boolean updateKeyMapping(
      String templateUid,
      String templateName,
      String engineType,
      String operator,
      List<ConfigKeyLimitVo> itemList,
      Boolean isFullMode)
      throws ConfigurationException {

    // Query the corresponding data and check the validity of the data(查询对应的数据 并做数据合法性检查)
    List<String> keyList = itemList.stream().map(e -> e.getKey()).collect(Collectors.toList());
    List<ConfigKey> configKeyList =
        configMapper.selectKeyByEngineTypeAndKeyList(engineType, keyList);
    // List of key ids to be updated(待更新的key id 列表)
    List<Long> keyIdList = configKeyList.stream().map(e -> e.getId()).collect(Collectors.toList());
    if (configKeyList.size() != itemList.size()) {
      List<String> dbKeyList =
          configKeyList.stream().map(e -> e.getKey()).collect(Collectors.toList());
      String msg =
          MessageFormat.format(
              "The num of configuration item data from the DB is inconsistent with input(从DB中获取到的配置数据条数不一致) :"
                  + "engineType:{0}, input keys:{1}, db keys:{2}",
              engineType, String.join(",", keyList), String.join(",", dbKeyList));
      throw new ConfigurationException(msg);
    }

    List<TemplateConfigKey> toUpdateOrInsertList = new ArrayList<>();

    // map k:v---> key：ConfigKey
    Map<String, ConfigKey> configKeyMap =
        configKeyList.stream().collect(Collectors.toMap(ConfigKey::getKey, item -> item));
    for (ConfigKeyLimitVo item : itemList) {

      String key = item.getKey();
      ConfigKey temp = configKeyMap.get(item.getKey());
      String validateType = temp.getValidateType();
      String validateRange = temp.getValidateRange();
      String configValue = item.getConfigValue();
      String maxValue = item.getMaxValue();

      if (StringUtils.isNotEmpty(configValue)
          && !validatorManager
              .getOrCreateValidator(validateType)
              .validate(configValue, validateRange)) {
        String msg =
            MessageFormat.format(
                "Parameter configValue verification failed(参数configValue校验失败):"
                    + "key:{0}, ValidateType:{1}, ValidateRange:{2},ConfigValue:{3}",
                key, validateType, validateRange, configValue);
        throw new ConfigurationException(msg);
      }

      if (StringUtils.isNotEmpty(maxValue)
          && BoundaryTypeEnum.WITH_BOTH.getId().equals(temp.getBoundaryType())) {
        if (!validatorManager
            .getOrCreateValidator(validateType)
            .validate(maxValue, validateRange)) {
          String msg =
              MessageFormat.format(
                  "Parameter maxValue verification failed(参数maxValue校验失败):"
                      + "key:{0}, ValidateType:{1}, ValidateRange:{2}, maxValue:{3}",
                  key, validateType, validateRange, maxValue);
          throw new ConfigurationException(msg);
        }

        try {
          Integer maxVal = Integer.valueOf(maxValue.replaceAll("[^0-9]", ""));
          Integer configVal = Integer.valueOf(configValue.replaceAll("[^0-9]", ""));
          if (configVal > maxVal) {
            String msg =
                MessageFormat.format(
                    "Parameter key:{0},config value:{1} verification failed, "
                        + "exceeds the specified max value: {2}:(参数校验失败，超过指定的最大值):",
                    key, configVal, maxVal);
            throw new ConfigurationException(msg);
          }
        } catch (Exception exception) {
          if (exception instanceof ConfigurationException) {
            throw exception;
          } else {
            logger.warn(
                "Failed to check special limit setting for key:"
                    + key
                    + ",config value:"
                    + configValue);
          }
        }
      }
      ;

      Long keyId = temp.getId();

      TemplateConfigKey templateConfigKey = new TemplateConfigKey();
      templateConfigKey.setTemplateName(templateName);
      templateConfigKey.setTemplateUuid(templateUid);
      templateConfigKey.setKeyId(keyId);
      templateConfigKey.setConfigValue(configValue);
      templateConfigKey.setMaxValue(maxValue);
      templateConfigKey.setCreateBy(operator);
      templateConfigKey.setUpdateBy(operator);
      toUpdateOrInsertList.add(templateConfigKey);
    }
    // Update data according to different mode
    if (isFullMode) {
      // The data previously in the database needs to be removed
      List<TemplateConfigKey> oldList =
          templateConfigKeyMapper.selectListByTemplateUuid(templateUid);
      List<Long> needToRemoveList =
          oldList.stream()
              .filter(
                  item -> {
                    return !keyIdList.contains(item.getKeyId());
                  })
              .map(e -> e.getKeyId())
              .collect(Collectors.toList());
      if (needToRemoveList.size() > 0) {
        logger.info(
            "Try to remove old data:[" + needToRemoveList + "] for templateUid:" + templateUid);
        templateConfigKeyMapper.deleteByTemplateUuidAndKeyIdList(templateUid, needToRemoveList);
      }
    }

    if (toUpdateOrInsertList.size() == 0) {
      String msg = "No key data to update, Please check if the keys are correct";
      throw new ConfigurationException(msg);
    }
    templateConfigKeyMapper.batchInsertOrUpdateList(toUpdateOrInsertList);

    return true;
  }

  @Override
  public List<Object> queryKeyInfoList(List<String> uuidList) throws ConfigurationException {
    List<Object> result = new ArrayList<>();

    List<TemplateConfigKey> templateConfigKeyList =
        templateConfigKeyMapper.selectListByTemplateUuidList(uuidList);

    Map<String, List<TemplateConfigKey>> templateConfigKeyListGroupByUuid =
        templateConfigKeyList.stream()
            .collect(Collectors.groupingBy(TemplateConfigKey::getTemplateUuid));

    List<Long> keyIdList =
        templateConfigKeyList.stream()
            .map(e -> e.getKeyId())
            .distinct()
            .collect(Collectors.toList());

    if (keyIdList.size() == 0) {
      String msg = "can not get any config key info from db, Please check if the keys are correct";
      throw new ConfigurationException(msg);
    }
    List<ConfigKey> configKeyList = configMapper.selectKeyByKeyIdList(keyIdList);
    // map k:v---> keyId：ConfigKey
    Map<Long, ConfigKey> configKeyMap =
        configKeyList.stream().collect(Collectors.toMap(ConfigKey::getId, item -> item));

    for (String uuid : templateConfigKeyListGroupByUuid.keySet()) {
      Map item = new HashMap();
      List<Object> keys = new ArrayList<>();
      item.put("templateUid", uuid);

      String engineType = "";
      List<String> engineTypeList = templateConfigKeyMapper.selectEngineTypeByTemplateUuid(uuid);

      if (engineTypeList.size() > 1) {
        String msg =
            MessageFormat.format(
                "template uuid:{0} associated with the engine type:{1} more than one! Please check if the keys are correct",
                uuid, StringUtils.join(engineTypeList.toArray(), ","));
        throw new ConfigurationException(msg);
      }

      if (engineTypeList.size() == 0) {
        String msg =
            MessageFormat.format(
                "template uuid:{0} can not associated with any engine type! Please check if the keys are correct",
                uuid);
        throw new ConfigurationException(msg);
      }

      engineType = engineTypeList.get(0);

      Map<Long, TemplateConfigKey> templateConfigKeyMap =
          templateConfigKeyListGroupByUuid.get(uuid).stream()
              .collect(Collectors.toMap(TemplateConfigKey::getKeyId, elemt -> elemt));

      List<ConfigKey> ecKeyList = configKeyService.getConfigKeyList(engineType);
      for (ConfigKey configKey : ecKeyList) {
        Map<String, Object> temp = new HashMap<>();
        temp.put("key", configKey.getKey());
        temp.put("name", configKey.getName());
        temp.put("description", configKey.getDescription());
        temp.put("engineType", configKey.getEngineType());
        temp.put("validateType", configKey.getValidateType());
        temp.put("validateRange", configKey.getValidateRange());
        temp.put("boundaryType", configKey.getBoundaryType());
        temp.put("defaultValue", configKey.getDefaultValue());
        temp.put("require", configKey.getTemplateRequired());
        temp.put("keyId", configKey.getId());

        Long keyId = configKey.getId();
        TemplateConfigKey templateConfigKey = templateConfigKeyMap.get(keyId);

        if (templateConfigKey == null) {
          temp.put("configValue", null);
          temp.put("maxValue", null);
          temp.put("createBy", null);
          temp.put("createTime", null);
          temp.put("updateBy", null);
          temp.put("updateTime", null);
        } else {
          temp.put("configValue", templateConfigKey.getConfigValue());
          temp.put("maxValue", templateConfigKey.getMaxValue());
          temp.put("createBy", templateConfigKey.getCreateBy());
          temp.put("createTime", templateConfigKey.getCreateTime());
          temp.put("updateBy", templateConfigKey.getUpdateBy());
          temp.put("updateTime", templateConfigKey.getUpdateTime());
        }

        keys.add(temp);
      }

      item.put("itemList", keys);
      result.add(item);
    }
    return result;
  }

  @Override
  public Map<String, Object> apply(
      String templateUid,
      String application,
      String engineType,
      String engineVersion,
      String operator,
      List<String> userList)
      throws ConfigurationException {
    List<Object> successList = new ArrayList<>();
    List<Object> errorList = new ArrayList<>();

    // get the associated config itsm list
    List<String> templateUuidList = new ArrayList<>();
    templateUuidList.add(templateUid);
    List<TemplateConfigKey> templateConfigKeyList =
        templateConfigKeyMapper.selectListByTemplateUuidList(templateUuidList);
    if (templateConfigKeyList.size() == 0) {
      String msg =
          MessageFormat.format(
              "The template configuration is empty. Please check the template associated configuration information in the database table"
                  + "(模板关联的配置为空,请检查数据库表中关于模板id：{0} 关联配置项是否完整)",
              templateUid);
      throw new ConfigurationException(msg);
    }
    // check input engineType is same as template key engineType
    List<Long> keyIdList =
        templateConfigKeyList.stream()
            .map(e -> e.getKeyId())
            .distinct()
            .collect(Collectors.toList());

    if (keyIdList.size() == 0) {
      String msg = "can not get any config key info from db, Please check if the keys are correct";
      throw new ConfigurationException(msg);
    }
    List<ConfigKey> configKeyList = configMapper.selectKeyByKeyIdList(keyIdList);
    // map k:v---> keyId：ConfigKey
    Set<String> configKeyEngineTypeSet =
        configKeyList.stream().map(ConfigKey::getEngineType).collect(Collectors.toSet());

    if (configKeyEngineTypeSet == null || configKeyEngineTypeSet.size() == 0) {
      String msg =
          MessageFormat.format(
              "Unable to get configuration parameter information associated with template id:{0}, please check whether the parameters are correct"
                  + "(无法获取模板:{0} 关联的配置参数信息,请检查参数是否正确)",
              templateUid);
      throw new ConfigurationException(msg);
    }

    if (configKeyEngineTypeSet.size() != 1 || !configKeyEngineTypeSet.contains(engineType)) {
      String msg =
          MessageFormat.format(
              "The engineType:{0} associated with the template:{1} does not match the input engineType:{2}, please check whether the parameters are correct"
                  + "(模板关联的引擎类型：{0} 和下发的引擎类型：{2} 不匹配,请检查参数是否正确)",
              String.join(",", configKeyEngineTypeSet), templateUid, engineType);
      throw new ConfigurationException(msg);
    }
    for (String user : userList) {
      // try to create combined_userCreator_engineType label for user
      Map res = new HashMap();
      res.put("user", user);
      try {
        CombinedLabel combinedLabel =
            configurationService.generateCombinedLabel(
                engineType, engineVersion, user, application);
        String conbinedLabelKey = combinedLabel.getLabelKey();
        String conbinedLabelStringValue = combinedLabel.getStringValue();
        // check lable is ok

        ConfigLabel configLabel =
            labelMapper.getLabelByKeyValue(conbinedLabelKey, conbinedLabelStringValue);
        if (null == configLabel || configLabel.getId() < 0) {
          configLabel = LabelEntityParser.parseToConfigLabel(combinedLabel);
          labelMapper.insertLabel(configLabel);
          logger.info("succeed to create label: {}", configLabel.getStringValue());
        }

        // batch update config value
        List<ConfigValue> configValues = new ArrayList<>();

        List<ConfigKeyLimitForUser> configKeyLimitForUsers = new ArrayList<>();

        for (TemplateConfigKey templateConfigKey : templateConfigKeyList) {
          Long keyId = templateConfigKey.getKeyId();
          String uuid = templateConfigKey.getTemplateUuid();
          String confVal = templateConfigKey.getConfigValue();
          String maxVal = templateConfigKey.getMaxValue();

          ConfigValue configValue = new ConfigValue();
          configValue.setConfigKeyId(keyId);
          configValue.setConfigValue(confVal);
          configValue.setConfigLabelId(configLabel.getId());
          configValues.add(configValue);

          ConfigKeyLimitForUser configKeyLimitForUser = new ConfigKeyLimitForUser();
          configKeyLimitForUser.setUserName(user);
          configKeyLimitForUser.setCombinedLabelValue(configLabel.getStringValue());
          configKeyLimitForUser.setKeyId(keyId);
          configKeyLimitForUser.setConfigValue(confVal);
          configKeyLimitForUser.setMaxValue(maxVal);
          configKeyLimitForUser.setLatestUpdateTemplateUuid(uuid);
          configKeyLimitForUser.setCreateBy(operator);
          configKeyLimitForUser.setUpdateBy(operator);
          configKeyLimitForUsers.add(configKeyLimitForUser);
        }

        if (configValues.size() == 0) {
          res.put("msg", "can not get any right key form the db");
          errorList.add(res);
        } else {

          DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
          TransactionStatus status =
              platformTransactionManager.getTransaction(transactionDefinition);
          try {
            configMapper.batchInsertOrUpdateValueList(configValues);
            // batch update user ConfigKeyLimitForUserMapper
            configKeyLimitForUserMapper.batchInsertOrUpdateList(configKeyLimitForUsers);

            platformTransactionManager.commit(status); // commit transaction if everything's fine
          } catch (Exception ex) {
            platformTransactionManager.rollback(
                status); // rollback transaction if any error occurred
            throw ex;
          }
          successList.add(res);
        }

      } catch (Exception e) {
        logger.warn("try to update configurations for  user:" + user + " with error", e);
        res.put("msg", e.getMessage());
        errorList.add(res);
      }
    }

    Map<String, Object> result = new HashMap<>();

    Map<String, Object> successResult = new HashMap<>();
    Map<String, Object> errorResult = new HashMap<>();

    successResult.put("num", successList.size());
    successResult.put("infoList", successList);

    errorResult.put("num", errorList.size());
    errorResult.put("infoList", errorList);

    result.put("success", successResult);
    result.put("error", errorResult);
    return result;
  }

  @Receiver
  @Override
  public TemplateConfResponse queryKeyInfoList(TemplateConfRequest templateConfRequest) {
    TemplateConfResponse result = new TemplateConfResponse();
    String templateUid = templateConfRequest.getTemplateUuid();
    String templateName = templateConfRequest.getTemplateName();
    if (logger.isDebugEnabled()) {
      logger.debug("query conf list with uid:{},name:{}", templateUid, templateName);
    }
    if (StringUtils.isBlank(templateUid) && StringUtils.isBlank(templateName)) {
      return result;
    }

    List<TemplateConfigKeyVO> voList = new ArrayList<>();

    if (StringUtils.isNotBlank(templateUid)) {
      voList = templateConfigKeyMapper.selectInfoListByTemplateUuid(templateUid);

    } else {
      voList = templateConfigKeyMapper.selectInfoListByTemplateName(templateName);
    }
    List<TemplateConfKey> data = new ArrayList<>();
    if (voList != null) {
      for (TemplateConfigKeyVO temp : voList) {
        TemplateConfKey item = new TemplateConfKey();
        item.setTemplateUuid(temp.getTemplateUuid());
        item.setKey(temp.getKey());
        item.setTemplateName(temp.getTemplateName());
        item.setConfigValue(temp.getConfigValue());
        data.add(item);
        if (logger.isDebugEnabled()) {
          logger.debug("query conf item={}", item);
        }
      }
    }
    result.setList(data);
    return result;
  }
}
