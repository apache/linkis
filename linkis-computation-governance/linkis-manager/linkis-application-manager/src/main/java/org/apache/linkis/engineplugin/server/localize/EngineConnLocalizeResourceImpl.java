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

package org.apache.linkis.engineplugin.server.localize;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineConnLocalizeResourceImpl implements EngineConnLocalizeResource {
  private static final Logger logger =
      LoggerFactory.getLogger(EngineConnLocalizeResourceImpl.class);

  private final String filePath;
  private final String fileName;
  private final long lastModified;
  private final long fileSize;

  public EngineConnLocalizeResourceImpl(
      String filePath, String fileName, long lastModified, long fileSize) {
    this.filePath = filePath;
    this.fileName = fileName;
    this.lastModified = lastModified;
    this.fileSize = fileSize;
  }

  @Override
  public InputStream getFileInputStream() {
    try {
      return new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      logger.warn("getFileInputStream failed filePath:[{}]", filePath, e);
    }
    return null;
  }

  public String filePath() {
    return filePath;
  }

  public String fileName() {
    return fileName;
  }

  public long lastModified() {
    return lastModified;
  }

  public long fileSize() {
    return fileSize;
  }
}
