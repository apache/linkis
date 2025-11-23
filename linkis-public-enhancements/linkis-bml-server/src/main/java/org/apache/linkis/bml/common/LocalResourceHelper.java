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

package org.apache.linkis.bml.common;

import org.apache.linkis.bml.conf.BmlServerConfiguration;
import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.utils.FileSystemUtils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Description:本地存储bml文件 */
public class LocalResourceHelper implements ResourceHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalResourceHelper.class);

  private static final String LOCAL_SCHEMA = "file://";

  @Override
  public long upload(
      String path,
      String user,
      InputStream inputStream,
      StringBuilder stringBuilder,
      boolean overwrite)
      throws UploadResourceException {
    OutputStream outputStream = null;
    InputStream is0 = null;
    InputStream is1 = null;
    long size = 0;
    Fs fileSystem = null;
    try {
      FsPath fsPath = new FsPath(path);
      fileSystem = FSFactory.getFsByProxyUser(fsPath, user);
      fileSystem.init(new HashMap<String, String>());
      if (!fileSystem.exists(fsPath)) {
        FileSystemUtils.createNewFile(fsPath, user, true);
      }
      byte[] buffer = new byte[1024];
      long beforeSize = -1;
      is0 = fileSystem.read(fsPath);
      int ch0 = 0;
      while ((ch0 = is0.read(buffer)) != -1) {
        beforeSize += ch0;
      }
      outputStream = fileSystem.write(fsPath, overwrite);
      int ch = 0;
      MessageDigest md5Digest = DigestUtils.getMd5Digest();
      while ((ch = inputStream.read(buffer)) != -1) {
        md5Digest.update(buffer, 0, ch);
        outputStream.write(buffer, 0, ch);
        size += ch;
      }
      if (stringBuilder != null) {
        stringBuilder.append(Hex.encodeHexString(md5Digest.digest()));
      }

      // Get all the bytes of the file by the file name. In this way, the wrong updated is
      // avoided.
      long afterSize = -1;
      is1 = fileSystem.read(fsPath);
      int ch1 = 0;
      while ((ch1 = is1.read(buffer)) != -1) {
        afterSize += ch1;
      }
      size = Math.max(size, afterSize - beforeSize);
    } catch (final IOException e) {
      LOGGER.error("{} write to {} failed, reason is, IOException:", user, path, e);
      UploadResourceException uploadResourceException = new UploadResourceException();
      uploadResourceException.initCause(e);
      throw uploadResourceException;
    } catch (final Throwable t) {
      LOGGER.error("{} write to {} failed, reason is", user, path, t);
      UploadResourceException uploadResourceException = new UploadResourceException();
      uploadResourceException.initCause(t);
      throw uploadResourceException;
    } finally {
      IOUtils.closeQuietly(outputStream);
      if (fileSystem != null) {
        try {
          fileSystem.close();
        } catch (IOException e) {
          LOGGER.error("close filesystem failed", e);
        }
      }
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(is0);
      IOUtils.closeQuietly(is1);
    }
    return size;
  }

  @Override
  public void update(String path) {}

  @Override
  public void getResource(String path, int start, int end) {}

  @Override
  public String generatePath(String user, String fileName, Map<String, Object> properties) {
    String resourceHeader = (String) properties.get("resourceHeader");
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    String dateStr = format.format(new Date());
    if (StringUtils.isNotEmpty(resourceHeader)) {
      return getSchema()
          + BmlServerConfiguration.BML_LOCAL_PREFIX().getValue()
          + "/"
          + user
          + "/"
          + dateStr
          + "/"
          + resourceHeader
          + "/"
          + fileName;
    } else {
      return getSchema()
          + BmlServerConfiguration.BML_LOCAL_PREFIX().getValue()
          + "/"
          + user
          + "/"
          + dateStr
          + "/"
          + fileName;
    }
  }

  @Override
  public String getSchema() {
    return LOCAL_SCHEMA;
  }

  @Override
  public boolean checkIfExists(String path, String user) throws IOException {
    Fs fileSystem = FSFactory.getFsByProxyUser(new FsPath(path), user);
    fileSystem.init(new HashMap<String, String>());
    try {
      return fileSystem.exists(new FsPath(path));
    } finally {
      fileSystem.close();
    }
  }

  @Override
  public boolean checkBmlResourceStoragePrefixPathIfChanged(String path) {
    String prefixPath = getSchema() + BmlServerConfiguration.BML_LOCAL_PREFIX().getValue();
    return !path.startsWith(prefixPath);
  }
}
