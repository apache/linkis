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

package org.apache.linkis.cli.common.entity.job;

import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.entity.command.CmdType;
import org.apache.linkis.cli.common.entity.operator.JobOperator;
import org.apache.linkis.cli.common.entity.present.PresentWay;

public interface Job {
  /** Linkis-cli specified id, not server-side returned job-id/task-id */
  String getCid();

  /**
   * Command Type for this Job, should be able to use this to find out corresponding {@link
   * CmdTemplate}
   */
  CmdType getCmdType();

  /** specifies which kind of sub-execution: e.g. jobManagement: status/list/log/kill; */
  JobSubType getSubType();

  /**
   * input-param/config will be stored in JobDescription information contained by this
   * data-structure should be passed to server
   */
  JobDescription getJobDesc();

  /**
   * data generated during execution(e.g. job status, job id, log, result etc.) is stored here
   * information contained by this data-structure can be further passed to server
   */
  JobData getJobData();

  /** operates lower level components(usually encapsulates a client) */
  JobOperator getJobOperator();

  /** decide how result should be presented */
  PresentWay getPresentWay();
}
