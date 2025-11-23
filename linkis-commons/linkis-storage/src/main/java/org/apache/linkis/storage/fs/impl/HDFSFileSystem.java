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

package org.apache.linkis.storage.fs.impl;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.hadoop.common.conf.HadoopConf;
import org.apache.linkis.hadoop.common.utils.HDFSUtils;
import org.apache.linkis.storage.conf.LinkisStorageConf;
import org.apache.linkis.storage.domain.FsPathListWithError;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HDFSFileSystem extends FileSystem {

  public static final String HDFS_PREFIX_WITHOUT_AUTH = "hdfs:///";
  public static final String HDFS_PREFIX_WITH_AUTH = "hdfs://";
  private org.apache.hadoop.fs.FileSystem fs = null;
  private org.apache.hadoop.conf.Configuration conf = null;

  private String label = null;

  private static final Logger logger = LoggerFactory.getLogger(HDFSFileSystem.class);

  private static final String LOCKER_SUFFIX = "refresh";

  private static final int REFRESH_INTERVAL =
      LinkisStorageConf.HDFS_FILE_SYSTEM_REFRESHE_INTERVAL() * 1000 * 60;

  private static final ConcurrentHashMap<String, Long> lastCallTimes = new ConcurrentHashMap<>();

  /** File System abstract method start */
  @Override
  public String listRoot() throws IOException {
    return "/";
  }

  @Override
  public long getTotalSpace(FsPath dest) throws IOException {
    return 0;
  }

  @Override
  public long getFreeSpace(FsPath dest) throws IOException {
    return 0;
  }

  @Override
  public long getUsableSpace(FsPath dest) throws IOException {
    return 0;
  }

  @Override
  public boolean canExecute(FsPath dest) throws IOException {
    return canAccess(dest, FsAction.EXECUTE, this.user);
  }

  @Override
  public boolean setOwner(FsPath dest, String user, String group) throws IOException {
    fs.setOwner(new Path(checkHDFSPath(dest.getPath())), user, group);
    return true;
  }

  @Override
  public boolean setOwner(FsPath dest, String user) throws IOException {
    Path path = new Path(checkHDFSPath(dest.getPath()));
    fs.setOwner(path, user, fs.getFileStatus(path).getGroup());
    return true;
  }

  @Override
  public boolean setGroup(FsPath dest, String group) throws IOException {
    Path path = new Path(checkHDFSPath(dest.getPath()));
    fs.setOwner(path, fs.getFileStatus(path).getOwner(), group);
    return true;
  }

  @Override
  public boolean mkdir(FsPath dest) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    if (!canExecute(getParentPath(path))) {
      throw new IOException("You have not permission to access path " + path);
    }
    boolean result = fs.mkdirs(new Path(path));
    this.setPermission(new FsPath(path), this.getDefaultFolderPerm());
    return result;
  }

  @Override
  public boolean mkdirs(FsPath dest) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    FsPath parentPath = getParentPath(path);
    while (!exists(parentPath)) {
      parentPath = getParentPath(parentPath.getPath());
    }
    if (!canExecute(parentPath)) {
      throw new IOException("You have not permission to access path " + path);
    }
    boolean result = fs.mkdirs(new Path(path));
    this.setPermission(new FsPath(path), this.getDefaultFolderPerm());
    return result;
  }

  @Override
  public boolean setPermission(FsPath dest, String permission) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    if (!isOwner(path)) {
      throw new IOException(path + " only can be set by owner.");
    }
    FsAction u = null;
    FsAction g = null;
    FsAction o = null;
    if (StringUtils.isNumeric(permission) && permission.length() == 3) {
      u = FsAction.getFsAction(FsPath.permissionFormatted(permission.charAt(0)));
      g = FsAction.getFsAction(FsPath.permissionFormatted(permission.charAt(1)));
      o = FsAction.getFsAction(FsPath.permissionFormatted(permission.charAt(2)));
    } else if (!StringUtils.isNumeric(permission) && permission.length() == 9) {
      u = FsAction.getFsAction(permission.substring(0, 3));
      g = FsAction.getFsAction(permission.substring(3, 6));
      o = FsAction.getFsAction(permission.substring(6, 9));
    } else {
      throw new IOException("Incorrent permission string " + permission);
    }
    FsPermission fsPermission = new FsPermission(u, g, o);
    fs.setPermission(new Path(path), fsPermission);
    return true;
  }

  @Override
  public FsPathListWithError listPathWithError(FsPath path) throws IOException {
    FileStatus[] stat = fs.listStatus(new Path(checkHDFSPath(path.getPath())));
    List<FsPath> fsPaths = new ArrayList<FsPath>();
    for (FileStatus f : stat) {
      fsPaths.add(
          fillStorageFile(
              new FsPath(StorageUtils.HDFS_SCHEMA() + f.getPath().toUri().getPath()), f));
    }
    if (fsPaths.isEmpty()) {
      return null;
    }
    return new FsPathListWithError(fsPaths, "");
  }

  /** FS interface method start */
  @Override
  public void init(Map<String, String> properties) throws IOException {
    if (MapUtils.isNotEmpty(properties)
        && properties.containsKey(StorageConfiguration.PROXY_USER().key())) {
      user = StorageConfiguration.PROXY_USER().getValue(properties);
      properties.remove(StorageConfiguration.PROXY_USER().key());
    }

    if (user == null) {
      throw new IOException("User cannot be empty(用户不能为空)");
    }
    if (label == null && Configuration.IS_MULTIPLE_YARN_CLUSTER()) {
      label = StorageConfiguration.LINKIS_STORAGE_FS_LABEL().getValue();
    }
    /** if properties is null do not to create conf */
    if (MapUtils.isNotEmpty(properties)) {
      conf = HDFSUtils.getConfigurationByLabel(user, label);
      if (MapUtils.isNotEmpty(properties)) {
        for (String key : properties.keySet()) {
          String v = properties.get(key);
          if (StringUtils.isNotEmpty(v)) {
            conf.set(key, v);
          }
        }
      }
    }
    if (null != conf) {
      fs = HDFSUtils.getHDFSUserFileSystem(user, label, conf);
    } else {
      fs = HDFSUtils.getHDFSUserFileSystem(user, label);
    }

    if (fs == null) {
      throw new IOException("init HDFS FileSystem failed!");
    }
    if (StorageConfiguration.FS_CHECKSUM_DISBALE()) {
      fs.setVerifyChecksum(false);
      fs.setWriteChecksum(false);
    }
  }

  @Override
  public String fsName() {
    return "hdfs";
  }

  @Override
  public String rootUserName() {
    return StorageConfiguration.HDFS_ROOT_USER().getValue();
  }

  @Override
  public FsPath get(String dest) throws IOException {
    String realPath = checkHDFSPath(dest);
    return fillStorageFile(new FsPath(realPath), fs.getFileStatus(new Path(realPath)));
  }

  @Override
  public InputStream read(FsPath dest) throws IOException {
    if (!canRead(dest)) {
      throw new IOException("You have not permission to access path " + dest.getPath());
    }
    return fs.open(new Path(dest.getPath()));
  }

  @Override
  public OutputStream write(FsPath dest, boolean overwrite) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    if (!exists(dest)) {
      if (!canWrite(dest.getParent())) {
        throw new IOException("You have not permission to access path " + dest.getParent());
      }
    } else {
      if (!canWrite(dest)) {
        throw new IOException("You have not permission to access path " + path);
      }
    }
    if (!overwrite) {
      return fs.append(new Path(path));
    } else {
      OutputStream out = fs.create(new Path(path), true);
      return out;
    }
  }

  @Override
  public boolean create(String dest) throws IOException {
    if (!canExecute(getParentPath(dest))) {
      throw new IOException("You have not permission to access path " + dest);
    }
    boolean result = fs.createNewFile(new Path(checkHDFSPath(dest)));
    this.setPermission(new FsPath(dest), this.getDefaultFilePerm());
    return result;
  }

  @Override
  public boolean copy(String origin, String dest) throws IOException {
    if (!canExecute(getParentPath(dest))) {
      throw new IOException("You have not permission to access path " + dest);
    }
    boolean res =
        FileUtil.copy(
            fs,
            new Path(checkHDFSPath(origin)),
            fs,
            new Path(checkHDFSPath(dest)),
            false,
            true,
            fs.getConf());
    this.setPermission(new FsPath(dest), this.getDefaultFilePerm());
    return res;
  }

  @Override
  public List<FsPath> list(FsPath path) throws IOException {
    FileStatus[] stat = fs.listStatus(new Path(checkHDFSPath(path.getPath())));
    List<FsPath> fsPaths = new ArrayList<FsPath>();
    for (FileStatus f : stat) {
      fsPaths.add(fillStorageFile(new FsPath(f.getPath().toUri().getPath()), f));
    }
    return fsPaths;
  }

  @Override
  public boolean canRead(FsPath dest) throws IOException {
    return canAccess(dest, FsAction.READ, this.user);
  }

  public boolean canRead(FsPath dest, String user) throws IOException {
    return canAccess(dest, FsAction.READ, user);
  }

  @Override
  public boolean canWrite(FsPath dest) throws IOException {
    return canAccess(dest, FsAction.WRITE, this.user);
  }

  @Override
  public boolean exists(FsPath dest) throws IOException {
    try {
      return fs.exists(new Path(checkHDFSPath(dest.getPath())));
    } catch (IOException e) {
      String message = e.getMessage();
      String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
      if ((message != null && message.matches(LinkisStorageConf.HDFS_FILE_SYSTEM_REST_ERRS()))
          || (rootCauseMessage != null
              && rootCauseMessage.matches(LinkisStorageConf.HDFS_FILE_SYSTEM_REST_ERRS()))) {
        logger.info("Failed to execute exists for user {}, retry", user, e);
        resetRootHdfs();
        return fs.exists(new Path(checkHDFSPath(dest.getPath())));
      } else {
        throw e;
      }
    }
  }

  private void resetRootHdfs() {
    if (fs != null) {
      String locker = user + LOCKER_SUFFIX;
      synchronized (locker.intern()) {
        if (fs != null) {
          if (HadoopConf.HDFS_ENABLE_CACHE()) {
            long currentTime = System.currentTimeMillis();
            Long lastCallTime = lastCallTimes.get(user);

            if (lastCallTime != null && (currentTime - lastCallTime) < REFRESH_INTERVAL) {
              logger.warn(
                  "Method call denied for username: {} Please wait for {} minutes.",
                  user,
                  REFRESH_INTERVAL / 60000);
              return;
            }
            lastCallTimes.put(user, currentTime);
            HDFSUtils.closeHDFSFIleSystem(fs, user, label, true);
          } else {
            HDFSUtils.closeHDFSFIleSystem(fs, user, label);
          }
          logger.warn("{} FS reset close.", user);
          if (null != conf) {
            fs = HDFSUtils.getHDFSUserFileSystem(user, label, conf);
          } else {
            fs = HDFSUtils.getHDFSUserFileSystem(user, label);
          }
        }
      }
    }
  }

  @Override
  public boolean delete(FsPath dest) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    if (!isOwner(path)) {
      throw new IOException("You have not permission to delete path " + path);
    }
    return fs.delete(new Path(path), true);
  }

  @Override
  public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
    if (!isOwner(checkHDFSPath(oldDest.getPath()))) {
      throw new IOException("You have not permission to rename path " + oldDest.getPath());
    }
    return fs.rename(
        new Path(checkHDFSPath(oldDest.getPath())), new Path(checkHDFSPath(newDest.getPath())));
  }

  @Override
  public void close() throws IOException {
    if (null != fs) {
      HDFSUtils.closeHDFSFIleSystem(fs, user);
    } else {
      logger.warn("FS was null, cannot close.");
    }
  }

  /** Utils method start */
  private FsPath fillStorageFile(FsPath fsPath, FileStatus fileStatus) throws IOException {
    fsPath.setAccess_time(fileStatus.getAccessTime());
    fsPath.setModification_time(fileStatus.getModificationTime());
    fsPath.setOwner(fileStatus.getOwner());
    fsPath.setGroup(fileStatus.getGroup());
    fsPath.setIsdir(fileStatus.isDirectory());
    try {
      if (fsPath.isdir()) {
        fsPath.setLength(fs.getContentSummary(fileStatus.getPath()).getLength());
      } else {
        fsPath.setLength(fileStatus.getLen());
      }
      fsPath.setPermissionString(fileStatus.getPermission().toString());
    } catch (Throwable e) {
      logger.warn("Failed to fill storage file：" + fileStatus.getPath(), e);
    }
    return fsPath;
  }

  private boolean canAccess(FsPath fsPath, FsAction access, String user) throws IOException {
    String path = checkHDFSPath(fsPath.getPath());
    if (!exists(fsPath)) {
      throw new IOException("directory or file not exists: " + path);
    }

    FileStatus f = fs.getFileStatus(new Path(path));
    FsPermission permission = f.getPermission();
    UserGroupInformation ugi = HDFSUtils.getUserGroupInformation(user);
    String[] groupNames;
    try {
      groupNames = ugi.getGroupNames();
    } catch (NullPointerException e) {
      if ((Boolean) org.apache.linkis.common.conf.Configuration.IS_TEST_MODE().getValue()) {
        groupNames = new String[] {"hadoop"};
      } else {
        throw e;
      }
    }
    if (user.equals(f.getOwner()) || user.equals(rootUserName())) {
      if (permission.getUserAction().implies(access)) {
        return true;
      }
    } else if (ArrayUtils.contains(groupNames, f.getGroup())) {
      if (permission.getGroupAction().implies(access)) {
        return true;
      }
    } else { // other class
      if (permission.getOtherAction().implies(access)) {
        return true;
      }
    }
    return false;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  private String checkHDFSPath(String path) {
    try {
      boolean checkHdfsPath = (boolean) StorageConfiguration.HDFS_PATH_PREFIX_CHECK_ON().getValue();
      if (checkHdfsPath) {
        boolean rmHdfsPrefix = (boolean) StorageConfiguration.HDFS_PATH_PREFIX_REMOVE().getValue();
        if (rmHdfsPrefix) {
          if (StringUtils.isBlank(path)) {
            return path;
          }
          if (path.startsWith(HDFS_PREFIX_WITHOUT_AUTH)) {
            // leave the first "/" in path
            int remainIndex = HDFS_PREFIX_WITHOUT_AUTH.length() - 1;
            if (logger.isDebugEnabled()) {
              logger.debug(
                  "checkHDFSPath  ori path : {}, after path : {}",
                  path,
                  path.substring(remainIndex));
            }
            return path.substring(remainIndex);
          } else if (path.startsWith(HDFS_PREFIX_WITH_AUTH)) {
            int remainIndex = HDFS_PREFIX_WITH_AUTH.length();
            String[] t1 = path.substring(remainIndex).split("/", 2);
            if (t1.length != 2) {
              logger.warn("checkHDFSPath Invalid path: " + path);
              return path;
            }
            if (logger.isDebugEnabled()) {
              logger.debug("checkHDFSPath  ori path : {}, after path : {}", path, "/" + t1[1]);
            }
            return "/" + t1[1];
          } else {
            return path;
          }
        }
      }
    } catch (Exception e) {
      logger.warn("checkHDFSPath error. msg : " + e.getMessage() + " ", e);
    }
    return path;
  }

  @Override
  public long getLength(FsPath dest) throws IOException {
    FileStatus fileStatus = fs.getFileStatus(new Path(checkHDFSPath(dest.getPath())));
    return fileStatus.getLen();
  }

  @Override
  public String getChecksumWithMD5(FsPath dest) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    if (!exists(dest)) {
      throw new IOException("directory or file not exists: " + path);
    }
    MD5MD5CRC32FileChecksum fileChecksum =
        (MD5MD5CRC32FileChecksum) fs.getFileChecksum(new Path(path));
    return fileChecksum.toString().split(":")[1];
  }

  @Override
  public String getChecksum(FsPath dest) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    if (!exists(dest)) {
      throw new IOException("directory or file not exists: " + path);
    }
    FileChecksum fileChecksum = fs.getFileChecksum(new Path(path));
    return fileChecksum.toString();
  }

  @Override
  public long getBlockSize(FsPath dest) throws IOException {
    String path = checkHDFSPath(dest.getPath());
    if (!exists(dest)) {
      throw new IOException("directory or file not exists: " + path);
    }
    return fs.getBlockSize(new Path(path));
  }

  @Override
  public List<FsPath> getAllFilePaths(FsPath path) throws IOException {
    FileStatus[] stat = fs.listStatus(new Path(checkHDFSPath(path.getPath())));
    List<FsPath> fsPaths = new ArrayList<>();
    for (FileStatus f : stat) {
      FsPath fsPath = fillStorageFile(new FsPath(f.getPath().toUri().getPath()), f);
      if (fs.isDirectory(f.getPath())) {
        fsPaths.addAll(getAllFilePaths(fsPath));
      } else {
        fsPaths.add(fsPath);
      }
    }
    return fsPaths;
  }
}
