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

package com.webank.wedatasphere.linkis.storage.fs;

import com.webank.wedatasphere.linkis.common.io.Fs;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.storage.domain.FsPathListWithError;

import java.io.File;
import java.io.IOException;

/**
 * Created by johnnwang on 10/15/18.
 */
public abstract class FileSystem implements Fs{

    protected  String user;


    public abstract String listRoot() throws IOException;

    public abstract long getTotalSpace(FsPath dest) throws IOException;

    public abstract long getFreeSpace(FsPath dest) throws IOException;

    public abstract long getUsableSpace(FsPath dest) throws IOException;


    public abstract boolean canExecute(FsPath dest) throws IOException;

    public abstract boolean setOwner(FsPath dest, String user, String group) throws IOException;

    public abstract boolean setOwner(FsPath dest, String user) throws IOException;

    public abstract boolean setGroup(FsPath dest, String group) throws IOException;

    public abstract boolean mkdir(FsPath dest) throws IOException;

    public abstract boolean mkdirs(FsPath dest) throws IOException;

    public FsPathListWithError listPathWithError(FsPath path) throws IOException {
        return null;
    }

    public   boolean createNewFile(FsPath dest) throws IOException{
        return create(dest.getPath());
    }

    /**
     * Set permissions for a path(设置某个路径的权限)
     * @param dest path(路径)
     * @param permission Permissions, such as rwxr-x---etc.(权限，如rwxr-x---等)
     * @throws IOException Setting a failure throws an exception, or throws an exception if the user is not owner(设置失败抛出异常，或者如果该用户不是owner，也会抛出异常)
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
        if(File.separatorChar == '/') {
            parentPath = new File(path).getParent();
        } else {
            parentPath = path.substring(0, path.lastIndexOf("/"));
        }
        return new FsPath(parentPath);
    }

    public boolean isOwner(String dest) throws IOException {
        FsPath fsPath = get(dest);
        return user.equals(fsPath.getOwner()) || user.equals(rootUserName());
    }

}
