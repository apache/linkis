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

import org.apache.linkis.configuration.dao.TenantMapper;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.configuration.util.HttpsUtil;
import org.apache.linkis.server.Message;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TenantConfigServiceImpl implements TenantConfigService {

  private static final Logger logger = LoggerFactory.getLogger(TenantConfigServiceImpl.class);

  @Autowired private TenantMapper tenantMapper;

  /**
   * * 查询tenant配置表
   *
   * @return List<TenantVo>
   * @param user
   * @param creator
   * @param tenant
   */
  @Override
  public List<TenantVo> queryTenantList(String user, String creator, String tenant) {
    if (StringUtils.isBlank(user)) user = null;
    if (StringUtils.isBlank(creator)) creator = null;
    if (StringUtils.isBlank(tenant)) tenant = null;
    return tenantMapper.queryTenantList(user, creator, tenant);
  }

  /**
   * 根据id删除tenant
   *
   * @param id
   */
  @Override
  public void deleteTenant(Integer id) {
    tenantMapper.deleteTenant(id);
  }

  /**
   * * 更新tenant
   *
   * @param tenantVo
   */
  @Override
  public Message updateTenant(TenantVo tenantVo) {
    try {
      dataProcessing(tenantVo);
      tenantVo.setUpdateTime(new Date());
      tenantMapper.updateTenant(tenantVo);
    } catch (ConfigurationException e) {
      return Message.error(e.getMessage());
    }
    return Message.ok();
  }

  /**
   * * 新增tenant
   *
   * @param tenantVo
   */
  @Override
  public Message createTenant(TenantVo tenantVo) {
    try {
      if (StringUtils.isBlank(tenantVo.getId())) {
        throw new ConfigurationException("id couldn't be empty ");
      }
      dataProcessing(tenantVo);
      tenantVo.setCreateTime(new Date());
      tenantVo.setUpdateTime(new Date());
      tenantMapper.createTenant(tenantVo);
    } catch (DuplicateKeyException e) {
      return Message.error("create user-creator is existed");
    } catch (ConfigurationException e) {
      return Message.error(e.getMessage());
    }
    return Message.ok();
  }

  private void dataProcessing(TenantVo tenantVo) throws ConfigurationException {
    AtomicReference<Boolean> tenantResult = new AtomicReference<>(false);
    // 参数校验
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
    // 获取ECM列表的租户信息
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
    // 对比ecm tenant的值
    if (!tenantResult.get())
      throw new ConfigurationException("The ECM with the corresponding label was not found");
    // tenantValue  开头需要包含  creator
    String creator = tenantVo.getCreator().toLowerCase();
    String tenantValue = tenantVo.getTenantValue().toLowerCase().split("_")[0];
    if (!creator.equals(tenantValue))
      throw new ConfigurationException("tenantValue should contain creator first");
  }
}
