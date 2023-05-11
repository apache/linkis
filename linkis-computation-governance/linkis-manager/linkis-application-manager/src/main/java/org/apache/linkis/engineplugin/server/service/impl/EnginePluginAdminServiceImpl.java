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

package org.apache.linkis.engineplugin.server.service.impl;

import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlResourceVersionsResponse;
import org.apache.linkis.bml.protocol.Version;
import org.apache.linkis.common.utils.ZipUtils;
import org.apache.linkis.engineplugin.server.dao.EngineConnBmlResourceDao;
import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.server.localize.DefaultEngineConnBmlResourceGenerator;
import org.apache.linkis.engineplugin.server.restful.EnginePluginRestful;
import org.apache.linkis.engineplugin.server.service.EnginePluginAdminService;
import org.apache.linkis.engineplugin.vo.EnginePluginBMLVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EnginePluginAdminServiceImpl implements EnginePluginAdminService {

  private static final Logger log = LoggerFactory.getLogger(EnginePluginRestful.class);
  @Autowired private EngineConnBmlResourceDao engineConnBmlResourceDao;
  private DefaultEngineConnBmlResourceGenerator defaultEngineConnBmlResourceGenerator =
      new DefaultEngineConnBmlResourceGenerator();

  private BmlClient bmlClient = BmlClientFactory.createBmlClient();

  @Override
  public void rollBackEnginePlugin(EngineConnBmlResource engineConnBmlResource) {
    engineConnBmlResourceDao.update(engineConnBmlResource);
  }

  @Override
  public List<String> getTypeVersionList(String type) {
    return engineConnBmlResourceDao.getTypeVersionList(type);
  }

  @Override
  public List<Version> getVersionList(String userName, String bmlResourceId) {
    BmlResourceVersionsResponse versions = bmlClient.getVersions(userName, bmlResourceId);
    List<Version> versions1 = versions.resourceVersions().versions();
    return versions1;
  }

  @Override
  public List<String> getTypeList() {
    return engineConnBmlResourceDao.getTypeList();
  }

  @Override
  public void deleteEnginePluginBML(String ecType, String version, String username) {
    List<EngineConnBmlResource> allEngineConnBmlResource = null;
    try {
      allEngineConnBmlResource =
          engineConnBmlResourceDao.getAllEngineConnBmlResource(ecType, version);
      allEngineConnBmlResource.forEach(
          engineConnBmlResource -> {
            // bmlClient.deleteResource(username,engineConnBmlResource.getBmlResourceId());
            engineConnBmlResourceDao.delete(engineConnBmlResource);
          });
      String engineConnsHome = defaultEngineConnBmlResourceGenerator.getEngineConnsHome();
      File file = new File(engineConnsHome + "/" + ecType);
      if (file.exists()) {
        deleteDir(file);
        log.info("file {} delete success", ecType);
      }
    } catch (Exception e) {
      log.warn(
          "deleteEnginePluginBML failed ecType:[{}] version:[{}] username:[{}]",
          ecType,
          version,
          username,
          e);
    }
  }

  @Override
  public PageInfo<EngineConnBmlResource> queryDataSourceInfoPage(
      EnginePluginBMLVo enginePluginBMLVo) {
    PageHelper.startPage(enginePluginBMLVo.getCurrentPage(), enginePluginBMLVo.getPageSize());
    try {
      List<EngineConnBmlResource> queryList =
          engineConnBmlResourceDao.selectByPageVo(enginePluginBMLVo);
      return new PageInfo<>(queryList);
    } finally {
      PageHelper.clearPage();
    }
  }

  @Override
  public void uploadToECHome(MultipartFile mfile) {
    String engineConnsHome = defaultEngineConnBmlResourceGenerator.getEngineConnsHome();
    try {
      InputStream in = mfile.getInputStream();
      byte[] buffer = new byte[1024];
      int len = 0;
      File file = new File(engineConnsHome);
      if (!file.exists()) {
        log.info("engineplugin's home doesn’t exist");
      }
      OutputStream out = new FileOutputStream(engineConnsHome + "/" + mfile.getOriginalFilename());
      while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      out.close();
      in.close();
    } catch (Exception e) {
      log.info("file {} upload fail", mfile.getOriginalFilename());
    }

    ZipUtils.fileToUnzip(engineConnsHome + "/" + mfile.getOriginalFilename(), engineConnsHome);
    File file = new File(engineConnsHome + "/" + mfile.getOriginalFilename());
    if (file.exists()) {
      file.delete();
      log.info("file {} delete success", mfile.getOriginalFilename());
    }
  }

  public static void deleteDir(File directory) {
    File files[] = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        deleteDir(file);
      } else {
        file.delete();
      }
    }
    directory.delete();
  }
}
