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

package org.apache.linkis.entrance.utils

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.utils.GovernanceUtils
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.{FileSystemUtils, StorageConfiguration, StorageUtils}

object CommonLogPathUtils {

  def buildCommonPath(commonPath: String, isResPath: Boolean): Unit = {
    val fileSystem = getRootFs(commonPath)
    fileSystem.init(null)
    val realPath: String = if (commonPath.endsWith("/")) {
      commonPath.substring(0, commonPath.length - 1)
    } else {
      commonPath
    }
    val fsPath = new FsPath(realPath)
    if (!fileSystem.exists(fsPath)) {
      FileSystemUtils.mkdirs(fileSystem, fsPath, StorageUtils.getJvmUser)
      fileSystem.setPermission(fsPath, "770")
    }
    // create defalut creator path
    if (isResPath) {
      val defaultPath =
        GovernanceUtils.getResultParentPath(GovernanceUtils.LINKIS_DEFAULT_RES_CREATOR)
      val resPath = new FsPath(defaultPath)
      if (!fileSystem.exists(resPath)) {
        FileSystemUtils.mkdirs(fileSystem, resPath, StorageUtils.getJvmUser)
        fileSystem.setPermission(resPath, "770")
      }
    }
    Utils.tryQuietly(fileSystem.close())
  }

  def getRootFs(commonPath: String): FileSystem = {
    val fsPath = new FsPath(commonPath)
    if (StorageUtils.HDFS.equals(fsPath.getFsType)) {
      FSFactory.getFs(StorageUtils.HDFS).asInstanceOf[FileSystem]
    } else {
      FSFactory
        .getFs(StorageUtils.FILE, StorageConfiguration.LOCAL_ROOT_USER.getValue)
        .asInstanceOf[FileSystem]
    }
  }

  def getResultParentPath(jobRequest: JobRequest): String = {
    val userCreator = LabelUtil.getUserCreatorLabel(jobRequest.getLabels)
    val creator =
      if (null == userCreator) EntranceConfiguration.DEFAULT_CREATE_SERVICE.getValue
      else userCreator.getCreator
    GovernanceUtils.getResultParentPath(creator)
  }

  def getResultPath(jobRequest: JobRequest): String = {
    val userCreator = LabelUtil.getUserCreatorLabel(jobRequest.getLabels)
    val creator =
      if (null == userCreator) EntranceConfiguration.DEFAULT_CREATE_SERVICE.getValue
      else userCreator.getCreator
    val parentPath = GovernanceUtils.getResultParentPath(creator)
    parentPath + "/" + jobRequest.getExecuteUser + "/" + jobRequest.getId
  }

}
