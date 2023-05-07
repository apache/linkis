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

package org.apache.linkis.cli.application.interactor.validate;

import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.ValidateException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.job.jobcmd.JobCmdDesc;

import org.apache.commons.lang3.StringUtils;

public class JobCmdDescValidator {
  public void doValidation(JobCmdDesc desc) throws LinkisClientRuntimeException {
    boolean ok = true;
    StringBuilder reasonSb = new StringBuilder();
    if (StringUtils.isBlank(desc.getJobID())) {
      reasonSb.append("jobId cannot be empty or blank").append(System.lineSeparator());
      ok = false;
    }
    if (StringUtils.isBlank(desc.getUser())) {
      reasonSb.append("user cannot be empty or blank").append(System.lineSeparator());
      ok = false;
    }
    if (!ok) {
      throw new ValidateException(
          "VLD0008",
          ErrorLevel.ERROR,
          CommonErrMsg.ValidationErr,
          "LinkisJobMan validation failed. Reason: " + reasonSb.toString());
    }
  }
}
