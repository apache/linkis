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

package org.apache.linkis.monitor.department.dao;

import org.apache.linkis.monitor.department.entity.UserDepartmentInfo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Mapper
public interface UserDepartmentInfoMapper {

  void insertUser(UserDepartmentInfo user);

  @Transactional(rollbackFor = Exception.class)
  int batchInsertUsers(@Param("userDepartmentInfos") List<UserDepartmentInfo> userDepartmentInfos);

  void updateUser(UserDepartmentInfo user);

  UserDepartmentInfo selectUser(@Param("userName") String userName);

  @Transactional(rollbackFor = Exception.class)
  void deleteUser();

  List<UserDepartmentInfo> selectAllUsers();
}
