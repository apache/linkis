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

package org.apache.linkis.engineconn.computation.executor.conf

import org.apache.linkis.common.conf.{ByteType, CommonVars}

object ComputationExecutorConf {

  val ENGINE_RESULT_SET_MAX_CACHE =
    CommonVars("wds.linkis.engine.resultSet.cache.max", new ByteType("0k"))

  val ENGINE_LOCK_DEFAULT_EXPIRE_TIME =
    CommonVars("wds.linkis.engine.lock.expire.time", 2 * 60 * 1000)

  val ENGINE_MAX_TASK_EXECUTE_NUM = CommonVars("wds.linkis.engineconn.max.task.execute.num", 0)

  val ENGINE_PROGRESS_FETCH_INTERVAL =
    CommonVars("wds.linkis.engineconn.progresss.fetch.interval-in-seconds", 5)

  val UDF_LOAD_FAILED_IGNORE = CommonVars("wds.linkis.engineconn.udf.load.ignore", true)

  val FUNCTION_LOAD_FAILED_IGNORE = CommonVars("wds.linkis.engineconn.function.load.ignore", true)

  val TASK_IGNORE_UNCOMPLETED_STATUS =
    CommonVars("wds.linkis.engineconn.task.ignore.uncompleted.status", true).getValue

  val ENGINE_CONCURRENT_THREAD_NUM = CommonVars("wds.linkis.engineconn.concurrent.thread.num", 20)

  val ASYNC_EXECUTE_MAX_PARALLELISM = CommonVars("wds.linkis.engineconn.max.parallelism", 300)

  val ASYNC_SCHEDULER_MAX_RUNNING_JOBS =
    CommonVars("wds.linkis.engineconn.async.group.max.running", 10).getValue

  val DEFAULT_COMPUTATION_EXECUTORMANAGER_CLAZZ = CommonVars(
    "wds.linkis.default.computation.executormanager.clazz",
    "org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorManagerImpl"
  )

  val UPSTREAM_MONITOR_ECTASK_SHOULD_START =
    CommonVars("linkis.upstream.monitor.ectask.should.start", true).getValue

  val UPSTREAM_MONITOR_WRAPPER_ENTRIES_SURVIVE_THRESHOLD_SEC =
    CommonVars("linkis.upstream.monitor.wrapper.entries.survive.time.sec", 86400).getValue

  val UPSTREAM_MONITOR_ECTASK_ENTRANCE_THRESHOLD_SEC =
    CommonVars("linkis.upstream.monitor.ectask.entrance.threshold.sec", 15).getValue

  val HIVE_RESULTSET_USE_TABLE_NAME = CommonVars("hive.resultset.use.unique.column.names", false)

  val JOB_ID_TO_ENV_KEY = CommonVars("wds.linkis.ec.job.id.env.key", "LINKIS_JOB_ID").getValue

  val TASK_ASYNC_MAX_THREAD_SIZE =
    CommonVars("linkis.ec.task.execution.async.thread.size", 50).getValue

  val TASK_SUBMIT_WAIT_TIME_MS = CommonVars("linkis.ec.task.submit.wait.time.ms", 2L).getValue
}
