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
import org.apache.linkis.monitor.bml.cleaner.service.VersionService;
import org.apache.linkis.storage.fs.FileSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class VersionServiceImpl implements VersionService {

  @Autowired VersionDao versionDao;

  public void setVersionDao(VersionDao versionDao) {
    this.versionDao = versionDao;
  }

  @Transactional(rollbackFor = Throwable.class)
  public void doMove(
      FileSystem fs,
      FsPath srcPath,
      FsPath destPath,
      CleanedResourceVersion insertVersion,
      long delVersionId)
      throws IOException {
    versionDao.insertCleanResourceVersion(insertVersion);
    versionDao.deleteResourceVersionById(delVersionId);
    fs.renameTo(srcPath, destPath);
  }

  @Transactional
  public void moveOnDb(CleanedResourceVersion insertVersion, long delVersionId) {
    versionDao.insertCleanResourceVersion(insertVersion);
    versionDao.deleteResourceVersionById(delVersionId);
  }

  public String test() {
    return "this a test string";
  }
}
