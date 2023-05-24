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

import org.apache.linkis.configuration.entity.UserIpVo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserIpMapperTest extends BaseDaoTest {

  @Autowired UserIpMapper userIpMapper;

  UserIpVo insert() {
    UserIpVo userIpVo = new UserIpVo();
    userIpVo.setUser("user");
    userIpVo.setBussinessUser("bussinessUser");
    userIpVo.setCreator("creator");
    userIpVo.setCreateTime(new Date());
    userIpVo.setUpdateTime(new Date());
    userIpVo.setDesc("desc");
    userIpVo.setIpList("ips");
    userIpMapper.createUserIP(userIpVo);
    return userIpVo;
  }

  @Test
  void createUserIP() {
    insert();
    UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
    assertTrue(userIpVo != null);
  }

  @Test
  void deleteUserIP() {
    insert();
    UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
    userIpMapper.deleteUserIP(Integer.valueOf(userIpVo.getId()));
    UserIpVo list = userIpMapper.queryUserIP("user", "creator");
    assertTrue(list == null);
  }

  @Test
  void updateUserIP() {
    insert();
    UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
    UserIpVo updateUserIpVo = new UserIpVo();
    updateUserIpVo.setId(userIpVo.getId());
    updateUserIpVo.setDesc("desc2");
    updateUserIpVo.setBussinessUser("bussinessUser2");
    userIpMapper.updateUserIP(updateUserIpVo);
    UserIpVo userIpVo1 = userIpMapper.queryUserIP("user", "creator");
    assertTrue(userIpVo1.getDesc().equals("desc2"));
    assertTrue(userIpVo1.getBussinessUser().equals("bussinessUser2"));
  }

  @Test
  void queryUserIP() {
    insert();
    UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
    assertTrue(userIpVo != null);
  }

  @Test
  void queryUserIPList() {
    insert();
    List<UserIpVo> userIpVos = userIpMapper.queryUserIPList("user", "creator");
    assertTrue(userIpVos.size() > 0);
  }
}
