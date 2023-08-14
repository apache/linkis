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

package org.apache.linkis.cli.application.operator.ujes;

import org.apache.linkis.cli.application.entity.job.JobStatus;
import org.apache.linkis.cli.application.interactor.job.common.ResultSet;

import java.util.Date;

public interface LinkisOperResultAdapter {

  String getJobID();

  String getUser();

  String getProxyUser();

  JobStatus getJobStatus();

  String getStrongerExecId();

  Float getJobProgress();

  String getLogPath();

  String getResultLocation();

  String[] getResultSetPaths();

  Integer getErrCode();

  String getErrDesc();

  String getLog();

  Integer getNextLogLine();

  Boolean hasNextLogLine();

  ResultSet getResultContent();

  Boolean resultHasNextPage();

  String getInstance();

  String getUmUser();

  String getSimpleExecId();

  String getExecutionCode();

  String getEngineType();

  String getRunType();

  Long getCostTime();

  Date getCreatedTime();

  Date getUpdatedTime();

  Date getEngineStartTime();

  String getExecuteApplicationName();

  String getRequestApplicationName();
}
