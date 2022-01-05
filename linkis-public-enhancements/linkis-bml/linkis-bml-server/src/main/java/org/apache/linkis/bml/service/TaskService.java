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
 
package org.apache.linkis.bml.service;

import org.apache.linkis.bml.Entity.ResourceTask;

import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TaskService {

  ResourceTask createUploadTask(List<MultipartFile> files, String user, Map<String, Object> properties)
      throws Exception;

  ResourceTask createUpdateTask(String resourceId, String user, MultipartFile file, Map<String, Object> properties)
      throws Exception;

  ResourceTask createDownloadTask(String resourceId, String version, String user, String clientIp);

  /**
   * 更新任务状态
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   */
  void updateState(long taskId, String state, Date updateTime);

  /**
   * 更新任务状态为失败
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   * @param errMsg 异常信息
   */
  void updateState2Failed(long taskId, String state, Date updateTime, String errMsg);

  ResourceTask createDeleteVersionTask(String resourceId, String version, String user, String ip);

  ResourceTask createDeleteResourceTask(String resourceId, String user, String ip);

  ResourceTask createDeleteResourcesTask(List<String> resourceIds, String user, String ip);
}
