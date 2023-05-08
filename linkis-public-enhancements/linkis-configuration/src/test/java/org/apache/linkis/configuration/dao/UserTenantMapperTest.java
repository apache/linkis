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

package org.apache.linkis.configuration.dao;

import org.apache.linkis.configuration.entity.TenantVo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTenantMapperTest extends BaseDaoTest {

  @Autowired UserTenantMapper userTenantMapper;

  TenantVo insert() {
    TenantVo tenantVo = new TenantVo();
    tenantVo.setUser("user");
    tenantVo.setCreateTime(new Date());
    tenantVo.setCreator("creator");
    tenantVo.setTenantValue("tenantValue");
    tenantVo.setUpdateTime(new Date());
    tenantVo.setBussinessUser("bussinessUser");
    tenantVo.setDesc("desc");
    userTenantMapper.createTenant(tenantVo);
    return tenantVo;
  }

  @Test
  void createTenant() {
    insert();
    List<TenantVo> tenantVos = userTenantMapper.queryTenantList("user", "creator", "tenantValue");
    assertTrue(tenantVos.size() > 0);
  }

  @Test
  void deleteTenant() {
    insert();
    TenantVo tenantVo = userTenantMapper.queryTenant("user", "creator");
    userTenantMapper.deleteTenant(Integer.valueOf(tenantVo.getId()));
    List<TenantVo> tenantVos = userTenantMapper.queryTenantList("user", "creator", "tenantValue");
    assertTrue(tenantVos.size() == 0);
  }

  @Test
  void updateTenant() {
    insert();
    TenantVo tenantVo = userTenantMapper.queryTenant("user", "creator");
    TenantVo updateTenantVo = new TenantVo();
    updateTenantVo.setId(tenantVo.getId());
    updateTenantVo.setDesc("desc2");
    updateTenantVo.setBussinessUser("bussinessUser2");
    userTenantMapper.updateTenant(updateTenantVo);
    TenantVo queryTenant = userTenantMapper.queryTenant("user", "creator");
    assertTrue(queryTenant.getDesc().equals("desc2"));
    assertTrue(queryTenant.getBussinessUser().equals("bussinessUser2"));
  }

  @Test
  void queryTenant() {
    insert();
    TenantVo tenantVo = userTenantMapper.queryTenant("user", "creator");
    assertTrue(tenantVo != null);
  }
}
