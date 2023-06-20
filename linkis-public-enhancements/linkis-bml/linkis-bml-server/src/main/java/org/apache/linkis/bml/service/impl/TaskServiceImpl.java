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

package org.apache.linkis.bml.service.impl;

import org.apache.linkis.bml.common.*;
import org.apache.linkis.bml.common.Constant;
import org.apache.linkis.bml.dao.ResourceDao;
import org.apache.linkis.bml.dao.TaskDao;
import org.apache.linkis.bml.dao.VersionDao;
import org.apache.linkis.bml.entity.Resource;
import org.apache.linkis.bml.entity.ResourceTask;
import org.apache.linkis.bml.entity.ResourceVersion;
import org.apache.linkis.bml.entity.Version;
import org.apache.linkis.bml.service.ResourceService;
import org.apache.linkis.bml.service.TaskService;
import org.apache.linkis.bml.service.VersionService;
import org.apache.linkis.bml.threading.TaskState;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageConfiguration;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.bml.common.Constant.FIRST_VERSION;

@Service
public class TaskServiceImpl implements TaskService {

  @Autowired private ResourceService resourceService;

  @Autowired private VersionService versionService;

  @Autowired private TaskDao taskDao;

  @Autowired private ResourceDao resourceDao;

  @Autowired private VersionDao versionDao;

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResourceTask createUploadTask(
      List<MultipartFile> files, String user, Map<String, Object> properties) throws Exception {
    // Create upload task record.
    String resourceId = UUID.randomUUID().toString();
    ResourceTask resourceTask = ResourceTask.createUploadTask(resourceId, user, properties);
    taskDao.insert(resourceTask);
    LOGGER.info(
        "Upload task information was successfully saved (成功保存上传任务信息).taskId:{},resourceTask:{}",
        resourceTask.getId(),
        resourceTask.toString());
    taskDao.updateState(resourceTask.getId(), TaskState.RUNNING.getValue(), new Date());
    LOGGER.info(
        "Successful update task (成功更新任务 ) taskId:{}-resourceId:{} status is  {} .",
        resourceTask.getId(),
        resourceTask.getResourceId(),
        TaskState.RUNNING.getValue());
    properties.put("resourceId", resourceTask.getResourceId());
    try {
      ResourceServiceImpl.UploadResult result =
          resourceService.upload(files, user, properties).get(0);
      if (result.isSuccess()) {
        taskDao.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
        LOGGER.info(
            "Upload resource successfully. Update task(上传资源成功.更新任务) taskId:{}-resourceId:{} status is   {} .",
            resourceTask.getId(),
            resourceTask.getResourceId(),
            TaskState.SUCCESS.getValue());
      } else {
        taskDao.updateState(resourceTask.getId(), TaskState.FAILED.getValue(), new Date());
        LOGGER.info(
            "Upload resource failed. Update task (上传资源失败.更新任务) taskId:{}-resourceId:{}  status is   {} .",
            resourceTask.getId(),
            resourceTask.getResourceId(),
            TaskState.FAILED.getValue());
      }
    } catch (Exception e) {
      taskDao.updateState2Failed(
          resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
      LOGGER.error(
          "Upload resource successfully. Update task (上传资源失败.更新任务) taskId:{}-resourceId:{}  status is   {} .",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.FAILED.getValue(),
          e);
      throw e;
    }
    return resourceTask;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResourceTask createUpdateTask(
      String resourceId, String user, MultipartFile file, Map<String, Object> properties)
      throws Exception {
    final String resourceIdLock = resourceId.intern();
    /*
    多个BML服务器实例对同一资源resourceId同时更新,规定只能有一个实例能更新成功,
    实现方案是:linkis_ps_bml_resources_task.resource_id和version设置唯一索引
    同一台服务器实例对同一资源更新,上传资源前，需要对resourceId这个字符串的intern进行加锁，这样所有需要更新该资源的用户都会同步
     */
    // synchronized (resourceIdLock.intern()){
    String system = resourceDao.getResource(resourceId).getSystem();
    // 生成新的version
    String lastVersion = getResourceLastVersion(resourceId);
    String newVersion = generateNewVersion(lastVersion);
    ResourceTask resourceTask =
        ResourceTask.createUpdateTask(resourceId, newVersion, user, system, properties);
    try {
      taskDao.insert(resourceTask);
    } catch (Exception e) {
      UpdateResourceException updateResourceException = new UpdateResourceException();
      updateResourceException.initCause(e);
      throw updateResourceException;
    }
    LOGGER.info(
        "Upload task information was successfully saved(成功保存上传任务信息).taskId:{},resourceTask:{}",
        resourceTask.getId(),
        resourceTask.toString());
    taskDao.updateState(resourceTask.getId(), TaskState.RUNNING.getValue(), new Date());
    LOGGER.info(
        "Successful update task (成功更新任务 ) taskId:{}-resourceId:{} status is  {} .",
        resourceTask.getId(),
        resourceTask.getResourceId(),
        TaskState.RUNNING.getValue());
    properties.put("newVersion", resourceTask.getVersion());
    try {
      versionService.updateVersion(resourceTask.getResourceId(), user, file, properties);
      taskDao.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
      LOGGER.info(
          "Upload resource successfully. Update task (上传资源成功.更新任务) taskId:{}-resourceId:{}  status is   {}.",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.SUCCESS.getValue());
    } catch (Exception e) {
      taskDao.updateState2Failed(
          resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
      LOGGER.error(
          "Upload resource failed . Update task (上传资源失败.更新任务) taskId:{}-resourceId:{}  status is   {}.",
          resourceTask.getId(),
          resourceTask.getResourceId(),
          TaskState.FAILED.getValue(),
          e);
      throw e;
    }
    // 创建上传任务线程
    return resourceTask;
    // }
  }

  @Override
  public ResourceTask createDownloadTask(
      String resourceId, String version, String user, String clientIp) {
    String system = resourceDao.getResource(resourceId).getSystem();
    ResourceTask resourceTask =
        ResourceTask.createDownloadTask(resourceId, version, user, system, clientIp);
    taskDao.insert(resourceTask);
    LOGGER.info(
        "The download task information was successfully saved (成功保存下载任务信息).taskId:{},resourceTask:{}",
        resourceTask.getId(),
        resourceTask.toString());
    return resourceTask;
  }

  /**
   * Update task status
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
   * Update task status to failed
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
  public ResourceTask createDeleteVersionTask(
      String resourceId, String version, String user, String clientIp) {
    String system = resourceDao.getResource(resourceId).getSystem();
    ResourceTask resourceTask =
        ResourceTask.createDeleteVersionTask(resourceId, version, user, system, clientIp);
    taskDao.insert(resourceTask);
    LOGGER.info(
        "The deleted resource version task information was successfully saved (成功保存删除资源版本任务信息).taskId:{},resourceTask:{}",
        resourceTask.getId(),
        resourceTask.toString());
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
    ResourceTask resourceTask =
        ResourceTask.createDeleteResourceTask(
            resourceId, user, system, clientIp, extraParams.toString());
    taskDao.insert(resourceTask);
    LOGGER.info(
        "The download task information was successfully saved (成功保存下载任务信息).taskId:{},resourceTask:{}",
        resourceTask.getId(),
        resourceTask.toString());
    return resourceTask;
  }

  @Override
  public ResourceTask createDeleteResourcesTask(
      List<String> resourceIds, String user, String clientIp) {
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
    ResourceTask resourceTask =
        ResourceTask.createDeleteResourcesTask(user, system, clientIp, extraParams.toString());
    taskDao.insert(resourceTask);
    LOGGER.info(
        "The download task information was successfully saved (成功保存下载任务信息).taskId:{},resourceTask:{}",
        resourceTask.getId(),
        resourceTask.toString());
    return resourceTask;
  }

  @Override
  public ResourceTask createRollbackVersionTask(
      String resourceId, String version, String user, Map<String, Object> properties)
      throws Exception {
    LOGGER.info("begin to rollback version,resourceId:{}, version:{}", resourceId, version);
    String lastVersion = getResourceLastVersion(resourceId);
    String newVersion = generateNewVersion(lastVersion);
    String firstVersionPath = versionDao.getResourcePath(resourceId);
    String dest = firstVersionPath + "_" + newVersion;
    String src;
    if (version.equals(FIRST_VERSION)) {
      src = firstVersionPath;
    } else {
      src = firstVersionPath + "_" + version;
    }
    FileSystem fs = null;
    ResourceTask resourceTask =
        ResourceTask.createRollbackVersionTask(resourceId, newVersion, user, null, properties);
    try {
      taskDao.insert(resourceTask);
      FsPath srcPath = new FsPath(src);
      FsPath destPath = new FsPath(dest);
      fs = (FileSystem) FSFactory.getFsByProxyUser(destPath, user);
      fs.init(null);
      fs.copyFile(srcPath, destPath);
      ResourceVersion oldVersion = versionDao.findResourceVersion(resourceId, version);
      ResourceVersion insertVersion = ResourceVersion.copyFromOldResourceVersion(oldVersion);
      insertVersion.setResource(dest);
      insertVersion.setVersion(newVersion);
      insertVersion.setStartTime(new Date());
      insertVersion.setEndTime(new Date());
      versionDao.insertNewVersion(insertVersion);
      taskDao.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
    } catch (Exception e) {
      taskDao.updateState2Failed(
          resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
      throw e;
    } finally {
      IOUtils.closeQuietly(fs);
    }
    LOGGER.info("end to rollback version,resourceId:{}, version:{}", resourceId, version);
    return resourceTask;
  }

  @Override
  @Transactional
  public ResourceTask createCopyResourceTask(
      String resourceId, String anotherUser, Map<String, Object> properties) throws Exception {
    List<ResourceVersion> resourceVersions = versionDao.getResourceVersionsByResourceId(resourceId);
    String newResourceId = UUID.randomUUID().toString();
    ResourceHelper resourceHelper = ResourceHelperFactory.getResourceHelper();
    String firstPath = resourceHelper.generatePath(anotherUser, newResourceId, properties);
    FsPath firstDestPath = new FsPath(firstPath);
    FileSystem hadoopFs =
        (FileSystem)
            FSFactory.getFsByProxyUser(
                firstDestPath, StorageConfiguration.HDFS_ROOT_USER.getValue());
    FileSystem anotherUserFs = (FileSystem) FSFactory.getFsByProxyUser(firstDestPath, anotherUser);
    try {
      hadoopFs.init(null);
      anotherUserFs.init(null);
      if (!anotherUserFs.exists(firstDestPath.getParent())) {
        anotherUserFs.mkdirs(firstDestPath.getParent());
      }
    } catch (IOException e) {
      LOGGER.error("failed to get filesystem:", e);
    }
    ResourceTask resourceTask =
        ResourceTask.createCopyResourceTask(newResourceId, anotherUser, null, properties);
    taskDao.insert(resourceTask);
    for (ResourceVersion resourceVersion : resourceVersions) {
      try {
        FsPath srcPath = new FsPath(resourceVersion.getResource());
        FsPath destPath = null;
        if (!resourceVersion.getVersion().equals(FIRST_VERSION)) {
          destPath = new FsPath(firstPath + "_" + resourceVersion.getVersion());
        } else {
          destPath = new FsPath(firstPath);
          Resource insertResource =
              Resource.createNewResource(newResourceId, anotherUser, newResourceId, properties);
          resourceDao.uploadResource(insertResource);
        }
        hadoopFs.copyFile(srcPath, destPath);
        hadoopFs.setOwner(destPath, anotherUser);
        ResourceVersion insertVersion = ResourceVersion.copyFromOldResourceVersion(resourceVersion);
        insertVersion.setResource(destPath.getSchemaPath());
        insertVersion.setStartTime(new Date());
        insertVersion.setEndTime(new Date());
        insertVersion.setResourceId(newResourceId);
        versionDao.insertNewVersion(insertVersion);
      } catch (Exception e) {
        // 某一个版本copy失败
        //                LOGGER.error("failed to copy bml file: ", e);
        taskDao.updateState2Failed(
            resourceTask.getId(), TaskState.FAILED.getValue(), new Date(), e.getMessage());
        IOUtils.closeQuietly(anotherUserFs);
        IOUtils.closeQuietly(hadoopFs);
        throw e;
      }
    }
    taskDao.updateState(resourceTask.getId(), TaskState.SUCCESS.getValue(), new Date());
    IOUtils.closeQuietly(anotherUserFs);
    IOUtils.closeQuietly(hadoopFs);
    return resourceTask;
  }

  /**
   * First check if linkis_resources_task has the latest version number, if there is, then this
   * shall prevail +1 and return If not, return with the latest version number of
   * linkis_resources_version+1
   *
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

  private String generateNewVersion(String version) {
    int next = Integer.parseInt(version.substring(1, version.length())) + 1;
    return Constant.VERSION_PREFIX + String.format(Constant.VERSION_FORMAT, next);
  }
}
