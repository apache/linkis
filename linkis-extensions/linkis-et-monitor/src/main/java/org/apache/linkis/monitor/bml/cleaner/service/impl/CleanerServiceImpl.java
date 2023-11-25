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

package org.apache.linkis.monitor.bml.cleaner.service.impl;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.monitor.bml.cleaner.dao.VersionDao;
import org.apache.linkis.monitor.bml.cleaner.entity.CleanedResourceVersion;
import org.apache.linkis.monitor.bml.cleaner.entity.ResourceVersion;
import org.apache.linkis.monitor.bml.cleaner.service.CleanerService;
import org.apache.linkis.monitor.bml.cleaner.service.VersionService;
import org.apache.linkis.monitor.bml.cleaner.vo.CleanResourceVo;
import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CleanerServiceImpl implements CleanerService {

  private final Logger logger = LoggerFactory.getLogger("CleanerServiceImpl");

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

  public static final String VERSION_FORMAT = "%06d";
  public static final String VERSION_PREFIX = "v";
  public static final String TRASH_DIR = "/trash";

  private FileSystem fs = null;

  @Autowired private VersionDao versionDao;

  public void setVersionDao(VersionDao versionDao) {
    this.versionDao = versionDao;
  }

  private Set<String> cleanedResourceIds = new HashSet<String>();

  Date previous;

  @Autowired VersionService versionService;

  public void clean() {
    previous =
        new Date(
            System.currentTimeMillis()
                - (Long) Constants.BML_PREVIOUS_INTERVAL_TIME_DAYS().getValue() * 86400 * 1000);

    if ((Integer) Constants.BML_VERSION_MAX_NUM().getValue()
            - (Integer) Constants.BML_VERSION_KEEP_NUM().getValue()
        <= 1) {
      logger.error("conf error need to keep version num > 1");
      return;
    }
    List<CleanResourceVo> needCleanResources = getCleanResources();
    while (needCleanResources != null && needCleanResources.size() > 0) {
      logger.info("need cleaned resource count:{}", needCleanResources.size());
      fs =
          (FileSystem)
              FSFactory.getFs(StorageUtils.HDFS, StorageConfiguration.HDFS_ROOT_USER.getValue());
      for (CleanResourceVo resourceVo : needCleanResources) {
        String minVersion =
            versionDao.getMinKeepVersion(
                resourceVo.getResourceId(),
                resourceVo.getMaxVersion(),
                (Integer) Constants.BML_VERSION_KEEP_NUM().getValue() - 1);
        List<ResourceVersion> cleanVersionList =
            versionDao.getCleanVersionsByResourceId(resourceVo.getResourceId(), minVersion);
        // move on hdfs
        for (ResourceVersion version : cleanVersionList) {
          FsPath srcPath = new FsPath(version.getResource());
          // fs放到外层
          try {
            fs.init(null);
            if (!fs.exists(srcPath)) {
              logger.error("try to move but bml source file:{} not exists!", version.getResource());
              CleanedResourceVersion cleanedResourceVersion =
                  CleanedResourceVersion.copyFromResourceVersion(version);
              cleanedResourceVersion.setResource("");
              versionService.moveOnDb(cleanedResourceVersion, version.getId());
              continue;
            }
            String destPrefix =
                version.getResource().substring(0, version.getResource().indexOf("/bml/") + 4);
            String destPath =
                destPrefix
                    + TRASH_DIR
                    + File.separator
                    + sdf.format(new Date())
                    + File.separator
                    + version.getResourceId()
                    + "_"
                    + version.getVersion();
            FsPath dest = new FsPath(destPath);
            if (!fs.exists(dest.getParent())) {
              fs.mkdirs(dest.getParent());
            }
            logger.info("begin to mv bml resource:{} to dest:{}", version.getResource(), destPath);
            CleanedResourceVersion cleanedResourceVersion =
                CleanedResourceVersion.copyFromResourceVersion(version);
            cleanedResourceVersion.setResource(destPath);
            versionService.doMove(fs, srcPath, dest, cleanedResourceVersion, version.getId());
          } catch (Exception e) {
            logger.error("failed to mv bml resource:{}", e.getMessage(), e);
          }
        }

        cleanedResourceIds.add(resourceVo.getResourceId());
      }
      needCleanResources = getCleanResources();
    }
  }

  public void run() {
    logger.info("start to clean.");
    clean();
    logger.info("start to shutdown.");
    shutdown();
  }

  void shutdown() {
    IOUtils.closeQuietly(fs);
  }

  List<CleanResourceVo> getCleanResources() {
    List<CleanResourceVo> cleanResourceVoList =
        versionDao.getAllNeedCleanResource(
            (Integer) Constants.BML_VERSION_MAX_NUM().getValue(),
            previous,
            (Integer) Constants.BML_CLEAN_ONCE_RESOURCE_LIMIT_NUM().getValue());

    return cleanResourceVoList.stream()
        .filter(cleanResourceVo -> !cleanedResourceIds.contains(cleanResourceVo.getResourceId()))
        .collect(Collectors.toList());
  }
}
