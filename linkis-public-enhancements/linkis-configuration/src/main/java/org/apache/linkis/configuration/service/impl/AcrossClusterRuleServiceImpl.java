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
import org.apache.linkis.governance.common.protocol.conf.AcrossClusterRequest;
import org.apache.linkis.governance.common.protocol.conf.AcrossClusterResponse;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.configuration.conf.AcrossClusterRuleKeys.KEY_CROSS_QUEUE;
import static org.apache.linkis.configuration.conf.AcrossClusterRuleKeys.KEY_QUEUE_RULE;

@Service
public class AcrossClusterRuleServiceImpl implements AcrossClusterRuleService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private AcrossClusterRuleMapper ruleMapper;

  @Override
  public void deleteAcrossClusterRule(Long id) throws Exception {
    AcrossClusterRule beforeRule = ruleMapper.getAcrossClusterRule(id, null);
    if (beforeRule == null) {
      throw new Exception("acrossClusterRule not exit");
    }

    ruleMapper.deleteAcrossClusterRule(id);
  }

  @Override
  public void deleteAcrossClusterRuleByBatch(List<Long> ids) throws Exception {
    ruleMapper.deleteAcrossClusterRuleByBatch(ids);
  }

  @Override
  public void deleteAcrossClusterRuleByUsername(String username) throws Exception {
    ruleMapper.deleteAcrossClusterRuleByUsername(username);
  }

  @Override
  public void deleteAcrossClusterRuleByCrossQueue(String crossQueue) throws Exception {
    ruleMapper.deleteAcrossClusterRuleByCrossQueue(crossQueue);
  }

  @Override
  public void updateAcrossClusterRule(AcrossClusterRule newRule) throws Exception {
    AcrossClusterRule beforeRule = ruleMapper.getAcrossClusterRule(newRule.getId(), null);
    if (beforeRule == null) {
      throw new Exception("acrossClusterRule not exit");
    }

    Date time = new Date();
    newRule.setCreateBy(beforeRule.getCreateBy());
    newRule.setCreateTime(beforeRule.getCreateTime());
    newRule.setUpdateTime(time);

    ruleMapper.updateAcrossClusterRule(newRule);
  }

  @Override
  public void updateAcrossClusterRuleByBatch(List<Long> ids, AcrossClusterRule newRule)
      throws Exception {
    Date time = new Date();
    newRule.setUpdateTime(time);

    ruleMapper.updateAcrossClusterRuleByBatch(ids, newRule);
  }

  @Override
  public void insertAcrossClusterRule(AcrossClusterRule acrossClusterRule) throws Exception {
    Date time = new Date();
    acrossClusterRule.setCreateTime(time);
    acrossClusterRule.setUpdateTime(time);
    ruleMapper.insertAcrossClusterRule(acrossClusterRule);
  }

  @Override
  public Map<String, Object> queryAcrossClusterRuleList(
      String creator, String username, String clusterName, Integer pageNow, Integer pageSize) {
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
      acrossClusterRules = ruleMapper.queryAcrossClusterRuleList(username, creator, clusterName);
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<AcrossClusterRule> pageInfo = new PageInfo<>(acrossClusterRules);
    result.put("acrossClusterRuleList", acrossClusterRules);
    result.put(JobRequestConstants.TOTAL_PAGE(), pageInfo.getTotal());
    return result;
  }

  @Override
  public void validAcrossClusterRule(Long id, String isValid, String username) throws Exception {
    AcrossClusterRule beforeRule = ruleMapper.getAcrossClusterRule(id, username);
    if (beforeRule == null) {
      throw new Exception("acrossClusterRule not exit");
    }

    ruleMapper.validAcrossClusterRule(isValid, id, username);
  }

  @Override
  public void validAcrossClusterRuleByBatch(List<Long> ids, String isValid) throws Exception {
    ruleMapper.validAcrossClusterRuleByBatch(ids, isValid);
  }

  @Receiver
  @Override
  public AcrossClusterResponse getAcrossClusterRuleByUsername(
      AcrossClusterRequest acrossClusterRequest, Sender sender) throws Exception {
    String username = acrossClusterRequest.username();
    AcrossClusterRule acrossClusterRule = ruleMapper.queryAcrossClusterRuleByUserName(username);
    if (acrossClusterRule == null) {
      return null;
    }
    String clusterName = acrossClusterRule.getClusterName();
    Map<String, Map<String, String>> rulesMap = new HashMap<>();
    try {
      Gson gson = BDPJettyServerHelper.gson();
      rulesMap = gson.fromJson(acrossClusterRule.getRules(), rulesMap.getClass());
      Map<String, String> queueRule = rulesMap.get(KEY_QUEUE_RULE);
      String crossQueueName = queueRule.get(KEY_CROSS_QUEUE);
      logger.info(
          "{} configure across cluster name is {}, queue name is {}",
          username,
          acrossClusterRule.getClusterName(),
          crossQueueName);
      return new AcrossClusterResponse(clusterName, crossQueueName);
    } catch (Exception e) {
      logger.warn("Failed to parse rulesMap from rules");
    }
    return null;
  }
}
