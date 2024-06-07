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
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.{FileSystemUtils, StorageConfiguration, StorageUtils}

import java.text.SimpleDateFormat
import java.util.Date

object CommonLogPathUtils {

  def buildCommonPath(commonPath: String): Unit = {
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

  private val resPrefix = EntranceConfiguration.DEFAULT_LOGPATH_PREFIX.getValue

  /**
   * get result path parentPath: resPrefix + dateStr + result + creator subPath: parentPath +
   * executeUser + taskid + filename
   * @param jobRequest
   * @return
   */
  def getResultParentPath(jobRequest: JobRequest): String = {
    val resStb = new StringBuilder()
    if (resStb.endsWith("/")) {
      resStb.append(resPrefix)
    } else {
      resStb.append(resPrefix).append("/")
    }
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val date = new Date(System.currentTimeMillis)
    val dateString = dateFormat.format(date)
    val userCreator = LabelUtil.getUserCreatorLabel(jobRequest.getLabels)
    val creator =
      if (null == userCreator) EntranceConfiguration.DEFAULT_CREATE_SERVICE
      else userCreator.getCreator
    resStb.append("result").append("/").append(dateString).append("/").append(creator)
    resStb.toString()
  }

  def getResultPath(jobRequest: JobRequest): String = {
    val parentPath = getResultParentPath(jobRequest)
    parentPath + "/" + jobRequest.getExecuteUser + "/" + jobRequest.getId
  }

}
