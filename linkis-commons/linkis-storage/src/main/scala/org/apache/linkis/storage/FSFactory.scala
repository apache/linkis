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

package org.apache.linkis.storage

import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.UNSUPPORTED_FILE
import org.apache.linkis.storage.exception.StorageFatalException
import org.apache.linkis.storage.factory.BuildFactory
import org.apache.linkis.storage.utils.{StorageConfiguration, StorageUtils}

import java.text.MessageFormat

object FSFactory extends Logging {

  private val buildClasses: Map[String, BuildFactory] = StorageUtils.loadClass[BuildFactory](
    StorageConfiguration.STORAGE_BUILD_FS_CLASSES.getValue,
    t => t.fsName()
  )

  def getBuildFactory(fsName: String): BuildFactory = {
    if (!buildClasses.contains(fsName)) {
      throw new StorageFatalException(
        UNSUPPORTED_FILE.getErrorCode,
        MessageFormat.format(UNSUPPORTED_FILE.getErrorDesc, fsName)
      )
    }
    buildClasses(fsName)
  }

  def getFs(fsType: String, proxyUser: String): Fs = {
    val user = StorageUtils.getJvmUser
    getBuildFactory(fsType).getFs(user, proxyUser)
  }

  def getFs(fsType: String): Fs = {
    val user = StorageUtils.getJvmUser
    getBuildFactory(fsType).getFs(user, user)
  }

  /**
   *   1. If this machine has shared storage, the file:// type FS obtained here is the FS of the
   *      process user. 2, if this machine does not have shared storage, then the file:// type FS
   *      obtained is the proxy to the Remote (shared storage machine root) FS 3. If it is HDFS, it
   *      returns the FS of the process user. 1、如果这台机器装有共享存储则这里获得的file://类型的FS为该进程用户的FS
   *      2、如果这台机器没有共享存储则获得的file://类型的FS为代理到Remote（共享存储机器root）的FS 3、如果是HDFS则返回的就是该进程用户的FS
   * @param fsPath
   * @return
   */
  def getFs(fsPath: FsPath): Fs = {
    getFs(fsPath.getFsType())
  }

  /**
   *   1. If the process user is passed and the proxy user and the process user are consistent, the
   *      file:// type FS is the FS of the process user (the shared storage exists) 2, if the
   *      process user is passed and the proxy user and the process user are consistent and there is
   *      no shared storage, the file:// type FS is the proxy to the remote (shared storage machine
   *      root) FS 3. If the passed proxy user and process user are consistent, the hdfs type is the
   *      FS of the process user. 4. If the proxy user and the process user are inconsistent, the
   *      hdfs type is the FS after the proxy. 1、如果传了进程用户且代理用户和进程用户一致则file://类型的FS为该进程用户的FS（存在共享存储）
   *      2、如果传了进程用户且代理用户和进程用户一致且没有共享存储则file://类型的FS为代理到Remote（共享存储机器root）的FS
   *      3、如果传了的代理用户和进程用户一致则hdfs类型为该进程用户的FS 4、如果传了代理用户和进程用户不一致则hdfs类型为代理后的FS
   *
   * @param fsPath
   * @param proxyUser
   * @return
   */
  def getFsByProxyUser(fsPath: FsPath, proxyUser: String): Fs = {
    getFs(fsPath.getFsType(), proxyUser)
  }

  def getFSByLabel(fs: String, label: String): Fs = {
    val user = StorageUtils.getJvmUser
    getBuildFactory(fs).getFs(user, user, label)
  }

  def getFSByLabelAndUser(fs: String, label: String, proxy: String): Fs = {
    val user = StorageUtils.getJvmUser
    getBuildFactory(fs).getFs(user, proxy, label)
  }

}
