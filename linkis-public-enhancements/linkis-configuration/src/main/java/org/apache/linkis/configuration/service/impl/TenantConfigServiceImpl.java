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

import org.apache.linkis.configuration.dao.UserTenantMapper;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.configuration.util.HttpsUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TenantConfigServiceImpl implements TenantConfigService {

  private static final Logger logger = LoggerFactory.getLogger(TenantConfigServiceImpl.class);

  @Autowired private UserTenantMapper userTenantMapper;

  /**
   * * Querying the tenant configuration table
   *
   * @param user
   * @param creator
   * @param tenantValue
   * @return List<TenantVo>
   */
  @Override
  public List<TenantVo> queryTenantList(String user, String creator, String tenantValue) {
    if (StringUtils.isBlank(user)) user = null;
    if (StringUtils.isBlank(creator)) creator = null;
    if (StringUtils.isBlank(tenantValue)) tenantValue = null;
    return userTenantMapper.queryTenantList(user, creator, tenantValue);
  }

  /**
   * Delete tenant By ID
   *
   * @param id
   */
  @Override
  public void deleteTenant(Integer id) throws ConfigurationException {
    logger.info("deleteUserIP : id:{}", id);
    if (StringUtils.isBlank(id.toString())) {
      throw new ConfigurationException("id couldn't be empty ");
    }
    userTenantMapper.deleteTenant(id);
  }

  /**
   * Update tenant
   *
   * @param tenantVo
   */
  @Override
  public void updateTenant(TenantVo tenantVo) throws ConfigurationException {
    if (StringUtils.isBlank(tenantVo.getId())) {
      throw new ConfigurationException("id couldn't be empty ");
    }
    dataProcessing(tenantVo);
    tenantVo.setUpdateTime(new Date());
    logger.info("updateTenant : {}", tenantVo);
    userTenantMapper.updateTenant(tenantVo);
  }

  /**
   * Insert tenant
   *
   * @param tenantVo
   */
  @Override
  public void createTenant(TenantVo tenantVo) throws ConfigurationException {
    dataProcessing(tenantVo);
    tenantVo.setCreateTime(new Date());
    tenantVo.setUpdateTime(new Date());
    userTenantMapper.createTenant(tenantVo);
  }

  private void dataProcessing(TenantVo tenantVo) throws ConfigurationException {
    AtomicReference<Boolean> tenantResult = new AtomicReference<>(false);
    // Parameter verification
    if (StringUtils.isBlank(tenantVo.getCreator())) {
      throw new ConfigurationException("creator couldn't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getUser())) {
      throw new ConfigurationException("user couldn't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getBussinessUser())) {
      throw new ConfigurationException("bussiness_user couldn't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getDesc())) {
      throw new ConfigurationException("desc couldn't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getTenantValue())) {
      throw new ConfigurationException("tenant couldn't be empty ");
    }
    // Obtain the tenant information of the ECM list
    Map<String, Object> resultmap = null;
    try {
      resultmap = HttpsUtil.sendHttp(null, null);
      logger.info("ResourceMonitor  response  {}:", resultmap);
    } catch (IOException e) {
      logger.warn("failed to get ecmResource data");
    }
    Map<String, List<Map<String, Object>>> data = MapUtils.getMap(resultmap, "data");
    List<Map<String, Object>> emNodeVoList = data.get("EMs");
    emNodeVoList.forEach(
        ecmInfo -> {
          List<Map<String, Object>> labels = (List<Map<String, Object>>) ecmInfo.get("labels");
          labels.stream()
              .filter(labelmap -> labelmap.containsKey("tenant"))
              .forEach(
                  map -> {
                    String tenant = map.get("tenant").toString();
                    if (tenant.equals(tenantVo.getTenantValue())) {
                      tenantResult.set(true);
                    }
                  });
        });
    // Compare the value of ecm tenant
    if (!tenantResult.get())
      throw new ConfigurationException("The ECM with the corresponding label was not found");
    // The beginning of tenantValue needs to contain creator
    String creator = tenantVo.getCreator().toLowerCase();
    String tenantValue = tenantVo.getTenantValue().toLowerCase().split("_")[0];
    if (!creator.equals(tenantValue))
      throw new ConfigurationException("tenantValue should contain creator first");
  }

  @Override
  public Boolean checkUserCteator(String user, String creator, String tenantValue)
      throws ConfigurationException {
    // Parameter verification
    if (StringUtils.isBlank(creator)) {
      throw new ConfigurationException("creator couldn't be empty ");
    }
    if (StringUtils.isBlank(user)) {
      throw new ConfigurationException("user couldn't be empty ");
    }
    if (StringUtils.isBlank(tenantValue)) {
      throw new ConfigurationException("tenant couldn't be empty ");
    }
    return CollectionUtils.isNotEmpty(queryTenantList(user, creator, tenantValue));
  }
}
