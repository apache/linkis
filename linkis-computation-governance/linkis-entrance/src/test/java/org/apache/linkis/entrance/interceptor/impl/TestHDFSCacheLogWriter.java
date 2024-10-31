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

package org.apache.linkis.entrance.interceptor.impl;

import org.apache.linkis.entrance.log.Cache;
import org.apache.linkis.entrance.log.CacheLogReader;
import org.apache.linkis.entrance.log.HDFSCacheLogWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import oshi.util.FileUtil;

class TestHDFSCacheLogWriter {

  @Test
  void write() throws IOException {

    Cache cache = new Cache(5);
    String fileName = UUID.randomUUID().toString().replace("-", "") + "-test.log";
    String username = System.getProperty("user.name");
    String parentPath = System.getProperty("java.io.tmpdir") + File.separator + username;
    String logPath = parentPath + File.separator + fileName;
    System.out.println(logPath);
    String chartSet = "utf-8";

    File file = new File(parentPath);
    file.mkdirs();

    File logfile = new File(logPath);
    logfile.createNewFile(); // NOSONAR

    HDFSCacheLogWriter logWriter =
        new HDFSCacheLogWriter(
            // "D:\\DataSphere\\linkis\\docs\\test.log",
            logPath, chartSet, cache, username);

    String[] msgArr =
        new String[] {
          "1", "2", "3", "4", "5", "6",
          "7", "8", "9", "10", "11", "12",
          "13", "14", "15", "16", "17", "18",
          "19", "20", "21", "22"
        };

    List<String> msgList = new ArrayList<String>(Arrays.asList(msgArr));
    String msg = String.join("\n", msgList);

    logWriter.write(msg);

    CacheLogReader logReader = new CacheLogReader(logPath, chartSet, cache, username);
    String[] logs = new String[4];
    int fromLine = 1;
    int size = 1000;
    int retFromLine = logReader.readArray(logs, fromLine, size);
    Assertions.assertEquals(msgArr.length, retFromLine);
    logWriter.flush();
    List<String> list = FileUtil.readFile(logPath);
    String res = String.join("\n", list);
    Assertions.assertEquals(res, msg);
  }

  @Test
  void write2() throws IOException, InterruptedException {

    Cache cache = new Cache(30);
    String fileName = UUID.randomUUID().toString().replace("-", "") + "-test.log";
    String username = System.getProperty("user.name");
    String parentPath = System.getProperty("java.io.tmpdir") + File.separator + username;
    String logPath = parentPath + File.separator + fileName;
    System.out.println(logPath);
    String chartSet = "utf-8";

    File file = new File(parentPath);
    file.mkdirs();

    File logfile = new File(logPath);
    logfile.createNewFile(); // NOSONAR

    HDFSCacheLogWriter logWriter =
        new HDFSCacheLogWriter(
            // "D:\\DataSphere\\linkis\\docs\\test.log",
            logPath, chartSet, cache, username);

    String[] msgArr =
        new String[] {
          "1", "2", "3", "4", "5", "6",
          "7", "8", "9", "10", "11", "12",
          "13", "14", "15", "16", "17", "18",
          "19", "20", "21", "22"
        };

    List<String> msgList = new ArrayList<String>(Arrays.asList(msgArr));
    String msg = String.join("\n", msgList);

    logWriter.write(msg);

    Thread.sleep(4 * 1000); // NOSONAR

    logWriter.write(msg);

    CacheLogReader logReader = new CacheLogReader(logPath, chartSet, cache, username);
    String[] logs = new String[4];
    int fromLine = 1;
    int size = 1000;
    int retFromLine = logReader.readArray(logs, fromLine, size);
    Assertions.assertEquals(msgArr.length * 2, retFromLine);
    Assertions.assertEquals(msg + "\n" + msg, logs[3]);

    logWriter.flush();

    List<String> list = FileUtil.readFile(logPath);
    String res = String.join("\n", list);
    Assertions.assertEquals(msg + "\n" + msg, res);
  }
}
