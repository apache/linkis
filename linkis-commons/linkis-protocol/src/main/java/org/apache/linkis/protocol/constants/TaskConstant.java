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

package org.apache.linkis.protocol.constants;

public interface TaskConstant {

  String UMUSER = "umUser";

  String SUBMIT_USER = "submitUser";

  String EXECUTE_USER = "executeUser";

  String TASKTYPE = "taskType";
  String STORAGETYPE = "storageType";
  String EXECUTIONCODE = "executionCode";
  String JOB_CONTENT = "jobContent";
  String TASK = "task";
  String TASKS = "tasks";
  String TASKID = "taskID";
  String PARAMS = "params";
  String FORMATCODE = "formatCode";
  String EXECUTEAPPLICATIONNAME = "executeApplicationName";
  String REQUESTAPPLICATIONNAME = "requestApplicationName";
  String SCRIPTPATH = "scriptPath";
  String SOURCE = "source";
  String RUNTYPE = "runType";
  String CACHE = "cache";
  String CACHE_EXPIRE_AFTER = "cacheExpireAfter";
  String READ_FROM_CACHE = "readFromCache";
  String READ_CACHE_BEFORE = "readCacheBefore";

  String PARAMS_VARIABLE = "variable";
  String PARAMS_CONFIGURATION = "configuration";
  String PARAMS_CONFIGURATION_STARTUP = "startup";
  String PARAMS_CONFIGURATION_RUNTIME = "runtime";
  String PARAMS_CONFIGURATION_SPECIAL = "special";

  String JOB_SUBMIT_TIME = "submitTime";
  String JOB_SCHEDULE_TIME = "scheduleTime";
  String JOB_RUNNING_TIME = "runningTime";
  String JOB_TO_ORCHESTRATOR = "jobToOrchestrator";
  String JOB_REQUEST_EC_TIME = "requestECTime";
  String JOB_SUBMIT_TO_EC_TIME = "jobToECTIme";
  String JOB_COMPLETE_TIME = "completeTime";
  String JOB_YARN_METRICS = "yarnMetrics";
  String JOB_YARNRESOURCE = "yarnResource";
  String JOB_CORE_PERCENT = "corePercent";
  String JOB_MEMORY_PERCENT = "memoryPercent";
  String JOB_CORE_RGB = "coreRGB";
  String JOB_MEMORY_RGB = "memoryRGB";
  String JOB_IS_REUSE = "isReuse";
  String JOB_ENGINECONN_MAP = "engineconnMap";
  String ENGINE_INSTANCE = "engineInstance";
  String TICKET_ID = "ticketId";
  String ENGINE_CONN_TASK_ID = "engineConnTaskId";
  String ENGINE_CONN_SUBMIT_TIME = "engineConnSubmitTime";
  String FAILOVER_FLAG = "failoverFlag";
  String DEBUG_ENBALE = "debug.enable";

  String PARAMS_DATA_SOURCE = "dataSources";

  String PARAMS_CONTEXT = "context";

  String LABELS = "labels";
  String EXECUTION_CONTENT = "executionContent";
  String CODE = "code";

  String REQUEST_IP = "requestIP";
  String MONITOR_LEVEL = "monitorLevel";
  String RECEIVER = "receiver";
  String SUB_SYSTEM_ID = "subSystemId";
  String EXTRA = "extra";
  String ECM_INSTANCE = "ecmInstance";
  String ENGINE_LOG_PATH = "engineLogPath";
}
