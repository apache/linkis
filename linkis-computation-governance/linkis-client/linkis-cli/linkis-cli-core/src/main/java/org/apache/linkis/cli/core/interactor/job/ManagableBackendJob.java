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

package org.apache.linkis.cli.core.interactor.job;

import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.job.JobStatus;
import org.apache.linkis.cli.common.entity.job.JobSubType;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;

/**
 * Backend support job-management, e.g. get status, get log, kill job etc. Then implement this
 * interface.
 */
public interface ManagableBackendJob extends Job {
  /**
   * since job management can diverge, we decide to assign it to lower-level implementation.
   * implementation should use {@link JobSubType} to decide which action to take, hence {@link
   * JobSubType} should not be null
   */
  void doManage() throws LinkisClientRuntimeException;

  /**
   * if execution is success. This can be different from {@link JobStatus} e.g. query job status,
   * job may be FAIL but execution is a asuccess
   */
  boolean isSuccess();
}
