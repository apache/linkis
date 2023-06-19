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
import org.apache.linkis.configuration.dao.TemplateConfigKeyMapper;
import org.apache.linkis.configuration.entity.ConfigKey;
import org.apache.linkis.configuration.entity.ConfigKeyValue;
import org.apache.linkis.configuration.entity.TemplateConfigKey;
import org.apache.linkis.configuration.entity.TemplateConfigKeyVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.ConfigurationService;
import org.apache.linkis.configuration.service.TemplateConfigKeyService;
import org.apache.linkis.configuration.validate.ValidatorManager;
import org.apache.linkis.manager.common.entity.persistence.PersistencerEcNodeInfo;
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TemplateConfigKeyServiceImpl implements TemplateConfigKeyService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateConfigKeyServiceImpl.class);

    @Autowired
    private ConfigMapper configMapper;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private TemplateConfigKeyMapper templateConfigKeyMapper;


    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ValidatorManager validatorManager;


    private CombinedLabelBuilder combinedLabelBuilder = new CombinedLabelBuilder();


  @Override
  public Boolean updateKeyMapping(String templateUid, String templateName, String engineType,
                                  String operator, Boolean isFullMode, List<TemplateConfigKeyVo> itemList) throws ConfigurationException {
    //isFullMode true
    //查询对应的数据 并做数据合法性检查
    List<String> keyList=itemList.stream().map(e->e.getKey()).collect(Collectors.toList());
    List<ConfigKey> configKeyList=configMapper.selectKeyByEngineTypeAndKeyList(engineType,keyList);
    //待更新的key id 列表
    List<Long> keyIdList=configKeyList.stream().map(e->e.getId()).collect(Collectors.toList());
    if(configKeyList.size()!=itemList.size())
    {

    }

      //组装更新
      List<TemplateConfigKey> toUpdateOrInsertList=new ArrayList<>();

      // map k:v---> key：ConfigKey
      Map<String, ConfigKey> configKeyMap =
              configKeyList.stream()
                      .collect(Collectors.toMap(ConfigKey::getKey, item -> item));
      for (TemplateConfigKeyVo item : itemList) {

          String key=item.getKey();
          ConfigKey temp = configKeyMap.get(item.getKey());
          String validateType=temp.getValidateType();
          String validateRange=temp.getValidateRange();
          String configValue=item.getConfigValue();
          String maxValue=item.getMaxValue();

          if (!validatorManager
                  .getOrCreateValidator(validateType)
                  .validate(configValue, validateRange)
          ) {
              String msg= MessageFormat.format("Parameter configValue verification failed(参数configValue校验失败):" +
                      "key:{0}, ValidateType:{1}, ValidateRange:{},ConfigValue:{}",
                      key,validateType,validateRange,configValue);
              throw new ConfigurationException(msg);
          }
          if (!validatorManager
                  .getOrCreateValidator(validateType)
                  .validate(maxValue, validateRange)
          ) {
              String msg= MessageFormat.format("Parameter maxValue verification failed(参数maxValue校验失败):" +
                              "key:{0}, ValidateType:{1}, ValidateRange:{},ConfigValue:{}",
                      key,validateType,validateRange,maxValue);
              throw new ConfigurationException(msg);
          }

          TemplateConfigKey templateConfigKey=new TemplateConfigKey();


      }
      //根据不同模式更新数据
      if(isFullMode)
      {
          //之前在数据库中的数据 需要移除的
          List<TemplateConfigKey> oldList =templateConfigKeyMapper.selectListByTemplateUuid(templateUid);
          List<Long> needToRemoveList=oldList.stream().filter(item ->{
              return !keyIdList.contains(item.getKeyId());
          }).map(e->e.getKeyId()).collect(Collectors.toList());
          logger.info("Try to remove old data:["+needToRemoveList+"] for templateUid:"+templateUid);
          templateConfigKeyMapper.deleteByTemplateUuidAndKeyIdList(templateUid,needToRemoveList);
      }


      itemList.forEach();
      templateConfigKeyMapper.batchInsertList(templateUid,needToRemoveList);

  }
}
