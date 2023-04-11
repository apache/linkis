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

package org.apache.linkis.storage;

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.factory.BuildFactory;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import java.text.MessageFormat;
import java.util.Map;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.UNSUPPORTED_FILE;

public class FSFactory {
  private static final Map<String, BuildFactory> buildClasses =
      StorageUtils.loadClass(
          StorageConfiguration.STORAGE_BUILD_FS_CLASSES.getValue(), t -> t.fsName());

  public static BuildFactory getBuildFactory(String fsName) {
    if (!buildClasses.containsKey(fsName)) {
      throw new StorageWarnException(
          UNSUPPORTED_FILE.getErrorCode(),
          MessageFormat.format(UNSUPPORTED_FILE.getErrorDesc(), fsName));
    }
    return buildClasses.get(fsName);
  }

  public static Fs getFs(String fsType, String proxyUser) {
    String user = StorageUtils.getJvmUser();
    return getBuildFactory(fsType).getFs(user, proxyUser);
  }

  public static Fs getFs(String fsType) {
    String user = StorageUtils.getJvmUser();
    return getBuildFactory(fsType).getFs(user, user);
  }

  /**
   * 1. If this machine has shared storage, the file:// type FS obtained here is the FS of the
   * process user. 2. If this machine does not have shared storage, then the file:// type FS
   * obtained is the proxy to the Remote (shared storage machine root) FS. 3. If it is HDFS, it
   * returns the FS of the process user. 1、如果这台机器装有共享存储则这里获得的file://类型的FS为该进程用户的FS
   * 2、如果这台机器没有共享存储则获得的file://类型的FS为代理到Remote（共享存储机器root）的FS 3、如果是HDFS则返回的就是该进程用户的FS
   *
   * @param fsPath
   * @return
   */
  public static Fs getFs(FsPath fsPath) {
    return getFs(fsPath.getFsType());
  }

  /**
   * 1. If the process user is passed and the proxy user and the process user are consistent, the
   * file:// type FS is the FS of the process user (the shared storage exists) 2. If the process
   * user is passed and the proxy user and the process user are consistent and there is no shared
   * storage, the file:// type FS is the proxy to the remote (shared storage machine root) FS 3. If
   * the passed proxy user and process user are consistent, the hdfs type is the FS of the process
   * user. 4. If the proxy user and the process user are inconsistent, the hdfs type is the FS after
   * the proxy.
   *
   * @param fsPath
   * @param proxyUser
   * @return
   */
  public static Fs getFsByProxyUser(FsPath fsPath, String proxyUser) {
    return getFs(fsPath.getFsType(), proxyUser);
  }

  public Fs getFSByLabel(String fs, String label) {
    String user = StorageUtils.getJvmUser();
    return getBuildFactory(fs).getFs(user, user, label);
  }

  public Fs getFSByLabelAndUser(String fs, String label, String proxy) {
    String user = StorageUtils.getJvmUser();
    return getBuildFactory(fs).getFs(user, proxy, label);
  }
}
