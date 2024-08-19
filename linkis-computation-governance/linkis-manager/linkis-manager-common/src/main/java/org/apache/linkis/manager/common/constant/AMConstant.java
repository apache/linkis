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

package org.apache.linkis.manager.common.constant;

public class AMConstant {

  public static final String EM_NAME = "engineManager";

  public static final String ENGINE_NAME = "engine";

  public static final int ENGINE_ERROR_CODE = 30001;

  public static final int EM_ERROR_CODE = 30002;

  public static final String PROCESS_MARK = "process";

  public static final String CLUSTER_PROCESS_MARK = "cluster_process";

  public static final String THREAD_MARK = "thread";

  public static final String START_REASON = "start_reason";

  public static final String EC_CAN_RETRY = "ec_can_try";

  public static final String EC_ASYNC_START_ID_KEY = "ecAsyncStartId";

  public static final String EC_ASYNC_START_MANAGER_INSTANCE_KEY = "managerInstance";

  /*
  result : starting,success,failed
   */
  public static final String EC_ASYNC_START_RESULT_KEY = "ecAsyncStartResult";

  /*
  default false
   */
  public static final String EC_SYNC_START_KEY = "ecSyncStart";

  public static final String EC_ASYNC_START_RESULT_SUCCESS = "success";

  public static final String EC_ASYNC_START_RESULT_FAIL = "failed";

  public static final String EC_ASYNC_START_RESULT_STARTING = "starting";

  public static final String EC_ASYNC_START_FAIL_RETRY_KEY = "canRetry";

  public static final String EC_ASYNC_START_FAIL_MSG_KEY = "failMsg";

  public static final String EC_METRICS_KEY = "ecMetrics";
}
