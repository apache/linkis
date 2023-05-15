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

package org.apache.linkis.storage.factory.impl;

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.storage.factory.BuildFactory;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.fs.impl.HDFSFileSystem;
import org.apache.linkis.storage.io.IOMethodInterceptorFactory;
import org.apache.linkis.storage.utils.StorageUtils;

import org.springframework.cglib.proxy.Enhancer;

public class BuildHDFSFileSystem implements BuildFactory {

  /**
   * If it is a node with hdfs configuration file, then go to the proxy mode of hdfs, if not go to
   * io proxy mode 如果是有hdfs配置文件的节点，则走hdfs的代理模式，如果不是走io代理模式
   *
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
      // TODO Agent user(代理的用户)
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(HDFSFileSystem.class.getSuperclass());
      enhancer.setCallback(IOMethodInterceptorFactory.getIOMethodInterceptor(fsName()));
      fs = (FileSystem) enhancer.create();
    }
    fs.setUser(proxyUser);
    return fs;
  }

  @Override
  public Fs getFs(String user, String proxyUser, String label) {
    HDFSFileSystem fs = new HDFSFileSystem();
    fs.setUser(proxyUser);
    fs.setLabel(label);
    return fs;
  }

  @Override
  public String fsName() {
    return StorageUtils.HDFS;
  }
}
