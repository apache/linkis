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

package org.apache.linkis.storage.fs;

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.domain.FsPathListWithError;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileSystem implements Fs {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystem.class);

  protected String user;
  private String defaultFilePerm = "rwxr-----"; // 740
  private String defaultFolderPerm = "rwxr-x---"; // 750

  public String getDefaultFilePerm() {
    return defaultFilePerm;
  }

  public String getDefaultFolderPerm() {
    return defaultFolderPerm;
  }

  public abstract String listRoot() throws IOException;

  public abstract long getTotalSpace(FsPath dest) throws IOException;

  public abstract long getFreeSpace(FsPath dest) throws IOException;

  public abstract long getUsableSpace(FsPath dest) throws IOException;

  public abstract long getLength(FsPath dest) throws IOException;

  public String getChecksumWithMD5(FsPath dest) throws IOException {
    return null;
  }

  public String getChecksum(FsPath dest) throws IOException {
    return null;
  }

  public long getBlockSize(FsPath dest) throws IOException {
    return 0L;
  }

  public List<FsPath> getAllFilePaths(FsPath dest) throws IOException {
    return Collections.emptyList();
  }

  public abstract boolean canExecute(FsPath dest) throws IOException;

  public abstract boolean setOwner(FsPath dest, String user, String group) throws IOException;

  public abstract boolean setOwner(FsPath dest, String user) throws IOException;

  public abstract boolean setGroup(FsPath dest, String group) throws IOException;

  public abstract boolean copy(String origin, String dest) throws IOException;

  public FsPathListWithError listPathWithError(FsPath path) throws IOException {
    return null;
  }

  public boolean createNewFile(FsPath dest) throws IOException {
    return create(dest.getPath());
  }

  public boolean copyFile(FsPath origin, FsPath dest) throws IOException {
    return copy(origin.getPath(), dest.getPath());
  }

  /**
   * Set permissions for a path(设置某个路径的权限)
   *
   * @param dest path(路径)
   * @param permission Permissions, such as rwxr-x---etc.(权限，如rwxr-x---等)
   * @throws IOException Setting a failure throws an exception, or throws an exception if the user
   *     is not owner(设置失败抛出异常，或者如果该用户不是owner，也会抛出异常)
   * @return
   */
  public abstract boolean setPermission(FsPath dest, String permission) throws IOException;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  protected FsPath getParentPath(String path) {
    String parentPath = "";
    if (File.separatorChar == '/') {
      parentPath = new File(path).getParent();
    } else {
      parentPath = path.substring(0, path.lastIndexOf("/"));
    }
    LOG.info("Get parent path:" + parentPath);
    return new FsPath(parentPath);
  }

  public boolean isOwner(String dest) throws IOException {
    FsPath fsPath = get(dest);
    return user.equals(fsPath.getOwner()) || user.equals(rootUserName());
  }
}
