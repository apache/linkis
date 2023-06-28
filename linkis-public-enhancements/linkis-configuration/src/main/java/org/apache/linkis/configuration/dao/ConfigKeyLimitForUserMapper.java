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

import org.apache.linkis.configuration.entity.ConfigKeyLimitForUser;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * linkis_ps_configuration_key_limit_for_user表的dao接口类 @Description
 *
 * @version 1.0
 */
public interface ConfigKeyLimitForUserMapper {

  /**
   * 根据主键删除数据库的记录
   *
   * @param id
   */
  int deleteByPrimaryKey(Long id);

  /**
   * 新写入数据库记录
   *
   * @param configKeyLimitForUser
   */
  int insert(ConfigKeyLimitForUser configKeyLimitForUser);

  /**
   * 批量插入记录，建议List一次不要超过1000条
   *
   * @param list
   */
  int batchInsertList(List<ConfigKeyLimitForUser> list);

  /**
   * 动态字段,写入数据库记录
   *
   * @param configKeyLimitForUser
   */
  int insertSelective(ConfigKeyLimitForUser configKeyLimitForUser);

  /**
   * 根据指定主键获取一条数据库记录
   *
   * @param id
   */
  ConfigKeyLimitForUser selectByPrimaryKey(Long id);

  /**
   * 查询分页数据条数 - 示例方法
   *
   * @param id
   */
  int selectCountByPage(Long id);

  /**
   * 查询分页数据列表 - 示例方法 public DataPage<ConfigKeyLimitForUser> selectByPage(Id id, int pageNo, int
   * pageSize) { if (pageNo > 100) { pageNo = 100; } if (pageNo < 1) { pageNo = 1; } if (pageSize >
   * 50) { pageSize = 50; } if (pageSize < 1) { pageSize = 1; } int totalCount =
   * configKeyLimitForUserDAO.selectCountByPage(id); List<CreditLogEntity> list =
   * configKeyLimitForUserDAO.selectListByPage(id, pageNo, pageSize);
   * DataPage<ConfigKeyLimitForUser> dp = new DataPage<>(list, pageSize, pageNo, totalCount); return
   * dp; }
   *
   * @param id
   * @param pageNo
   * @param pageSize
   */
  List<ConfigKeyLimitForUser> selectListByPage(
      @Param("id") Long id, @Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

  /**
   * 动态字段,根据主键来更新符合条件的数据库记录
   *
   * @param configKeyLimitForUser
   */
  int updateByPrimaryKeySelective(ConfigKeyLimitForUser configKeyLimitForUser);

  /**
   * 根据主键来更新符合条件的数据库记录
   *
   * @param configKeyLimitForUser
   */
  int updateByPrimaryKey(ConfigKeyLimitForUser configKeyLimitForUser);

  // === 下方为用户自定义模块,下次生成会保留 ===

  int batchInsertOrUpdateList(List<ConfigKeyLimitForUser> list);
}
