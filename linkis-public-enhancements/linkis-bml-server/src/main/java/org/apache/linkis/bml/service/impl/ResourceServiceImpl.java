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

import org.apache.linkis.bml.common.Constant;
import org.apache.linkis.bml.common.ResourceHelper;
import org.apache.linkis.bml.common.ResourceHelperFactory;
import org.apache.linkis.bml.dao.ResourceDao;
import org.apache.linkis.bml.dao.VersionDao;
import org.apache.linkis.bml.entity.Resource;
import org.apache.linkis.bml.entity.ResourceVersion;
import org.apache.linkis.bml.service.ResourceService;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ResourceServiceImpl implements ResourceService {

  private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

  @Autowired private ResourceDao resourceDao;

  @Autowired private VersionDao versionDao;

  private static final String FIRST_VERSION = "v000001";

  @Override
  public List<Resource> getResources(Map paramMap) {
    return resourceDao.getResources(paramMap);
  }

  @Override
  public void deleteResource(String resourceId) {
    resourceDao.deleteResource(resourceId);
    versionDao.deleteResource(resourceId);
  }

  @Override
  public void batchDeleteResources(List<String> resourceIds) {
    resourceDao.batchDeleteResources(resourceIds);
    versionDao.batchDeleteResources(resourceIds);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<UploadResult> upload(
      List<MultipartFile> files, String user, Map<String, Object> properties) throws Exception {
    ResourceHelper resourceHelper = ResourceHelperFactory.getResourceHelper();
    // List<FormDataBodyPart> files = formDataMultiPart.getFields("file");
    List<UploadResult> results = new ArrayList<>();
    for (MultipartFile p : files) {
      String resourceId = (String) properties.get("resourceId");
      InputStream inputStream = p.getInputStream();
      String fileName = resourceId;
      // fileName = resourceId;
      String path = resourceHelper.generatePath(user, fileName, properties);
      StringBuilder sb = new StringBuilder();
      long size = resourceHelper.upload(path, user, inputStream, sb, true);
      String md5String = sb.toString();
      boolean isSuccess = false;
      if (StringUtils.isNotEmpty(md5String) && size >= 0) {
        isSuccess = true;
      }
      Resource resource = Resource.createNewResource(resourceId, user, fileName, properties);
      // 插入一条记录到resource表
      long id = resourceDao.uploadResource(resource);
      logger.info("{} uploaded a resource and resourceId is {}", user, resource.getResourceId());
      // 插入一条记录到resource version表
      String clientIp = (String) properties.get("clientIp");
      ResourceVersion resourceVersion =
          ResourceVersion.createNewResourceVersion(
              resourceId, path, md5String, clientIp, size, Constant.FIRST_VERSION, 1);
      versionDao.insertNewVersion(resourceVersion);
      UploadResult uploadResult = new UploadResult(resourceId, FIRST_VERSION, isSuccess);
      results.add(uploadResult);
    }
    return results;
  }

  @Override
  public boolean checkResourceId(String resourceId) {
    return resourceDao.checkExists(resourceId) == 1;
  }

  public static class UploadResult {
    private String resourceId;
    private String version;
    private boolean isSuccess;

    public UploadResult(String resourceId, String version, boolean isSuccess) {
      this.resourceId = resourceId;
      this.version = version;
      this.isSuccess = isSuccess;
    }

    public String getResourceId() {
      return resourceId;
    }

    public void setResourceId(String resourceId) {
      this.resourceId = resourceId;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public boolean isSuccess() {
      return isSuccess;
    }

    public void setSuccess(boolean success) {
      isSuccess = success;
    }
  }

  @Override
  public Resource getResource(String resourceId) {
    return resourceDao.getResource(resourceId);
  }

  @Override
  public boolean checkAuthority(@NotNull String user, String resourceId) {
    return user.equals(resourceDao.getUserByResourceId(resourceId));
  }

  @Override
  public boolean checkExpire(String resourceId, String version) {
    Resource resource = resourceDao.getResource(resourceId);
    ResourceVersion resourceVersion = versionDao.getResourceVersion(resourceId, version);
    if (!resource.isEnableFlag() || !resourceVersion.isEnableFlag()) {
      return false;
    }
    return true;
  }

  @Override
  public void cleanExpiredResources() {
    // 1 找出已经过期的所有的资源
    // 2 将这些资源干掉
    //        List<Resource> resources = resourceDao.getResources();
    //        List<Resource> expiredResources = new ArrayList<>();
    //        for(Resource resource : resources){
    //            if (resource.isExpire() && resource.isEnableFlag()){
    //                String expiredTimeStr =
    // RestfulUtils.getExpireTime(resource.getCreateTime(), resource.getExpireType(),
    // resource.getExpireTime());
    //
    //            }
    //        }
    // resourceDao.cleanExpiredResources();
  }

  @Override
  public void changeOwnerByResourceId(String resourceId, String oldOwner, String newOwner) {
    resourceDao.changeOwner(resourceId, oldOwner, newOwner);
  }

  @Override
  public void copyResourceToOtherUser(String resourceId, String otherUser) {}
}
