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

package org.apache.linkis.filesystem.util;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.fs.FileSystem;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.PreDestroy;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.apache.linkis.filesystem.conf.WorkSpaceConfiguration.executorService;

public class FilesystemUtils {

  public static final ExecutorService executorService = Executors.newFixedThreadPool(10);

  public static boolean checkFilePermissions(String filePermission) {
    boolean result = false;
    if (StringUtils.isNumeric(filePermission)) {
      char[] ps = filePermission.toCharArray();
      int ownerPermissions = Integer.parseInt(String.valueOf(ps[0]));
      if (ownerPermissions >= 4) {
        result = true;
      }
    }
    return result;
  }

  public static void traverseFolder(FsPath fsPath, FileSystem fileSystem, Stack<FsPath> dirsToChmod)
      throws IOException {
    List<FsPath> list = fileSystem.list(fsPath);
    if (list == null) {
      return;
    }
    for (FsPath path : list) {
      if (path.isdir()) {
        traverseFolder(path, fileSystem, dirsToChmod);
      }
      dirsToChmod.push(path);
    }
  }

  @PreDestroy
  public void shutdown() {
    // 在应用程序关闭时优雅地关闭线程池
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60L, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
