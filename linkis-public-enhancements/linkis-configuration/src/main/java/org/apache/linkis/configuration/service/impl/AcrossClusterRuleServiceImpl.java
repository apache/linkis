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

import org.apache.linkis.configuration.dao.AcrossClusterRuleMapper;
import org.apache.linkis.configuration.entity.AcrossClusterRule;
import org.apache.linkis.configuration.service.AcrossClusterRuleService;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AcrossClusterRuleServiceImpl implements AcrossClusterRuleService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private AcrossClusterRuleMapper ruleMapper;

  @Override
  public void deleteAcrossClusterRule(String creator, String user) throws Exception {
    ruleMapper.deleteAcrossClusterRule(creator, user);
    logger.info("delete acrossClusterRule success");
    return;
  }

  @Override
  public void updateAcrossClusterRule(
      AcrossClusterRule newRule)
      throws Exception {
    AcrossClusterRule beforeRule = ruleMapper.getAcrossClusterRule(newRule.getId());
    if (beforeRule == null) {
      logger.info("acrossClusterRule not exit");
      throw new Exception("acrossClusterRule not exit");
    }

    Date time = new Date();
    newRule.setCreateBy(beforeRule.getCreateBy());
    newRule.setCreateTime(beforeRule.getCreateTime());
    newRule.setUpdateTime(time);

    ruleMapper.updateAcrossClusterRule(newRule);
    logger.info("update acrossClusterRule success");
    return;
  }

  @Override
  public void insertAcrossClusterRule(
          AcrossClusterRule acrossClusterRule)
      throws Exception {
    Date time = new Date();
    acrossClusterRule.setCreateTime(time);
    acrossClusterRule.setUpdateTime(time);
    ruleMapper.insertAcrossClusterRule(acrossClusterRule);
    logger.info("insert acrossClusterRule success");
    return;
  }

  @Override
  public Map<String, Object> queryAcrossClusterRuleList(
      String creator, String user, String clusterName, Integer pageNow, Integer pageSize) {
    Map<String, Object> result = new HashMap<>(2);
    List<AcrossClusterRule> acrossClusterRules = null;
    if (Objects.isNull(pageNow)) {
      pageNow = 1;
    }
    if (Objects.isNull(pageSize)) {
      pageSize = 20;
    }
    PageHelper.startPage(pageNow, pageSize);

    try {
      acrossClusterRules = ruleMapper.queryAcrossClusterRuleList(user, creator, clusterName);
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<AcrossClusterRule> pageInfo = new PageInfo<>(acrossClusterRules);
    result.put("acrossClusterRuleList", acrossClusterRules);
    result.put(JobRequestConstants.TOTAL_PAGE(), pageInfo.getTotal());
    return result;
  }

  @Override
  public void validAcrossClusterRule(Long id, String isValid, String updateBy) throws Exception{
    AcrossClusterRule acrossClusterRule = ruleMapper.getAcrossClusterRule(id);
    if (acrossClusterRule == null) {
      logger.info("acrossClusterRule not exit");
      throw new Exception("acrossClusterRule not exit");
    }
    acrossClusterRule.setIsValid(isValid);
    acrossClusterRule.setUpdateBy(updateBy);
    acrossClusterRule.setUpdateTime(new Date());
    logger.info("delete acrossClusterRule success");
    ruleMapper.validAcrossClusterRule(acrossClusterRule);
    logger.info("valid acrossClusterRule success");
    return;
  }


}
