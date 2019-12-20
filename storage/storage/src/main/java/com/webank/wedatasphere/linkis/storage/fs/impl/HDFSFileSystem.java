/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.storage.fs.impl;

import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.hadoop.common.utils.HDFSUtils;
import com.webank.wedatasphere.linkis.storage.domain.FsPathListWithError;
import com.webank.wedatasphere.linkis.storage.fs.FileSystem;
import com.webank.wedatasphere.linkis.storage.utils.StorageConfiguration;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by johnnwang on 10/15/18.
 */
public class HDFSFileSystem extends FileSystem {

    private org.apache.hadoop.fs.FileSystem fs = null;
    private Configuration conf = null;

    private static final Logger logger = LoggerFactory.getLogger(HDFSFileSystem.class);

    /**
     * File System abstract method start
     */


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
        return canAccess(dest, FsAction.EXECUTE);
    }

    @Override
    public boolean setOwner(FsPath dest, String user, String group) throws IOException {
        fs.setOwner(new Path(dest.getPath()), user, group);
        return true;
    }

    @Override
    public boolean setOwner(FsPath dest, String user) throws IOException {
        Path path = new Path(dest.getPath());
        fs.setOwner(path, user, fs.getFileStatus(path).getGroup());
        return true;
    }

    @Override
    public boolean setGroup(FsPath dest, String group) throws IOException {
        Path path = new Path(dest.getPath());
        fs.setOwner(path, fs.getFileStatus(path).getOwner(), group);
        return true;
    }

    @Override
    public boolean mkdir(FsPath dest) throws IOException {
        String path = dest.getPath();
        if (!canExecute(getParentPath(path))) {
            throw new IOException("You have not permission to access path " + path);
        }
        return fs.mkdirs(new Path(path));
    }

    @Override
    public boolean mkdirs(FsPath dest) throws IOException {
        String path = dest.getPath();
        FsPath parentPath = getParentPath(path);
        while (!exists(parentPath)) {
            parentPath = getParentPath(parentPath.getPath());
        }
        if (!canExecute(parentPath)) {
            throw new IOException("You have not permission to access path " + path);
        }
        return fs.mkdirs(new Path(path));
    }


    @Override
    public boolean setPermission(FsPath dest, String permission) throws IOException {
        String path = dest.getPath();
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
        FileStatus[] stat = fs.listStatus(new Path(path.getPath()));
        List<FsPath> fsPaths = new ArrayList<FsPath>();
        for (FileStatus f : stat) {
            fsPaths.add(fillStorageFile(new FsPath(f.getPath().toUri().getPath()), f));
        }
        if (fsPaths.isEmpty()) {
            return null;
        }
        return new FsPathListWithError(fsPaths, "");
    }

    /**
     * FS interface method start
     */

    public void init(Map<String, String> properties) throws IOException {
        if (MapUtils.isNotEmpty(properties) && properties.containsKey(StorageConfiguration.PROXY_USER().key())) {
            user = StorageConfiguration.PROXY_USER().getValue(properties);
        }

        if (user == null) {
            throw new IOException("User cannot be empty(用户不能为空)");
        }

        conf = HDFSUtils.getConfiguration(user);

        if (MapUtils.isNotEmpty(properties)) {
            for (String key : properties.keySet()) {
                String v = properties.get(key);
                if (StringUtils.isNotEmpty(v)) {
                    conf.set(key, v);
                }
            }

        }
        /*conf.set("fs.hdfs.impl.disable.cache","true");*/
        fs = HDFSUtils.getHDFSUserFileSystem(user, conf);
        if (fs == null) {
            throw new IOException("init HDFS FileSystem failed!");
        }
    }

    public String fsName() {
        return "hdfs";
    }


    public String rootUserName() {
        return StorageConfiguration.HDFS_ROOT_USER().getValue();
    }


    public FsPath get(String dest) throws IOException {
        return fillStorageFile(new FsPath(dest), fs.getFileStatus(new Path(dest)));
    }

    public InputStream read(FsPath dest) throws IOException {
        if (!canRead(dest)) {
            throw new IOException("You have not permission to access path " + dest.getPath());
        }
        return fs.open(new Path(dest.getPath()));
    }

    public OutputStream write(FsPath dest, boolean overwrite) throws IOException {
        String path = dest.getPath();
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
            return fs.create(new Path(path), true);
        }
    }

    public boolean create(String dest) throws IOException {
        if (!canExecute(getParentPath(dest))) {
            throw new IOException("You have not permission to access path " + dest);
        }
        return fs.createNewFile(new Path(dest));
    }

    public List<FsPath> list(FsPath path) throws IOException {
        FileStatus[] stat = fs.listStatus(new Path(path.getPath()));
        List<FsPath> fsPaths = new ArrayList<FsPath>();
        for (FileStatus f : stat) {
            fsPaths.add(fillStorageFile(new FsPath(f.getPath().toUri().getPath()), f));
        }
        return fsPaths;
    }

    public boolean canRead(FsPath dest) throws IOException {
        return canAccess(dest, FsAction.READ);
    }

    public boolean canWrite(FsPath dest) throws IOException {
        return canAccess(dest, FsAction.WRITE);
    }

    public boolean exists(FsPath dest) throws IOException {
        return fs.exists(new Path(dest.getPath()));
    }

    public boolean delete(FsPath dest) throws IOException {
        String path = dest.getPath();
        if (!isOwner(path)) {
            throw new IOException("You have not permission to delete path " + path);
        }
        return fs.delete(new Path(path), true);
    }

    public boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException {
        if (!isOwner(oldDest.getPath())) {
            throw new IOException("You have not permission to rename path " + oldDest.getPath());
        }
        return fs.rename(new Path(oldDest.getPath()), new Path(newDest.getPath()));
    }

    public void close() throws IOException {
        fs.close();
    }


    /**
     * Utils method start
     */

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
            logger.error("Failed to fill storage file：" + fileStatus.getPath(), e);
        }
        return fsPath;
    }

    private boolean canAccess(FsPath fsPath, FsAction access) throws IOException {
        String path = fsPath.getPath();
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
            if ((Boolean) com.webank.wedatasphere.linkis.common.conf.Configuration.IS_TEST_MODE().getValue()) {
                groupNames = new String[]{"hadoop"};
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
        } else { //other class
            if (permission.getOtherAction().implies(access)) {
                return true;
            }
        }
        return false;
    }

}
