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
import org.apache.linkis.bml.dao.VersionDao;
import org.apache.linkis.bml.entity.ResourceVersion;
import org.apache.linkis.bml.entity.Version;
import org.apache.linkis.bml.service.ResourceService;
import org.apache.linkis.bml.service.VersionService;
import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.FSFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VersionServiceImpl implements VersionService {

  private static final Logger logger = LoggerFactory.getLogger(VersionServiceImpl.class);
  /** When the version is updated, OVER_WRITE is always false */
  private static final boolean OVER_WRITE = false;

  @Autowired private VersionDao versionDao;

  @Autowired private ResourceService resourceService;

  @Override
  public Version getVersion(String resourceId, String version) {
    return versionDao.getVersion(resourceId, version);
  }

  @Override
  public List<ResourceVersion> getResourcesVersions(Map paramMap) {
    return versionDao.getResourcesVersions(paramMap);
  }

  @Override
  public void deleteResourceVersion(String resourceId, String version) {
    versionDao.deleteVersion(resourceId, version);
  }

  @Override
  public void deleteResourceVersions(String resourceId) {}

  @Override
  public void deleteResourcesVersions(List<String> resourceIds, List<String> versions) {}

  @Override
  public String updateVersion(
      String resourceId, String user, MultipartFile file, Map<String, Object> params)
      throws Exception {
    ResourceHelper resourceHelper = ResourceHelperFactory.getResourceHelper();
    InputStream inputStream = file.getInputStream();
    // final String resourceIdLock = resourceId.intern();
    // String fileName = file.getOriginalFilename();
    // 获取资源的path
    String newVersion = params.get("newVersion").toString();
    String path = versionDao.getResourcePath(resourceId);
    // if the bml resource storage prefix has changed，then regenerate the path.
    if (resourceHelper.checkBmlResourceStoragePrefixPathIfChanged(path)) {
      path =
          resourceHelper.generatePath(
              user, path.substring(path.lastIndexOf("/") + 1), new HashMap<>());
    }
    // resource path with version such as
    // hdfs:///apps-data/hadoop/bml/20210608/1c8b78e1-ea12-4fa5-9637-ddc9e9a25bae_v000003
    path = path + "_" + newVersion;
    // 上传资源前，需要对resourceId这个字符串的intern进行加锁，这样所有需要更新该资源的用户都会同步
    // synchronized (resourceIdLock.intern()){
    // 资源上传到hdfs
    StringBuilder stringBuilder = new StringBuilder();
    long size = resourceHelper.upload(path, user, inputStream, stringBuilder, OVER_WRITE);
    String md5String = stringBuilder.toString();
    String clientIp = params.get("clientIp").toString();
    // 生成新的version
    // String lastVersion = versionDao.getNewestVersion(resourceId);
    // 更新resource_version表
    ResourceVersion resourceVersion =
        ResourceVersion.createNewResourceVersion(
            resourceId, path, md5String, clientIp, size, newVersion, 1);
    versionDao.insertNewVersion(resourceVersion);
    // }
    return newVersion;
  }

  private String generateNewVersion(String version) {
    int next = Integer.parseInt(version.substring(1, version.length())) + 1;
    return Constant.VERSION_PREFIX + String.format(Constant.VERSION_FORMAT, next);
  }

  @Override
  public String getNewestVersion(String resourceId) {
    return versionDao.getNewestVersion(resourceId);
  }

  @Override
  public boolean downloadResource(
      String user,
      String resourceId,
      String version,
      OutputStream outputStream,
      Map<String, Object> properties)
      throws IOException {
    // 1.Get the path of the resource corresponding to resourceId and version
    // 2.Get startByte and EndByte
    // 3.Use storage to get input stream
    ResourceVersion resourceVersion = versionDao.findResourceVersion(resourceId, version);
    long startByte = resourceVersion.getStartByte();
    long endByte = resourceVersion.getEndByte();
    String path = resourceVersion.getResource();
    Fs fileSystem = FSFactory.getFsByProxyUser(new FsPath(path), user);
    fileSystem.init(new HashMap<String, String>());
    InputStream inputStream = fileSystem.read(new FsPath(path));
    inputStream.skip(startByte - 1);
    logger.info(
        "{} downLoad source {} inputStream skipped {} bytes", user, resourceId, (startByte - 1));
    byte[] buffer = new byte[1024];
    long size = endByte - startByte + 1;
    int left = (int) size;
    try {
      while (left > 0) {
        int readed = inputStream.read(buffer);
        int useful = Math.min(readed, left);
        if (useful < 0) {
          break;
        }
        left -= useful;
        byte[] bytes = new byte[useful];
        for (int i = 0; i < useful; i++) {
          bytes[i] = buffer[i];
        }
        outputStream.write(bytes);
      }
    } finally {
      // int size = IOUtils.copy(inputStream, outputStream);
      IOUtils.closeQuietly(inputStream);
      fileSystem.close();
    }
    return size >= 0;
  }

  @Override
  public List<Version> getVersions(String resourceId) {
    return versionDao.getVersions(resourceId);
  }
  //    @Override
  //    public List<Version> getVersions(String resourceId, List<String> versions) {
  //        return versionDao.getVersions(resourceId, versions);
  //    }

  // 分页查询
  @Override
  public List<Version> selectVersionByPage(int currentPage, int pageSize, String resourceId) {
    List<Version> rvList = null;
    if (StringUtils.isNotEmpty(resourceId)) {
      PageHelper.startPage(currentPage, pageSize);
      rvList = versionDao.selectVersionByPage(resourceId);
    } else {
      rvList = new ArrayList<Version>();
    }
    PageInfo<Version> pageInfo = new PageInfo<>(rvList);
    return pageInfo.getList();
  }

  @Override
  public List<ResourceVersion> getAllResourcesViaSystem(String system, String user) {
    return versionDao.getAllResourcesViaSystem(system, user);
  }

  @Override
  public List<ResourceVersion> selectResourcesViaSystemByPage(
      int currentPage, int pageSize, String system, String user) {
    List<ResourceVersion> resourceVersions = null;
    if (StringUtils.isNotEmpty(system) || StringUtils.isNotEmpty(user)) {
      PageHelper.startPage(currentPage, pageSize);
      resourceVersions = versionDao.selectResourcesViaSystemByPage(system, user);
    } else {
      resourceVersions = new ArrayList<ResourceVersion>();
    }
    PageInfo<ResourceVersion> pageInfo = new PageInfo<>(resourceVersions);
    return pageInfo.getList();
  }

  @Override
  public boolean checkVersion(String resourceId, String version) {
    return versionDao.checkVersion(resourceId, version) == 1;
  }

  @Override
  public boolean canAccess(String resourceId, String version) {
    return versionDao.selectResourceVersionEnbleFlag(resourceId, version) == 1;
  }
}
