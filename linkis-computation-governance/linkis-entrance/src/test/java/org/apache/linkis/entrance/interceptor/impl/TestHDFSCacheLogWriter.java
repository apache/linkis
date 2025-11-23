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
import org.apache.linkis.entrance.log.HDFSCacheLogWriter;

import org.apache.commons.lang3.StringUtils;

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
    String logPath = System.getProperty("java.io.tmpdir") + File.separator + fileName;
    System.out.println(logPath);
    String chartSet = "utf-8";
    String username = System.getProperty("user.name");

    File file = new File(logPath);
    file.createNewFile();

    HDFSCacheLogWriter logWriter =
        new HDFSCacheLogWriter(
            // "D:\\DataSphere\\linkis\\docs\\test.log",
            logPath, chartSet, cache, username);

    String[] msgArr =
        new String[] {
          "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
          "17", "18"
        };

    List<String> msgList = new ArrayList<String>(Arrays.asList(msgArr));
    String msg = String.join("\n", msgList);

    logWriter.write(msg);
    logWriter.flush();

    List<String> list = FileUtil.readFile(logPath);
    String res = String.join("\n", list);

    res = res.replace("\n\n", "\n");
    res = StringUtils.strip(res, " \n");
    Assertions.assertEquals(res, msg);
  }
}
