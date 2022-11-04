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

package org.apache.linkis.publicservice.common.lock.dao;

import org.apache.linkis.publicservice.common.lock.entity.CommonLock;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommonLockMapper {
  @Insert(
      "insert into linkis_ps_common_lock (lock_object, time_out, update_time, create_time)"
          + "values(#{jsonObject}, #{timeOut}, now(), now())")
  void lock(@Param("jsonObject") String jsonObject, @Param("timeOut") Long timeOut);

  @Delete("delete  from linkis_ps_common_lock where lock_object = #{jsonObject}")
  void unlock(String jsonObject);

  @Select("select * from linkis_ps_common_lock")
  @Results({
    @Result(property = "updateTime", column = "update_time"),
    @Result(property = "createTime", column = "create_time")
  })
  List<CommonLock> getAll();
}
