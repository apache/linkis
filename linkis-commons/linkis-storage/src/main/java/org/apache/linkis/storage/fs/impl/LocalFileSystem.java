/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;


public class LocalFileSystem extends FileSystem {


    private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystem.class);

    private Map<String, String> properties;
    private String group;

    /**
     *File System abstract method start
     */

    @Override
    public String listRoot() throws IOException {
        return "/";
    }

    @Override
    public long getTotalSpace(FsPath dest) throws IOException {
        String path = dest.getPath();
        return new File(path).getTotalSpace();
    }

    @Override
    public long getFreeSpace(FsPath dest) throws IOException {
        String path = dest.getPath();
        return new File(path).getFreeSpace();
    }

    @Override
    public long getUsableSpace(FsPath dest) throws IOException {
        String path = dest.getPath();
        return new File(path).getUsableSpace();
    }

    @Override
    public boolean canExecute(FsPath dest) throws IOException {
        return  can(dest, PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE);
    }

    @Override
    public boolean setOwner(FsPath dest, String user, String group) throws IOException {
        if (!StorageUtils.isIOProxy()){
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
        if (!StorageUtils.isIOProxy()){
            LOG.info("io not proxy, setOwner skip");
            return true;
        }
        UserPrincipalLookupService lookupService =
                FileSystems.getDefault().getUserPrincipalLookupService();
        PosixFileAttributeView view =
                Files.getFileAttributeView(Paths.get(dest.getPath()), PosixFileAttributeView.class,
                        LinkOption.NOFOLLOW_LINKS);
        UserPrincipal userPrincipal = lookupService.lookupPrincipalByName(user);
        view.setOwner(userPrincipal);
        return true;
    }

    @Override
    public boolean setGroup(FsPath dest, String group) throws IOException {
        if (!StorageUtils.isIOProxy()){
            LOG.info("io not proxy, setGroup skip");
            return true;
        }
        UserPrincipalLookupService lookupService =
                FileSystems.getDefault().getUserPrincipalLookupService();
        PosixFileAttributeView view =
                Files.getFileAttributeView(Paths.get(dest.getPath()), PosixFileAttributeView.class,
                        LinkOption.NOFOLLOW_LINKS);
        GroupPrincipal groupPrincipal = lookupService.lookupPrincipalByGroupName(group);
        view.setGroup(groupPrincipal);
        return true;
    }

    @Override
    public boolean mkdir(FsPath dest) throws IOException {
        return  mkdirs(dest);
    }

    @Override
    public boolean mkdirs(FsPath dest) throws IOException {
        String path = dest.getPath();
        File file = new File(path);
        // Create parent directories one by one and set their permissions to rwxrwxrwx.
        Stack<File> dirsToMake = new Stack<File>();
        dirsToMake.push(file);
        File parent = file.getParentFile();
        while (!parent.exists()) {
            dirsToMake.push(parent);
            parent = parent.getParentFile();
        }
        if(!canMkdir(new FsPath(parent.getPath()))) {
            throw new IOException("no permission to  mkdir path " + path);
        }
        while (!dirsToMake.empty()) {
            File dirToMake = dirsToMake.pop();
            if (dirToMake.mkdir()) {
                if(!user.equals(getOwner(dirToMake.getAbsolutePath()))) {
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
        if (!StorageUtils.isIOProxy()){
            LOG.debug("io not proxy, not check ownerer, just check if hava write permission ");
            return this.canWrite(destParentDir);
        }else{
            LOG.info("io proxy, check owner ");
            if(!isOwner(destParentDir.getPath())) {
                throw new IOException("current user:" + user + ", parentPath:"+ destParentDir.getPath() +", only owner can mkdir path " + destParentDir);
            }
        }
        return false;
    }

    @Override
    public boolean copy(String origin, String dest) throws IOException {
        File file = new File(dest);
        if(!isOwner(file.getParent())) {
            throw new IOException("you have on permission to create file " + dest);
        }
        FileUtils.copyFile(new File(origin), file);
        try {
            setPermission(new FsPath(dest), this.getDefaultFilePerm());
            if(!user.equals(getOwner(dest))) {
                setOwner(new FsPath(dest), user, null);
            }
        } catch (Throwable e) {
            file.delete();
            if(e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
        return true;
    }

    @Override
    public boolean setPermission(FsPath dest, String permission) throws IOException {
        if (!StorageUtils.isIOProxy()){
            LOG.info("io not proxy, setPermission as parent.");
            try {
                PosixFileAttributes attr  = Files.readAttributes(Paths.get(dest.getParent().getPath()), PosixFileAttributes.class);
                LOG.debug("parent permissions: attr: " + attr);
                Files.setPosixFilePermissions(Paths.get(dest.getPath()), attr.permissions());

            }catch (NoSuchFileException e){
                LOG.error("File or folder does not exist or file name is garbled(文件或者文件夹不存在或者文件名乱码)",e);
                throw new StorageWarnException(51001,e.getMessage());
            }
            return true;

        }
        String path = dest.getPath();
        if(StringUtils.isNumeric(permission)) {
            permission = FsPath.permissionFormatted(permission);
        }
        Files.setPosixFilePermissions(Paths.get(path), PosixFilePermissions.fromString(permission));
        return true;

    }


    @Override
    public FsPathListWithError listPathWithError(FsPath path) throws IOException {
        File file = new File(path.getPath());
        File[] files = file.listFiles();
        if (files != null) {
            List<FsPath> rtn = new ArrayList();
            String message = "";
            for (File f : files) {
                try {
                    rtn.add(get(f.getPath()));
                }catch (Throwable e){
                    LOG.error("Failed to list path:",e);
                    message = "The file name is garbled. Please go to the shared storage to delete it.(文件名存在乱码，请手动去共享存储进行删除):" + e.getMessage();
                }
            }
            return new FsPathListWithError(rtn, message);
        }
        return null;
    }

    /**
     *FS interface method start
     *
     * TODO Caching /etc/passwd information to the local as object(将/etc/passwd的信息缓存到本地作为object进行判断)
     */

    public void init(Map<String, String> properties) throws IOException {

        if(MapUtils.isNotEmpty(properties)) {
            this.properties = properties;
            if (properties.containsKey(StorageConfiguration.PROXY_USER().key())) {
                user = StorageConfiguration.PROXY_USER().getValue(properties);
            }
            group = StorageConfiguration.STORAGE_USER_GROUP().getValue(properties);
        } else{
            this.properties = new HashMap<String, String>();
        }
        if(StringUtils.isEmpty(group)) {
            String groupInfo;
            try {
                groupInfo = Utils.exec(new String[]{"id", user});
            } catch (RuntimeException e) {
                group = user;
                return;
            }
            String groups = groupInfo.substring(groupInfo.indexOf("groups=") + 7);
            group = groups.replaceAll("\\d+", "").replaceAll("\\(", "").replaceAll("\\)", "");
        }

    }

    public String fsName() {
        return "file";
    }

    public String rootUserName() {
        return StorageConfiguration.LOCAL_ROOT_USER().getValue();
    }

    public FsPath get(String dest) throws IOException {
        FsPath fsPath = null;
        if(FsPath.WINDOWS){
            fsPath = new FsPath("file://" +  dest);
            return fsPath;
        } else {
            fsPath = new FsPath(dest);
        }

        PosixFileAttributes attr = null;
        try {
            attr  = Files.readAttributes(Paths.get(fsPath.getPath()), PosixFileAttributes.class);
        }catch (NoSuchFileException e){
            LOG.error("File or folder does not exist or file name is garbled(文件或者文件夹不存在或者文件名乱码)",e);
            throw new StorageWarnException(51001,e.getMessage());
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

    public InputStream read(FsPath dest) throws IOException {
        if(canRead(dest)) {
            return new FileInputStream(dest.getPath());
        }
        throw new IOException("you have no permission to read path " + dest.getPath());
    }

    public OutputStream write(FsPath dest, boolean overwrite) throws IOException {
        String path  = dest.getPath();
        if(new File(path).isDirectory()) {
            throw new IOException("you cannot write a directory " + path);
        }
        if(exists(dest) && canWrite(dest)) {
            return new FileOutputStream(path, !overwrite);
        } else if(canWrite(dest.getParent())) {
            return new FileOutputStream(path, !overwrite);
        }
        throw new IOException("you have no permission to write file " + path);
    }


    public boolean create(String dest) throws IOException {

        File file = new File(dest);
        if(!isOwner(file.getParent())) {
            throw new IOException("you have on permission to create file " + dest);
        }
        file.createNewFile();
        try {
            setPermission(new FsPath(dest), this.getDefaultFilePerm());
            if(!user.equals(getOwner(dest))) {
                setOwner(new FsPath(dest), user, null);
            }
        } catch (Throwable e) {
            file.delete();
            if(e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
        return true;
    }

    public List<FsPath> list(FsPath path) throws IOException {
        File file = new File(path.getPath());
        File[] files = file.listFiles();
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

    public boolean canRead(FsPath dest) throws IOException {
        return can(dest, PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ);
    }

    public boolean canWrite(FsPath dest) throws IOException {
        return can(dest, PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_WRITE, PosixFilePermission.OTHERS_WRITE);
    }

    public boolean exists(FsPath dest) throws IOException {
        return new File(dest.getPath()).exists();
    }

    public boolean delete(FsPath dest) throws IOException {
        String path = dest.getPath();
        if(isOwner(path)) {
            return new File(path).delete();
        }
        throw new IOException("only owner can delete file " + path);
    }

    public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
        String path = oldDest.getPath();
        if(isOwner(path)) {
            return new File(path).renameTo(new File(newDest.getPath()));
        }
        throw new IOException("only owner can rename path " + path);
    }

    public void close() throws IOException {

    }


    /**
     *Utils method start
     */

    private boolean can(FsPath fsPath, PosixFilePermission userPermission, PosixFilePermission groupPermission,
                        PosixFilePermission otherPermission) throws IOException {
        String path = fsPath.getPath();
        if(!exists(fsPath)) {
            throw new IOException("path " + path + " not exists.");
        }
        if(FsPath.WINDOWS) return true;
        PosixFileAttributes attr =
                Files.readAttributes(Paths.get(path), PosixFileAttributes.class);
        Set<PosixFilePermission> permissions = attr.permissions();
        if(attr.owner().getName().equals(user) && permissions.contains(userPermission)) {
            return true;
        }
        String pathGroup = attr.group().getName();
        if((pathGroup.equals(user) || group.contains(pathGroup)) && permissions.contains(groupPermission)) {
            return true;
        } else if(permissions.contains(otherPermission)) {
            return true;
        }
        return false;
    }

    private String getOwner(String path) throws IOException {
        PosixFileAttributes attr =
                Files.readAttributes(Paths.get(path), PosixFileAttributes.class);
        return attr.owner().getName();
    }
}