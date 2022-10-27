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
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserIpConfigServiceImpl implements UserIpConfigService {

  private static final Logger logger = LoggerFactory.getLogger(UserIpConfigServiceImpl.class);

  @Autowired private UserIpMapper userIpMapper;

  /**
   * * createUserIP
   *
   * @param userIpVo
   * @return
   */
  @Override
  public void createUserIP(UserIpVo userIpVo) throws ConfigurationException {
    dataProcessing(userIpVo);
    UserIpVo userIpVoLowerCase = toLowerCase(userIpVo);
    userIpVoLowerCase.setCreateTime(new Date());
    userIpVoLowerCase.setUpdateTime(new Date());
    userIpMapper.createUserIP(userIpVoLowerCase);
  }

  /**
   * updateUserIP
   *
   * @param userIpVo
   */
  @Override
  public void updateUserIP(UserIpVo userIpVo) throws ConfigurationException {
    if (StringUtils.isBlank(userIpVo.getId())) {
      throw new ConfigurationException("id couldn't be empty ");
    }
    dataProcessing(userIpVo);
    UserIpVo userIpVoLowerCase = toLowerCase(userIpVo);
    userIpVoLowerCase.setUpdateTime(new Date());
    logger.info("updateUserIP : {}", userIpVoLowerCase);
    userIpMapper.updateUserIP(userIpVoLowerCase);
  }

  /**
   * deleteUserIP
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
   * Query IP Collection
   *
   * @return List<UserIpVo>
   * @param user
   * @param creator
   * @param pageNow
   * @param pageSize
   */
  @Override
  public Map<String, Object> queryUserIPList(
      String user, String creator, Integer pageNow, Integer pageSize) {
    Map<String, Object> result = new HashMap<>(2);
    if (StringUtils.isBlank(user)) user = null;
    if (StringUtils.isBlank(creator)) creator = null;
    if (null == pageNow) pageNow = 1;
    if (null == pageSize) pageSize = 20;
    List<UserIpVo> userIpVos = null;
    PageHelper.startPage(pageNow, pageSize);
    try {
      userIpVos = userIpMapper.queryUserIPList(user, creator);
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<UserIpVo> pageInfo = new PageInfo<>(userIpVos);
    result.put("userIpList", userIpVos);
    result.put(JobRequestConstants.TOTAL_PAGE(), pageInfo.getTotal());
    return result;
  }

  private void dataProcessing(UserIpVo userIpVo) throws ConfigurationException {
    // Parameter verification
    if (StringUtils.isBlank(userIpVo.getCreator())) {
      throw new ConfigurationException("creator couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getUser())) {
      throw new ConfigurationException("user couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getBussinessUser())) {
      throw new ConfigurationException("bussiness_user couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getIpList())) {
      throw new ConfigurationException("ipList couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getDesc())) {
      throw new ConfigurationException("desc couldn't be empty ");
    }
    // Ip rule verification
    String ipList = userIpVo.getIpList();
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
    boolean result = true;
    // Parameter verification
    if (StringUtils.isBlank(creator)) {
      throw new ConfigurationException("creator couldn't be empty ");
    }
    if (StringUtils.isBlank(user)) {
      throw new ConfigurationException("user couldn't be empty ");
    }
    if (creator.equals("*")) {
      throw new ConfigurationException("creator couldn't be '*' ");
    }
    Map<String, Object> resultMap =
        queryUserIPList(user.toLowerCase(), creator.toLowerCase(), null, null);
    Object userIpList = resultMap.getOrDefault(JobRequestConstants.TOTAL_PAGE(), 0);
    int total = Integer.parseInt(userIpList.toString());
    if (total == 0) result = false;
    return result;
  }

  @Override
  public UserIpVo queryUserIP(String user, String creator) {
    return userIpMapper.queryUserIP(user, creator);
  }

  private UserIpVo toLowerCase(UserIpVo userIpVo) {
    userIpVo.setCreator(userIpVo.getCreator().toLowerCase());
    userIpVo.setUser(userIpVo.getUser().toLowerCase());
    return userIpVo;
  }
}
