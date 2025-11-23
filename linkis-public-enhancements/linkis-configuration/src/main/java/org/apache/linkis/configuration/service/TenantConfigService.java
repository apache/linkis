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

package org.apache.linkis.configuration.service;

import org.apache.linkis.configuration.entity.DepartmentTenantVo;
import org.apache.linkis.configuration.entity.DepartmentVo;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;

import java.util.List;
import java.util.Map;

public interface TenantConfigService {

  Map<String, Object> queryTenantList(
      String user, String creator, String tenantValue, Integer pageNow, Integer pageSize);

  void deleteTenant(Integer id) throws ConfigurationException;

  void updateTenant(TenantVo tenantVo) throws ConfigurationException;

  void createTenant(TenantVo tenantVo) throws ConfigurationException;

  Boolean isExist(String user, String creator) throws ConfigurationException;

  TenantVo queryTenant(String user, String creator);

  void saveDepartmentTenant(DepartmentTenantVo departmentTenantVo) throws ConfigurationException;

  Map<String, Object> queryDepartmentTenant(
      String departmentId,
      String department,
      String creator,
      String tenantValue,
      Integer pageNow,
      Integer pageSize);

  void deleteDepartmentTenant(Integer id);

  DepartmentTenantVo queryDepartTenant(String creator, String departmentId, String department);

  List<DepartmentVo> queryDepartmentList();

  DepartmentVo getDepartmentByUser(String user);
}
