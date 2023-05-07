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

import org.apache.linkis.configuration.dao.UserTenantMapper;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.configuration.util.HttpsUtil;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
   * @param pageNow
   * @param pageSize
   * @return List<TenantVo>
   */
  @Override
  public Map<String, Object> queryTenantList(
      String user, String creator, String tenantValue, Integer pageNow, Integer pageSize) {
    Map<String, Object> result = new HashMap<>(2);
    List<TenantVo> tenantVos = null;
    if (Objects.isNull(pageNow)) {
      pageNow = 1;
    }
    if (Objects.isNull(pageSize)) {
      pageSize = 20;
    }
    PageHelper.startPage(pageNow, pageSize);
    try {
      tenantVos = userTenantMapper.queryTenantList(user, creator, tenantValue);
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<TenantVo> pageInfo = new PageInfo<>(tenantVos);
    result.put("tenantList", tenantVos);
    result.put(JobRequestConstants.TOTAL_PAGE(), pageInfo.getTotal());
    return result;
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
      throw new ConfigurationException("id can't be empty ");
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
      throw new ConfigurationException("id can't be empty ");
    }
    dataProcessing(tenantVo);
    TenantVo tenantVoLowerCase = toLowerCase(tenantVo);
    logger.info("updateTenant : {}", tenantVoLowerCase);
    userTenantMapper.updateTenant(tenantVoLowerCase);
  }

  /**
   * Insert tenant
   *
   * @param tenantVo
   */
  @Override
  public void createTenant(TenantVo tenantVo) throws ConfigurationException {
    dataProcessing(tenantVo);
    TenantVo tenantVoLowerCase = toLowerCase(tenantVo);
    tenantVoLowerCase.setCreateTime(new Date());
    logger.info("createTenant : {}", tenantVoLowerCase);
    userTenantMapper.createTenant(tenantVo);
  }

  private void dataProcessing(TenantVo tenantVo) throws ConfigurationException {
    if (!tenantVo.getCreator().equals("*")) {
      AtomicReference<Boolean> tenantResult = new AtomicReference<>(false);
      // Obtain the tenant information of the ECM list
      Map<String, Object> ecmListResult = null;
      try {
        ecmListResult = HttpsUtil.sendHttp(null, null);
        logger.info("Request ecm list  response  {}:", ecmListResult);
      } catch (IOException e) {
        logger.warn("failed to get ecmResource data");
      }
      Map<String, List<Map<String, Object>>> data = MapUtils.getMap(ecmListResult, "data");
      List<Map<String, Object>> emNodeVoList = data.get("EMs");
      // Compare ECM list tenant labels for task
      emNodeVoList.forEach(
          ecmInfo -> {
            List<Map<String, Object>> labels = (List<Map<String, Object>>) ecmInfo.get("labels");
            labels.stream()
                .filter(labelmap -> labelmap.containsKey("tenant"))
                .forEach(
                    map -> {
                      String tenant = map.get("tenant").toString().toLowerCase();
                      if (tenant.equals(tenantVo.getTenantValue().toLowerCase())) {
                        tenantResult.set(true);
                      }
                    });
          });
      // Compare the value of ecm tenant
      if (!tenantResult.get())
        throw new ConfigurationException("The ECM with the corresponding label was not found");
      // The beginning of tenantValue needs to contain creator
      String creator = tenantVo.getCreator().toLowerCase();
      String[] tenantArray = tenantVo.getTenantValue().toLowerCase().split("_");
      if (tenantArray.length > 1 && !creator.equals(tenantArray[0])) {
        throw new ConfigurationException("tenantValue should contain creator first");
      }
    }
  }

  @Override
  public Boolean isExist(String user, String creator) {
    boolean result = true;
    Map<String, Object> resultMap =
        queryTenantList(user.toLowerCase(), creator.toLowerCase(), null, 1, 20);
    Object tenantList = resultMap.getOrDefault(JobRequestConstants.TOTAL_PAGE(), 0);
    int total = Integer.parseInt(tenantList.toString());
    if (total == 0) result = false;
    return result;
  }

  @Override
  public TenantVo queryTenant(String user, String creator) {
    return userTenantMapper.queryTenant(user, creator);
  }

  public TenantVo toLowerCase(TenantVo tenantVo) {
    tenantVo.setTenantValue(tenantVo.getTenantValue().toLowerCase());
    tenantVo.setCreator(tenantVo.getCreator().toLowerCase());
    tenantVo.setUser(tenantVo.getUser().toLowerCase());
    tenantVo.setUpdateTime(new Date());
    return tenantVo;
  }
}
