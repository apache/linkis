/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.bml.service.impl;

import com.webank.wedatasphere.linkis.bml.Entity.ResourceTask;
import com.webank.wedatasphere.linkis.bml.Entity.Version;
import com.webank.wedatasphere.linkis.bml.common.Constant;
import com.webank.wedatasphere.linkis.bml.dao.ResourceDao;
import com.webank.wedatasphere.linkis.bml.dao.TaskDao;
import com.webank.wedatasphere.linkis.bml.dao.VersionDao;
import com.webank.wedatasphere.linkis.bml.service.ResourceService;
import com.webank.wedatasphere.linkis.bml.service.TaskService;
import com.webank.wedatasphere.linkis.bml.service.VersionService;
import com.webank.wedatasphere.linkis.bml.threading.TaskState;
import com.webank.wedatasphere.linkis.bml.common.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author cooperyang
 * @date 2019-9-16
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private VersionDao versionDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Override
    public ResourceTask createUploadTask(FormDataMultiPart form, String user,
        Map<String, Object> properties) throws Exception {
        //创建上传任务记录
        String resourceId = UUID.randomUUID().toString();
        ResourceTask resourceTask = ResourceTask.createUploadTask(resourceId, user, properties);
        taskDao.insert(resourceTask);
        LOGGER.info("成功保存上传任务信息.taskId:{},resourceTask:{}", resourceTask.getId(), resourceTask.toString());
        taskDao.updateState(resourceTask.getId(), TaskState.RUNNING.getValue(), new Date());
        LOGGER.info("成功更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.RUNNING.getValue());
        properties.put("resourceId", resourceTask.getResourceId());
        try {
          ResourceServiceImpl.UploadResult result = resourceService.upload(form, user, properties).get(0);
          if (result.isSuccess()){
            taskDao.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
            LOGGER.info("上传资源成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.SUCCESS.getValue());
          } else {
            taskDao.updateState(resourceTask.getId(), TaskState.FAILED.getValue(), new Date());
            LOGGER.info("上传资源失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.FAILED.getValue());
          }
        } catch (Exception e) {
          taskDao.updateState2Failed(resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
          LOGGER.error("上传资源失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.FAILED.getValue(), e);
          throw e;
        }
        //创建上传任务线程
      return resourceTask;
    }

  @Override
  public ResourceTask createUpdateTask(String resourceId, String user,
      FormDataMultiPart formDataMultiPart, Map<String, Object> properties) throws Exception{
      final String resourceIdLock = resourceId.intern();
      /*
      多个BML服务器实例对同一资源resourceId同时更新,规定只能有一个实例能更新成功,
      实现方案是:linkis_resources_task.resource_id和version设置唯一索引
      同一台服务器实例对同一资源更新,上传资源前，需要对resourceId这个字符串的intern进行加锁，这样所有需要更新该资源的用户都会同步
       */
      synchronized (resourceIdLock){
        String system = resourceDao.getResource(resourceId).getSystem();
        //生成新的version
        String lastVersion = getResourceLastVersion(resourceId);
        String newVersion = generateNewVersion(lastVersion);
        ResourceTask resourceTask = ResourceTask.createUpdateTask(resourceId, newVersion, user, system, properties);
        try {
          taskDao.insert(resourceTask);
        } catch (Exception e) {
          UpdateResourceException updateResourceException = new UpdateResourceException();
          updateResourceException.initCause(e);
          throw updateResourceException;
        }
        LOGGER.info("成功保存上传任务信息.taskId:{},resourceTask:{}", resourceTask.getId(), resourceTask.toString());
        taskDao.updateState(resourceTask.getId(), TaskState.RUNNING.getValue(), new Date());
        LOGGER.info("成功更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.RUNNING.getValue());
        properties.put("newVersion", resourceTask.getVersion());
        try {
          versionService.updateVersion(resourceTask.getResourceId(), user, formDataMultiPart, properties);
          taskDao.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
          LOGGER.info("上传更新资源成功.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.SUCCESS.getValue());
        } catch (Exception e) {
          taskDao.updateState2Failed(resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
          LOGGER.error("上传更新资源失败.更新任务 taskId:{}-resourceId:{} 为 {} 状态.", resourceTask.getId(), resourceTask.getResourceId(), TaskState.FAILED.getValue(), e);
          throw e;
        }
        //创建上传任务线程
        return resourceTask;
      }
  }

  @Override
  public ResourceTask createDownloadTask(String resourceId, String version, String user,
      String clientIp) {
    String system = resourceDao.getResource(resourceId).getSystem();
    ResourceTask resourceTask = ResourceTask.createDownloadTask(resourceId, version, user, system, clientIp);
    taskDao.insert(resourceTask);
    LOGGER.info("成功保存下载任务信息.taskId:{},resourceTask:{}", resourceTask.getId(), resourceTask.toString());
    return resourceTask;
  }

  /**
   * 更新任务状态
   *
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   */
  @Override
  public void updateState(long taskId, String state, Date updateTime) {
    taskDao.updateState(taskId, state, updateTime);
  }

  /**
   * 更新任务状态为失败
   *
   * @param taskId 任务ID
   * @param state 执行状态
   * @param updateTime 操作时间
   * @param errMsg 异常信息
   */
  @Override
  public void updateState2Failed(long taskId, String state, Date updateTime, String errMsg) {
    taskDao.updateState2Failed(taskId, state, updateTime, errMsg);
  }

  @Override
  public ResourceTask createDeleteVersionTask(String resourceId, String version, String user,
      String clientIp) {
    String system = resourceDao.getResource(resourceId).getSystem();
    ResourceTask resourceTask = ResourceTask.createDeleteVersionTask(resourceId, version, user, system, clientIp);
    taskDao.insert(resourceTask);
    LOGGER.info("成功保存删除资源版本任务信息.taskId:{},resourceTask:{}", resourceTask.getId(), resourceTask.toString());
    return resourceTask;
  }

  @Override
  public ResourceTask createDeleteResourceTask(String resourceId, String user, String clientIp) {
    String system = resourceDao.getResource(resourceId).getSystem();
    List<Version> versions = versionDao.getVersions(resourceId);
    StringBuilder extraParams = new StringBuilder();
    extraParams.append("delete resourceId:").append(resourceId);
    extraParams.append(", and delete versions is :");
    String delVersions = null;
    if (CollectionUtils.isNotEmpty(versions)) {
      delVersions = versions.stream().map(Version::getVersion).collect(Collectors.joining(","));
    }
    extraParams.append(delVersions);
    ResourceTask resourceTask = ResourceTask.createDeleteResourceTask(resourceId, user, system, clientIp, extraParams.toString());
    taskDao.insert(resourceTask);
    LOGGER.info("成功保存下载任务信息.taskId:{},resourceTask:{}", resourceTask.getId(), resourceTask.toString());
    return resourceTask;
  }

  @Override
  public ResourceTask createDeleteResourcesTask(List<String> resourceIds, String user, String clientIp) {
    String system = resourceDao.getResource(resourceIds.get(0)).getSystem();
    StringBuilder extraParams = new StringBuilder();
    for (String resourceId : resourceIds) {
      extraParams.append("delete resourceId:").append(resourceId);
      extraParams.append(", and delete versions is :");
      String delVersions = null;
      List<Version> versions = versionDao.getVersions(resourceId);
      if (CollectionUtils.isNotEmpty(versions)) {
        delVersions = versions.stream().map(Version::getVersion).collect(Collectors.joining(","));
      }
      extraParams.append(delVersions);
      extraParams.append(System.lineSeparator());
    }
    ResourceTask resourceTask = ResourceTask.createDeleteResourcesTask(user, system, clientIp, extraParams.toString());
    taskDao.insert(resourceTask);
    LOGGER.info("成功保存下载任务信息.taskId:{},resourceTask:{}", resourceTask.getId(), resourceTask.toString());
    return resourceTask;
  }

  /**
   * 先查看linkis_resources_task有没有最新版本号,如果有则以此为主+1返回
   * 如果没有则以linkis_resources_version最新版本号+1返回
   * @param resourceId 资源ID
   * @return 下一个版本号
   */
  private String getResourceLastVersion(String resourceId) {
      String lastVersion = taskDao.getNewestVersion(resourceId);
      if (StringUtils.isBlank(lastVersion)) {
        lastVersion = versionDao.getNewestVersion(resourceId);
      }
      return lastVersion;
  }

  private String generateNewVersion(String version){
    int next = Integer.parseInt(version.substring(1, version.length())) + 1;
    return Constant.VERSION_PREFIX + String.format(Constant.VERSION_FORMAT, next);
  }
}
