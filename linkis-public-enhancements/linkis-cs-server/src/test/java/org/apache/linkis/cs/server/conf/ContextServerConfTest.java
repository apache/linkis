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

package org.apache.linkis.cs.server.conf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextServerConfTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String keywordScanPackage = ContextServerConf.KEYWORD_SCAN_PACKAGE;
    int csSchedulerMaxRunningJobs = ContextServerConf.CS_SCHEDULER_MAX_RUNNING_JOBS;
    long csSchedulerMaxAskExecutorTimes = ContextServerConf.CS_SCHEDULER_MAX_ASK_EXECUTOR_TIMES;
    long csSchedulerJobWaitMills = ContextServerConf.CS_SCHEDULER_JOB_WAIT_MILLS;
    String confLabel = ContextServerConf.CS_LABEL_SUFFIX;

    Assertions.assertNotNull(keywordScanPackage);
    Assertions.assertTrue(100 == csSchedulerMaxRunningJobs);
    Assertions.assertTrue(1000 == csSchedulerMaxAskExecutorTimes);
    Assertions.assertTrue(10000 == csSchedulerJobWaitMills);
  }
}
