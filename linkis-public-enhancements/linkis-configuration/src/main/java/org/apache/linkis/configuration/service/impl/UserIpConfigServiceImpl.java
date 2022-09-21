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
package org.apache.linkis.configuration.service.impl;

import org.apache.linkis.configuration.dao.UserIpMapper;
import org.apache.linkis.configuration.entity.UserIpVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.UserIpConfigService;
import org.apache.linkis.configuration.util.CommonUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserIpConfigServiceImpl implements UserIpConfigService {

  private static final Logger logger = LoggerFactory.getLogger(UserIpConfigServiceImpl.class);

  @Autowired private UserIpMapper userIpMapper;

  /**
   * * 创建IP数据
   *
   * @param userIpVo
   * @return
   */
  @Override
  public void createUserIP(UserIpVo userIpVo) throws ConfigurationException {
    dataProcessing(userIpVo);
    userIpVo.setCreateTime(new Date());
    userIpVo.setUpdateTime(new Date());
    userIpMapper.createUserIP(userIpVo);
  }

  /**
   * 更新IP数据
   *
   * @param userIpVo
   */
  @Override
  public void updateUserIP(UserIpVo userIpVo) throws ConfigurationException {
    if (StringUtils.isBlank(userIpVo.getId())) {
      throw new ConfigurationException("id couldn't be empty ");
    }
    dataProcessing(userIpVo);
    userIpVo.setUpdateTime(new Date());
    logger.info("updateUserIP : {}", userIpVo);
    userIpMapper.updateUserIP(userIpVo);
  }

  /**
   * 删除IP
   *
   * @param id
   */
  @Override
  @Transactional(rollbackFor = Throwable.class)
  public void deleteUserIP(Integer id) throws ConfigurationException {
    logger.info("deleteUserIP : id:{}", id);
    if (StringUtils.isBlank(id.toString())) {
      throw new ConfigurationException("id couldn't be empty ");
    }
    userIpMapper.deleteUserIP(id);
  }

  /**
   * 查询IP集合
   *
   * @return List<UserIpVo>
   * @param user
   * @param creator
   */
  @Override
  public List<UserIpVo> queryUserIPList(String user, String creator) {
    if (StringUtils.isBlank(user)) user = null;
    if (StringUtils.isBlank(creator)) creator = null;
    return userIpMapper.queryUserIPList(user, creator);
  }

  private void dataProcessing(UserIpVo userIpVo) throws ConfigurationException {
    // 参数校验
    if (StringUtils.isBlank(userIpVo.getCreator())) {
      throw new ConfigurationException("creator couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getUser())) {
      throw new ConfigurationException("user couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getBussinessUser())) {
      throw new ConfigurationException("bussiness_user couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getIps())) {
      throw new ConfigurationException("ipList couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getDesc())) {
      throw new ConfigurationException("desc couldn't be empty ");
    }
    // ip规则校验
    String ipList = userIpVo.getIps();
    if (!ipList.equals("*")) {
      String[] split = ipList.split(",");
      StringJoiner joiner = new StringJoiner(",");
      Arrays.stream(split)
          .distinct()
          .filter(ipStr -> !CommonUtils.ipCheck(ipStr))
          .forEach(joiner::add);
      if (StringUtils.isNotBlank(joiner.toString())) {
        throw new ConfigurationException(joiner + ",Illegal IP address ");
      }
    }
  }

  @Override
  public Boolean checkUserCteator(String user, String creator) throws ConfigurationException {
    // 参数校验
    if (StringUtils.isBlank(creator)) {
      throw new ConfigurationException("creator couldn't be empty ");
    }
    if (StringUtils.isBlank(user)) {
      throw new ConfigurationException("user couldn't be empty ");
    }
    return CollectionUtils.isNotEmpty(queryUserIPList(user, creator));
  }
}
