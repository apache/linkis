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
import org.apache.linkis.storage.fs.impl.AzureBlobFileSystem;
import org.apache.linkis.storage.utils.StorageUtils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildAzureBlobFileSystem implements BuildFactory {
  private static final Logger LOG = LoggerFactory.getLogger(BuildAzureBlobFileSystem.class);

  @Override
  public Fs getFs(String user, String proxyUser) {
    AzureBlobFileSystem fs = new AzureBlobFileSystem();
    try {
      fs.init(null);
    } catch (IOException e) {
      LOG.warn("get file system failed", e);
    }
    fs.setUser(user);
    return fs;
  }

  @Override
  public Fs getFs(String user, String proxyUser, String label) {
    AzureBlobFileSystem fs = new AzureBlobFileSystem();
    try {
      fs.init(null);
    } catch (IOException e) {
      LOG.warn("get file system failed", e);
    }
    fs.setUser(user);
    return fs;
  }

  @Override
  public String fsName() {
    return StorageUtils.BLOB();
  }
}
