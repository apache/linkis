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
 
package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.Entity.ResourceTask;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface TaskDao {

  /**
   * 插入任务信息
   * @param resourceTask 任务信息
   * @return 主键ID
   */
    long insert(ResourceTask resourceTask);

  /**
   * 更新任务状态
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   */
    void updateState(@Param("taskId") long taskId, @Param("state") String state, @Param("updateTime") Date updateTime);

  /**
   * 更新任务状态为失败
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   * @param errMsg 异常信息
   */
    void updateState2Failed(@Param("taskId") long taskId, @Param("state") String state, @Param("updateTime") Date updateTime, @Param("errMsg") String errMsg);

  /**
   * 获取资源最大版本号
   * @param resourceId 资源ID
   * @return 资源最大版本号
   */
    String getNewestVersion(@Param("resourceId") String resourceId);
}
