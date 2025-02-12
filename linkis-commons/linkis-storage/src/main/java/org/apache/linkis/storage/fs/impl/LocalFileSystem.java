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
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.storage.domain.FsPathListWithError;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.TO_BE_UNKNOW;

public class LocalFileSystem extends FileSystem {

  private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystem.class);

  private Map<String, String> properties;
  private String group;

  /** File System abstract method start */
  @Override
  public String listRoot() throws IOException {
    return "/";
  }

  @Override
  public long getTotalSpace(FsPath dest) throws IOException {
    String path = dest.getPath();
    LOG.info("Get total space with path:" + path);
    return new File(path).getTotalSpace();
  }

  @Override
  public long getFreeSpace(FsPath dest) throws IOException {
    String path = dest.getPath();
    LOG.info("Get free space with path:" + path);
    return new File(path).getFreeSpace();
  }

  @Override
  public long getUsableSpace(FsPath dest) throws IOException {
    String path = dest.getPath();
    return new File(path).getUsableSpace();
  }

  @Override
  public boolean canExecute(FsPath dest) throws IOException {
    return can(
        dest,
        PosixFilePermission.OWNER_EXECUTE,
        PosixFilePermission.GROUP_EXECUTE,
        PosixFilePermission.OTHERS_EXECUTE);
  }

  @Override
  public boolean setOwner(FsPath dest, String user, String group) throws IOException {
    if (!StorageUtils.isIOProxy()) {
      LOG.info("io not proxy, setOwner skip");
      return true;
    }
    if (user != null) {
      setOwner(dest, user);
    }
    if (group != null) {
      setGroup(dest, group);
    }
    setGroup(dest, StorageConfiguration.STORAGE_USER_GROUP().getValue());
    return true;
  }

  @Override
  public boolean setOwner(FsPath dest, String user) throws IOException {
    LOG.info("Set owner with path:" + dest.getPath() + "and user:" + user);
    if (!StorageUtils.isIOProxy()) {
      LOG.info("io not proxy, setOwner skip");
      return true;
    }
    UserPrincipalLookupService lookupService =
        FileSystems.getDefault().getUserPrincipalLookupService();
    PosixFileAttributeView view =
        Files.getFileAttributeView(
            Paths.get(dest.getPath()), PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
    UserPrincipal userPrincipal = lookupService.lookupPrincipalByName(user);
    view.setOwner(userPrincipal);
    return true;
  }

  @Override
  public boolean setGroup(FsPath dest, String group) throws IOException {
    LOG.info("Set group with path:" + dest.getPath() + " and group:" + group);
    if (!StorageUtils.isIOProxy()) {
      LOG.info("io not proxy, setGroup skip");
      return true;
    }
    UserPrincipalLookupService lookupService =
        FileSystems.getDefault().getUserPrincipalLookupService();
    PosixFileAttributeView view =
        Files.getFileAttributeView(
            Paths.get(dest.getPath()), PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
    GroupPrincipal groupPrincipal = lookupService.lookupPrincipalByGroupName(group);
    view.setGroup(groupPrincipal);
    return true;
  }

  @Override
  public boolean mkdir(FsPath dest) throws IOException {
    return mkdirs(dest);
  }

  @Override
  public boolean mkdirs(FsPath dest) throws IOException {
    String path = dest.getPath();
    LOG.info("Try to mkdirs with path:" + path);
    File file = new File(path);
    // Create parent directories one by one and set their permissions to rwxrwxrwx.
    Stack<File> dirsToMake = new Stack<File>();
    dirsToMake.push(file);
    File parent = file.getParentFile();
    while (!parent.exists()) {
      dirsToMake.push(parent);
      parent = parent.getParentFile();
    }
    if (!canMkdir(new FsPath(parent.getPath()))) {
      throw new IOException("no permission to  mkdir path " + path);
    }
    while (!dirsToMake.empty()) {
      File dirToMake = dirsToMake.pop();
      if (dirToMake.mkdir()) {
        if (!user.equals(getOwner(dirToMake.getAbsolutePath()))) {
          setOwner(new FsPath(dirToMake.getAbsolutePath()), user, null);
        }
        setPermission(new FsPath(dirToMake.getAbsolutePath()), this.getDefaultFolderPerm());
      } else {
        return false;
      }
    }
    return true;
  }

  public boolean canMkdir(FsPath destParentDir) throws IOException {
    LOG.info("Try to check if the directory can be created with path:" + destParentDir.getPath());
    if (!StorageUtils.isIOProxy()) {
      LOG.debug("io not proxy, not check owner, just check if have write permission ");
      return this.canWrite(destParentDir);
    } else {
      LOG.info("io proxy, check owner ");
      if (!isOwner(destParentDir.getPath())) {
        throw new IOException(
            "current user:"
                + user
                + ", parentPath:"
                + destParentDir.getPath()
                + ", only owner can mkdir path "
                + destParentDir);
      }
      return true;
    }
  }

  @Override
  public boolean copy(String origin, String dest) throws IOException {
    File file = new File(dest);
    LOG.info("Try to copy file from:" + origin + " to dest:" + dest);
    if (!isOwner(file.getParent())) {
      throw new IOException("you have on permission to create file " + dest);
    }
    FileUtils.copyFile(new File(origin), file);
    try {
      setPermission(new FsPath(dest), this.getDefaultFilePerm());
      if (!user.equals(getOwner(dest))) {
        setOwner(new FsPath(dest), user, null);
      }
    } catch (Throwable e) {
      file.delete();
      if (e instanceof IOException) {
        throw (IOException) e;
      } else {
        throw new IOException(e);
      }
    }
    return true;
  }

  @Override
  public boolean setPermission(FsPath dest, String permission) throws IOException {
    LOG.info("Try to set permission dest with path:" + dest.getPath());
    if (!StorageUtils.isIOProxy()) {
      LOG.info("io not proxy, setPermission as parent.");
      try {
        PosixFileAttributes attr =
            Files.readAttributes(Paths.get(dest.getParent().getPath()), PosixFileAttributes.class);
        LOG.debug("parent permissions: attr: " + attr);
        Files.setPosixFilePermissions(Paths.get(dest.getPath()), attr.permissions());

      } catch (NoSuchFileException e) {
        LOG.warn("File or folder does not exist or file name is garbled(文件或者文件夹不存在或者文件名乱码)", e);
        throw new StorageWarnException(TO_BE_UNKNOW.getErrorCode(), e.getMessage());
      }
      return true;
    }
    String path = dest.getPath();
    if (StringUtils.isNumeric(permission)) {
      permission = FsPath.permissionFormatted(permission);
    }
    Files.setPosixFilePermissions(Paths.get(path), PosixFilePermissions.fromString(permission));
    return true;
  }

  @Override
  public FsPathListWithError listPathWithError(FsPath path) throws IOException {
    File file = new File(path.getPath());
    File[] files = file.listFiles();
    LOG.info("Try to list path:" + path.getPath() + " with error msg");
    if (files != null) {
      List<FsPath> rtn = new ArrayList();
      Set<String> fileNameSet = new HashSet<>();
      fileNameSet.add(path.getPath().trim());
      String message = "";
      for (File f : files) {
        try {
          if (fileNameSet.contains(f.getPath())) {
            LOG.info("File {} is duplicate", f.getPath());
            continue;
          } else {
            fileNameSet.add(f.getParent().trim());
          }
          rtn.add(get(f.getPath()));
        } catch (Throwable e) {
          LOG.warn("Failed to list path:", e);
          message =
              "The file name is garbled. Please go to the shared storage to delete it.(文件名存在乱码，请手动去共享存储进行删除):"
                  + e.getMessage();
        }
      }
      return new FsPathListWithError(rtn, message);
    }
    return null;
  }

  /**
   * FS interface method start
   *
   * <p>TODO Caching /etc/passwd information to the local as
   * object(将/etc/passwd的信息缓存到本地作为object进行判断)
   */
  @Override
  public void init(Map<String, String> properties) throws IOException {

    if (MapUtils.isNotEmpty(properties)) {
      this.properties = properties;
      if (properties.containsKey(StorageConfiguration.PROXY_USER().key())) {
        user = StorageConfiguration.PROXY_USER().getValue(properties);
      }
      group = StorageConfiguration.STORAGE_USER_GROUP().getValue(properties);
    } else {
      this.properties = new HashMap<String, String>();
    }
    if (FsPath.WINDOWS) {
      group = StorageConfiguration.STORAGE_USER_GROUP().getValue(properties);
    }
    if (StringUtils.isEmpty(group)) {
      String groupInfo;
      try {
        groupInfo = Utils.exec(new String[] {"id", user});
        LOG.info("Get groupinfo:" + groupInfo + "  with shell command: id " + user);
      } catch (RuntimeException e) {
        group = user;
        return;
      }
      String groups = groupInfo.substring(groupInfo.indexOf("groups=") + 7);
      group = groups.replaceAll("\\d+", "").replaceAll("\\(", "").replaceAll("\\)", "");
    }
  }

  @Override
  public String fsName() {
    return "file";
  }

  @Override
  public String rootUserName() {
    return StorageConfiguration.LOCAL_ROOT_USER().getValue();
  }

  @Override
  public FsPath get(String dest) throws IOException {
    FsPath fsPath = null;
    if (FsPath.WINDOWS) {
      fsPath = new FsPath("file://" + dest);
      return fsPath;
    } else {
      fsPath = new FsPath(dest);
    }
    LOG.info("Try to get FsPath with  path:" + fsPath.getPath());
    PosixFileAttributes attr = null;
    try {
      attr = Files.readAttributes(Paths.get(fsPath.getPath()), PosixFileAttributes.class);
    } catch (NoSuchFileException e) {
      LOG.warn("File or folder does not exist or file name is garbled(文件或者文件夹不存在或者文件名乱码)", e);
      throw new StorageWarnException(TO_BE_UNKNOW.getErrorCode(), e.getMessage());
    }

    fsPath.setIsdir(attr.isDirectory());
    fsPath.setModification_time(attr.lastModifiedTime().toMillis());
    fsPath.setAccess_time(attr.lastAccessTime().toMillis());
    fsPath.setLength(attr.size());
    fsPath.setPermissions(attr.permissions());
    fsPath.setOwner(attr.owner().getName());
    fsPath.setGroup(attr.group().getName());
    return fsPath;
  }

  @Override
  public InputStream read(FsPath dest) throws IOException {
    if (canRead(dest)) {
      return new FileInputStream(dest.getPath());
    }
    throw new IOException("you have no permission to read path " + dest.getPath());
  }

  @Override
  public OutputStream write(FsPath dest, boolean overwrite) throws IOException {
    String path = dest.getPath();
    if (new File(path).isDirectory()) {
      throw new IOException("you cannot write a directory " + path);
    }
    if (exists(dest) && canWrite(dest)) {
      return new FileOutputStream(path, !overwrite);
    } else if (canWrite(dest.getParent())) {
      return new FileOutputStream(path, !overwrite);
    }
    throw new IOException("you have no permission to write file " + path);
  }

  @Override
  public boolean create(String dest) throws IOException {
    LOG.info("try to create file with path:" + dest);
    File file = new File(dest);
    if (!isOwner(file.getParent())) {
      throw new IOException("you have on permission to create file " + dest);
    }
    file.createNewFile();
    try {
      setPermission(new FsPath(dest), this.getDefaultFilePerm());
      if (!user.equals(getOwner(dest))) {
        setOwner(new FsPath(dest), user, null);
      }
    } catch (Throwable e) {
      file.delete();
      if (e instanceof IOException) {
        throw (IOException) e;
      } else {
        throw new IOException(e);
      }
    }
    return true;
  }

  @Override
  public List<FsPath> list(FsPath path) throws IOException {
    File file = new File(path.getPath());
    File[] files = file.listFiles();
    LOG.info("Try to get file list with path:" + path.getPath());
    if (files != null) {
      List<FsPath> rtn = new ArrayList();
      for (File f : files) {
        rtn.add(get(f.getPath()));
      }
      return rtn;
    } else {
      return null;
    }
  }

  @Override
  public boolean canRead(FsPath dest) throws IOException {
    return can(
        dest,
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.GROUP_READ,
        PosixFilePermission.OTHERS_READ);
  }

  @Override
  public boolean canRead(FsPath dest, String user) throws IOException {
    return false;
  }

  @Override
  public boolean canWrite(FsPath dest) throws IOException {
    return can(
        dest,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.GROUP_WRITE,
        PosixFilePermission.OTHERS_WRITE);
  }

  @Override
  public boolean exists(FsPath dest) throws IOException {
    return new File(dest.getPath()).exists();
  }

  @Override
  public boolean delete(FsPath dest) throws IOException {
    String path = dest.getPath();
    if (isOwner(path)) {
      return new File(path).delete();
    }
    throw new IOException("only owner can delete file " + path);
  }

  @Override
  public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
    String path = oldDest.getPath();
    if (isOwner(path)) {
      return new File(path).renameTo(new File(newDest.getPath()));
    }
    throw new IOException("only owner can rename path " + path);
  }

  @Override
  public void close() throws IOException {}

  /** Utils method start */
  private boolean can(
      FsPath fsPath,
      PosixFilePermission userPermission,
      PosixFilePermission groupPermission,
      PosixFilePermission otherPermission)
      throws IOException {
    String path = fsPath.getPath();
    if (!exists(fsPath)) {
      throw new IOException("path " + path + " not exists.");
    }
    if (FsPath.WINDOWS) return true;
    PosixFileAttributes attr = Files.readAttributes(Paths.get(path), PosixFileAttributes.class);
    Set<PosixFilePermission> permissions = attr.permissions();
    if (attr.owner().getName().equals(user) && permissions.contains(userPermission)) {
      return true;
    }
    String pathGroup = attr.group().getName();
    LOG.debug("pathGroup: {}, group: {}, permissions: {}", pathGroup, group, permissions);
    if ((pathGroup.equals(user) || (group != null && group.contains(pathGroup)))
        && permissions.contains(groupPermission)) {
      return true;
    } else if (permissions.contains(otherPermission)) {
      return true;
    }
    return false;
  }

  private String getOwner(String path) throws IOException {
    PosixFileAttributes attr = Files.readAttributes(Paths.get(path), PosixFileAttributes.class);
    return attr.owner().getName();
  }

  @Override
  public long getLength(FsPath dest) throws IOException {
    String path = dest.getPath();
    LOG.info("Get file length with path:" + path);
    return new File(path).length();
  }

  @Override
  public String getChecksum(FsPath dest) {
    return null;
  }

  @Override
  public String getChecksumWithMD5(FsPath dest) {
    return null;
  }

  @Override
  public long getBlockSize(FsPath dest) {
    return 0L;
  }

  @Override
  public List<FsPath> getAllFilePaths(FsPath dest) {
    return new ArrayList<>();
  }
}
