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

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.hadoop.common.utils.HDFSUtils;
import org.apache.linkis.storage.conf.LinkisStorageConf;
import org.apache.linkis.storage.domain.FsPathListWithError;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.aliyun.oss.AliyunOSSFileSystem;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSSFileSystem extends FileSystem {

  public static final String OSS_PREFIX = "oss://";
  private org.apache.hadoop.fs.aliyun.oss.AliyunOSSFileSystem fs = null;
  private Configuration conf = null;

  private String label = null;

  private static final Logger logger = LoggerFactory.getLogger(OSSFileSystem.class);

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
    return true;
  }

  @Override
  public boolean setOwner(FsPath dest, String user, String group) throws IOException {
    return true;
  }

  @Override
  public boolean setOwner(FsPath dest, String user) throws IOException {
    return true;
  }

  @Override
  public boolean setGroup(FsPath dest, String group) throws IOException {
    return true;
  }

  @Override
  public boolean mkdir(FsPath dest) throws IOException {
    String path = checkOSSPath(dest.getPath());
    if (!canExecute(getParentPath(path))) {
      throw new IOException("You have not permission to access path " + path);
    }
    boolean result =
        fs.mkdirs(new Path(path), new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL));
    this.setPermission(new FsPath(path), this.getDefaultFolderPerm());
    return result;
  }

  @Override
  public boolean mkdirs(FsPath dest) throws IOException {
    String path = checkOSSPath(dest.getPath());
    FsPath parentPath = getParentPath(path);
    while (!exists(parentPath)) {
      parentPath = getParentPath(parentPath.getPath());
    }
    return fs.mkdirs(new Path(path), new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL));
  }

  @Override
  public boolean setPermission(FsPath dest, String permission) throws IOException {
    return true;
  }

  @Override
  public FsPathListWithError listPathWithError(FsPath path) throws IOException {
    FileStatus[] stat = fs.listStatus(new Path(checkOSSPath(path.getPath())));
    List<FsPath> fsPaths = new ArrayList<FsPath>();
    for (FileStatus f : stat) {
      fsPaths.add(
          fillStorageFile(
              new FsPath(
                  StorageUtils.OSS_SCHEMA
                      + StorageConfiguration.OSS_ACCESS_BUCKET_NAME.getValue()
                      + "/"
                      + f.getPath().toUri().getPath()),
              f));
    }
    if (fsPaths.isEmpty()) {
      return null;
    }
    return new FsPathListWithError(fsPaths, "");
  }

  /** FS interface method start */
  @Override
  public void init(Map<String, String> properties) throws IOException {
    // read origin configs from hadoop conf
    if (label == null
        && (boolean)
            org.apache.linkis.common.conf.Configuration.IS_MULTIPLE_YARN_CLUSTER().getValue()) {
      label = StorageConfiguration.LINKIS_STORAGE_FS_LABEL.getValue();
    }
    conf = HDFSUtils.getConfigurationByLabel(user, label);

    // origin configs
    Map<String, String> originProperties = Maps.newHashMap();
    originProperties.put("fs.oss.endpoint", StorageConfiguration.OSS_ENDPOINT.getValue());
    originProperties.put("fs.oss.accessKeyId", StorageConfiguration.OSS_ACCESS_KEY_ID.getValue());
    originProperties.put(
        "fs.oss.accessKeySecret", StorageConfiguration.OSS_ACCESS_KEY_SECRET.getValue());
    for (String key : originProperties.keySet()) {
      String value = originProperties.get(key);
      if (StringUtils.isNotBlank(value)) {
        conf.set(key, value);
      }
    }

    // additional configs
    if (MapUtils.isNotEmpty(properties)) {
      for (String key : properties.keySet()) {
        String v = properties.get(key);
        if (StringUtils.isNotBlank(v)) {
          conf.set(key, v);
        }
      }
    }
    fs = new AliyunOSSFileSystem();
    try {
      fs.initialize(
          new URI(StorageUtils.OSS_SCHEMA + StorageConfiguration.OSS_ACCESS_BUCKET_NAME.getValue()),
          conf);
    } catch (URISyntaxException e) {
      throw new IOException("init OSS FileSystem failed!");
    }
    if (fs == null) {
      throw new IOException("init OSS FileSystem failed!");
    }
  }

  @Override
  public String fsName() {
    return StorageUtils.OSS;
  }

  @Override
  public String rootUserName() {
    return null;
  }

  @Override
  public FsPath get(String dest) throws IOException {
    String realPath = checkOSSPath(dest);
    return fillStorageFile(new FsPath(realPath), fs.getFileStatus(new Path(realPath)));
  }

  @Override
  public InputStream read(FsPath dest) throws IOException {
    if (!canRead(dest)) {
      throw new IOException("You have not permission to access path " + dest.getPath());
    }
    return fs.open(new Path(dest.getPath()), 128);
  }

  @Override
  public OutputStream write(FsPath dest, boolean overwrite) throws IOException {
    String path = checkOSSPath(dest.getPath());
    if (!exists(dest)) {
      if (!canWrite(dest.getParent())) {
        throw new IOException("You have not permission to access path " + dest.getParent());
      }
    } else {
      if (!canWrite(dest)) {
        throw new IOException("You have not permission to access path " + path);
      }
    }
    OutputStream out =
        fs.create(
            new Path(path),
            new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
            overwrite,
            0,
            (short) 0,
            0L,
            null);
    this.setPermission(dest, this.getDefaultFilePerm());
    return out;
  }

  @Override
  public boolean create(String dest) throws IOException {
    if (!canExecute(getParentPath(dest))) {
      throw new IOException("You have not permission to access path " + dest);
    }
    // to do
    boolean result = fs.createNewFile(new Path(checkOSSPath(dest)));
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
            new Path(checkOSSPath(origin)),
            fs,
            new Path(checkOSSPath(dest)),
            false,
            true,
            fs.getConf());
    this.setPermission(new FsPath(dest), this.getDefaultFilePerm());
    return res;
  }

  @Override
  public List<FsPath> list(FsPath path) throws IOException {
    FileStatus[] stat = fs.listStatus(new Path(checkOSSPath(path.getPath())));
    List<FsPath> fsPaths = new ArrayList<FsPath>();
    for (FileStatus f : stat) {
      fsPaths.add(fillStorageFile(new FsPath(f.getPath().toUri().toString()), f));
    }
    return fsPaths;
  }

  @Override
  public boolean canRead(FsPath dest) throws IOException {
    return true;
  }

  @Override
  public boolean canWrite(FsPath dest) throws IOException {
    return true;
  }

  @Override
  public boolean exists(FsPath dest) throws IOException {
    try {
      return fs.exists(new Path(checkOSSPath(dest.getPath())));
    } catch (IOException e) {
      String message = e.getMessage();
      String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
      if ((message != null && message.matches(LinkisStorageConf.HDFS_FILE_SYSTEM_REST_ERRS))
          || (rootCauseMessage != null
              && rootCauseMessage.matches(LinkisStorageConf.HDFS_FILE_SYSTEM_REST_ERRS))) {
        logger.info("Failed to execute exists, retry", e);
        resetRootOSS();
        return fs.exists(new Path(checkOSSPath(dest.getPath())));
      } else {
        throw e;
      }
    }
  }

  private void resetRootOSS() throws IOException {
    if (fs != null) {
      synchronized (this) {
        if (fs != null) {
          fs.close();
          logger.warn(user + " FS reset close.");
          init(null);
        }
      }
    }
  }

  @Override
  public boolean delete(FsPath dest) throws IOException {
    String path = checkOSSPath(dest.getPath());
    return fs.delete(new Path(path), true);
  }

  @Override
  public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
    return fs.rename(
        new Path(checkOSSPath(oldDest.getPath())), new Path(checkOSSPath(newDest.getPath())));
  }

  @Override
  public void close() throws IOException {
    if (null != fs) {
      fs.close();
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
    return fsPath;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  private static String checkOSSPath(String path) {
    try {
      boolean checkOSSPath = (boolean) StorageConfiguration.OSS_PATH_PREFIX_CHECK_ON.getValue();
      if (checkOSSPath) {
        boolean rmOSSPrefix = (boolean) StorageConfiguration.OSS_PATH_PREFIX_REMOVE.getValue();
        if (rmOSSPrefix) {
          if (StringUtils.isBlank(path)) {
            return path;
          }
          if (path.startsWith(OSS_PREFIX)) {
            int remainIndex = OSS_PREFIX.length();
            String[] t1 = path.substring(remainIndex).split("/", 2);
            if (t1.length != 2) {
              logger.warn("checkOSSPath Invalid path: " + path);
              return path;
            }
            if (logger.isDebugEnabled()) {
              logger.debug("checkOSSPath  ori path : {}, after path : {}", path, "/" + t1[1]);
            }
            return "/" + t1[1];
          } else {
            return path;
          }
        }
      }
    } catch (Exception e) {
      logger.warn("checkOSSPath error. msg : " + e.getMessage() + " ", e);
    }
    return path;
  }
}
