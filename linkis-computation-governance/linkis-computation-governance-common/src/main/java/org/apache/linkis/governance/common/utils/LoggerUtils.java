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

package org.apache.linkis.governance.common.utils;

import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;

import java.util.Map;

import org.slf4j.MDC;

public class LoggerUtils {

  public static void setJobIdMDC(String jobId) {
    MDC.put(JobRequestConstants.JOB_ID(), jobId);
  }

  public static void setJobIdMDC(Map<String, Object> props) {
    if (GovernanceCommonConf.MDC_ENABLED()) {
      String jobId = JobUtils.getJobIdFromMap(props);
      MDC.put(JobRequestConstants.JOB_ID(), jobId);
    }
  }

  public static void removeJobIdMDC() {
    MDC.remove(JobRequestConstants.JOB_ID());
  }
}
