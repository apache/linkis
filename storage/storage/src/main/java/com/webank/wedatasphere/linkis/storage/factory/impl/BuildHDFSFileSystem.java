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

package com.webank.wedatasphere.linkis.storage.factory.impl;


import com.webank.wedatasphere.linkis.common.io.Fs;
import com.webank.wedatasphere.linkis.storage.factory.BuildFactory;
import com.webank.wedatasphere.linkis.storage.fs.FileSystem;
import com.webank.wedatasphere.linkis.storage.fs.impl.HDFSFileSystem;
import com.webank.wedatasphere.linkis.storage.io.IOMethodInterceptor;
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils;
import net.sf.cglib.proxy.Enhancer;


/**
 * Created by johnnwang on 10/17/18.
 */
public class BuildHDFSFileSystem implements BuildFactory {

    /**
     * If it is a node with hdfs configuration file, then go to the proxy mode of hdfs, if not go to io proxy mode
     * 如果是有hdfs配置文件的节点，则走hdfs的代理模式，如果不是走io代理模式
     * @param user
     * @param proxyUser
     * @return
     */
    @Override
    public Fs getFs(String user, String proxyUser) {
        FileSystem fs = null;

        if (StorageUtils.isHDFSNode()) {
            fs = new HDFSFileSystem();
        } else {
            //TODO Agent user(代理的用户)
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(HDFSFileSystem.class.getSuperclass());
            enhancer.setCallback(new IOMethodInterceptor(fsName()));
            fs = (FileSystem) enhancer.create();
        }
        fs.setUser(proxyUser);
        return fs;
    }

    @Override
    public String fsName() {
        return "hdfs";
    }
}
