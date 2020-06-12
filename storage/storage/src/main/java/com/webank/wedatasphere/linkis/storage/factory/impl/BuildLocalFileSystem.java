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
import com.webank.wedatasphere.linkis.storage.fs.impl.LocalFileSystem;
import com.webank.wedatasphere.linkis.storage.io.IOMethodInterceptor;
import com.webank.wedatasphere.linkis.storage.utils.StorageConfiguration;
import net.sf.cglib.proxy.Enhancer;

/**
 * Created by johnnwang on 10/17/18.
 */
public class BuildLocalFileSystem implements BuildFactory {

    @Override
    public Fs getFs(String user, String proxyUser){
        FileSystem fs = null;
        if(user.equals(proxyUser)){
            if((Boolean) StorageConfiguration.IS_SHARE_NODE().getValue()){
               fs = new  LocalFileSystem();
            } else {
               fs =  getProxyFs();
            }
        } else {
            fs =  getProxyFs();
        }
        fs.setUser(proxyUser);
        return  fs;
    }

    private FileSystem getProxyFs(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(LocalFileSystem.class.getSuperclass());
        enhancer.setCallback(new IOMethodInterceptor(fsName()));
        return (FileSystem)enhancer.create();
    }

    @Override
    public String fsName() {
        return "file";
    }
}
