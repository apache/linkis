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

import org.apache.linkis.configuration.dao.DepartmentMapper;
import org.apache.linkis.configuration.dao.DepartmentTenantMapper;
import org.apache.linkis.configuration.dao.UserTenantMapper;
import org.apache.linkis.configuration.entity.DepartmentTenantVo;
import org.apache.linkis.configuration.entity.DepartmentVo;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.configuration.util.ClientUtil;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TenantConfigServiceImpl implements TenantConfigService {

  private static final Logger logger = LoggerFactory.getLogger(TenantConfigServiceImpl.class);

  @Autowired private UserTenantMapper userTenantMapper;

  @Autowired private DepartmentTenantMapper departmentTenantMapper;

  @Autowired private DepartmentMapper departmentMapper;

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
    logger.info("createTenant : {}", tenantVo);
    userTenantMapper.createTenant(tenantVo);
  }

  private void dataProcessing(TenantVo tenantVo) throws ConfigurationException {
    // If tenant is set to invalid, skip ecm check
    if (("N").equals(tenantVo.getIsValid())) {
      return;
    }
    AtomicReference<Boolean> tenantResult = new AtomicReference<>(false);
    // Obtain the tenant information of the ECM list
    Map<String, Object> ecmList = null;
    try {
      ecmList = ClientUtil.getEcmList();
      logger.info("Request ecm list  response  {}:", ecmList);
    } catch (IOException e) {
      logger.warn("failed to get ecmResource data", e);
    }
    Map<String, List<Map<String, Object>>> data = MapUtils.getMap(ecmList, "data");
    List<Map<String, Object>> emNodeVoList = data.get("EMs");
    // Compare ECM list tenant labels for task
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
    if (!("*").equals(tenantVo.getCreator())) {
      // The beginning of tenantValue needs to contain creator
      String creator = tenantVo.getCreator();
      String[] tenantArray = tenantVo.getTenantValue().split("_");
      if (tenantArray.length > 1 && !creator.equals(tenantArray[0])) {
        throw new ConfigurationException("tenantValue should contain creator first");
      }
    }
  }

  @Override
  public Boolean isExist(String user, String creator) {
    boolean result = true;
    Map<String, Object> resultMap = queryTenantList(user, creator, null, 1, 20);
    Object tenantList = resultMap.getOrDefault(JobRequestConstants.TOTAL_PAGE(), 0);
    int total = Integer.parseInt(tenantList.toString());
    if (total == 0) result = false;
    return result;
  }

  @Override
  public TenantVo queryTenant(String user, String creator) {
    return userTenantMapper.queryTenant(user, creator);
  }

  @Override
  public void saveDepartmentTenant(DepartmentTenantVo departmentTenantVo)
      throws ConfigurationException {
    TenantVo tenantVo = new TenantVo();
    BeanUtils.copyProperties(departmentTenantVo, tenantVo);
    dataProcessing(tenantVo);
    departmentTenantVo.setUpdateTime(new Date());
    if (StringUtils.isBlank(departmentTenantVo.getId())) {
      departmentTenantVo.setCreateTime(new Date());
      departmentTenantMapper.insertTenant(departmentTenantVo);
    } else {
      departmentTenantMapper.updateTenant(departmentTenantVo);
    }
  }

  /**
   * *
   *
   * @param departmentId
   * @param creator
   * @param tenantValue
   * @param pageNow
   * @param pageSize
   * @return
   */
  @Override
  public Map<String, Object> queryDepartmentTenant(
      String departmentId, String creator, String tenantValue, Integer pageNow, Integer pageSize) {
    Map<String, Object> result = new HashMap<>(2);
    List<DepartmentTenantVo> tenantVos = null;
    PageHelper.startPage(pageNow, pageSize);
    try {
      tenantVos = departmentTenantMapper.queryTenantList(creator, departmentId, tenantValue);
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<DepartmentTenantVo> pageInfo = new PageInfo<>(tenantVos);
    result.put("tenantList", tenantVos);
    result.put(JobRequestConstants.TOTAL_PAGE(), pageInfo.getTotal());
    return result;
  }

  public void deleteDepartmentTenant(Integer id) {
    departmentTenantMapper.deleteTenant(id);
  }

  @Override
  public DepartmentTenantVo queryDepartTenant(String creator, String departmentId) {
    return departmentTenantMapper.queryTenant(creator, departmentId);
  }

  @Override
  public List<DepartmentVo> queryDepartmentList() {
    return new ArrayList<>(
        departmentMapper.queryDepartmentList().stream()
            .collect(
                Collectors.toMap(
                    DepartmentVo::getOrgId,
                    department -> department,
                    (existing, replacement) -> existing))
            .values());
  }
}
