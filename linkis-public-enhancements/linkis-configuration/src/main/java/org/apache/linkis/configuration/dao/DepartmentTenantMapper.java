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

import org.apache.linkis.configuration.entity.DepartmentTenantVo;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DepartmentTenantMapper {

  void insertTenant(DepartmentTenantVo departmentTenantVo);

  void updateTenant(DepartmentTenantVo departmentTenantVo);

  List<DepartmentTenantVo> queryTenantList(
      @Param("creator") String creator,
      @Param("departmentId") String departmentId,
      @Param("tenantValue") String tenantValue);

  void deleteTenant(@Param("id") Integer id);

  DepartmentTenantVo queryTenant(
      @Param("creator") String creator, @Param("departmentId") String departmentId);
}
