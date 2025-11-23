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

import org.apache.linkis.filesystem.entity.LogLevel;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WorkspaceUtilTest {

  @Test
  @DisplayName("staticCommonConstTest")
  public void staticCommonConstTest() {

    String infoReg = WorkspaceUtil.infoReg;
    String allReg = WorkspaceUtil.allReg;
    String errorReg = WorkspaceUtil.errorReg;
    String warnReg = WorkspaceUtil.warnReg;

    Assertions.assertNotNull(infoReg);
    Assertions.assertNotNull(allReg);
    Assertions.assertNotNull(errorReg);
    Assertions.assertNotNull(warnReg);
  }

  @Test
  @DisplayName("logMatchTest")
  public void logMatchTest() {
    String code =
        "2022-09-18 01:03:35.120 INFO  [SpringContextShutdownHook] com.netflix.util.concurrent.ShutdownEnabledTimer 67 cancel - Shutdown hook removed for: NFLoadBalancer-PingTimer-linkis-cg-linkismanager";
    LogLevel logLevel = new LogLevel(LogLevel.Type.INFO);
    List<Integer> logMatch = WorkspaceUtil.logMatch(code, logLevel);

    Assertions.assertTrue(logMatch.size() == 2);
  }

  @Test
  @DisplayName("suffixTuningTest")
  public void suffixTuningTest() {
    String path = "/home/hadoop/logs/linkis/apps";
    String tuningPath = WorkspaceUtil.suffixTuning(path);

    Assertions.assertNotNull(tuningPath);
  }

  //  @Test
  //  @DisplayName("isLogAdminTest")
  //  public void isLogAdminTest() {
  //    boolean logAdmin = WorkspaceUtil.isLogAdmin("hadoop");
  //    boolean admin = WorkspaceUtil.isLogAdmin("hadoops");
  //
  //    Assertions.assertTrue(logAdmin);
  //    Assertions.assertFalse(admin);
  //  }
}
