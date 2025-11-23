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
    CommonVars("wds.linkis.engine.resultSet.cache.max", new ByteType("0k"), "Result set cache size")

  val ENGINE_LOCK_DEFAULT_EXPIRE_TIME =
    CommonVars("wds.linkis.engine.lock.expire.time", 2 * 60 * 1000, "lock expiration time")

  val ENGINE_MAX_TASK_EXECUTE_NUM = CommonVars(
    "wds.linkis.engineconn.max.task.execute.num",
    50,
    "Maximum number of tasks executed by the synchronization EC"
  )

  val PRINT_TASK_PARAMS_SKIP_KEYS = CommonVars(
    "linkis.engineconn.print.task.params.skip.keys",
    "jobId,wds.linkis.rm.yarnqueue",
    "skip to print params key at job logs"
  )

  val ENGINE_PROGRESS_FETCH_INTERVAL =
    CommonVars(
      "wds.linkis.engineconn.progresss.fetch.interval-in-seconds",
      5,
      "Progress information push interval"
    )

  val UDF_LOAD_FAILED_IGNORE =
    CommonVars("wds.linkis.engineconn.udf.load.ignore", true, "UDF load failed ignore")

  val FUNCTION_LOAD_FAILED_IGNORE =
    CommonVars("wds.linkis.engineconn.function.load.ignore", true, "Function load failed ignore")

  val TASK_IGNORE_UNCOMPLETED_STATUS =
    CommonVars(
      "wds.linkis.engineconn.task.ignore.uncompleted.status",
      true,
      "Ignore pushes with uncompleted status"
    ).getValue

  val ENGINE_CONCURRENT_THREAD_NUM = CommonVars(
    "linkis.engineconn.concurrent.thread.num",
    20,
    "Maximum thread pool of the concurrent EC"
  )

  val ASYNC_EXECUTE_MAX_PARALLELISM = CommonVars(
    "wds.linkis.engineconn.max.parallelism",
    300,
    "Maximum  parallelism for the asynchronous EC"
  )

  val ASYNC_SCHEDULER_MAX_RUNNING_JOBS =
    CommonVars(
      "wds.linkis.engineconn.async.group.max.running",
      10,
      "Maximum number of running tasks for a group of asynchronous EC"
    ).getValue

  val DEFAULT_COMPUTATION_EXECUTORMANAGER_CLAZZ = CommonVars(
    "wds.linkis.default.computation.executormanager.clazz",
    "org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorManagerImpl",
    "Executor manager implementation class"
  )

  val UPSTREAM_MONITOR_ECTASK_SHOULD_START =
    CommonVars(
      "linkis.upstream.monitor.ectask.should.start",
      true,
      "Enable upstream live monitoring"
    ).getValue

  val UPSTREAM_MONITOR_WRAPPER_ENTRIES_SURVIVE_THRESHOLD_SEC =
    CommonVars(
      "linkis.upstream.monitor.wrapper.entries.survive.time.sec",
      86400,
      "Upstream task cache cleanup threshold"
    ).getValue

  val UPSTREAM_MONITOR_ECTASK_ENTRANCE_THRESHOLD_SEC =
    CommonVars(
      "linkis.upstream.monitor.ectask.entrance.threshold.sec",
      15,
      "Maximum heartbeat time for whether the upstream task is alive"
    ).getValue

  val HIVE_RESULTSET_USE_TABLE_NAME = CommonVars(
    "hive.resultset.use.unique.column.names",
    false,
    "hive result set to enable unique column names"
  )

  val JOB_ID_TO_ENV_KEY =
    CommonVars("wds.linkis.ec.job.id.env.key", "LINKIS_JOB_ID", "LINKIS_JOB_ID ENV").getValue

  val TASK_ASYNC_MAX_THREAD_SIZE =
    CommonVars(
      "linkis.ec.task.execution.async.thread.size",
      50,
      "Task submit thread pool size"
    ).getValue

  val TASK_SUBMIT_WAIT_TIME_MS =
    CommonVars("linkis.ec.task.submit.wait.time.ms", 2L, "Task submit wait time(ms)").getValue

  val ENGINE_SEND_LOG_TO_ENTRANCE_LIMIT_ENABLED =
    CommonVars("linkis.ec.send.log.entrance.limit.enabled", true)

  val ENGINE_SEND_LOG_TO_ENTRANCE_LIMIT_LENGTH =
    CommonVars("linkis.ec.send.log.entrance.limit.length", 2000)

  val ENGINE_KERBEROS_AUTO_REFRESH_ENABLED =
    CommonVars("linkis.ec.kerberos.auto.refresh.enabled", false).getValue

  val CLOSE_RS_OUTPUT_WHEN_RESET_BY_DEFAULT_ENABLED =
    CommonVars("linkis.ec.rs.close.when.reset.enabled", true).getValue

  val SPECIAL_UDF_CHECK_ENABLED =
    CommonVars("linkis.ec.spacial.udf.check.enabled", false)

  val SPECIAL_UDF_CHECK_BY_REGEX_ENABLED =
    CommonVars("linkis.ec.spacial.udf.check.by.regex.enabled", false)

  val SPECIAL_UDF_NAMES =
    CommonVars("linkis.ec.spacial.udf.check.names", "")

  val SUPPORT_SPECIAL_UDF_LANGUAGES =
    CommonVars("linkis.ec.support.spacial.udf.languages", "sql,python")

  val ONLY_SQL_USE_UDF_KEY = "load.only.sql.use.udf"

  val CODE_TYPE = "codeType"

  val SUPPORT_PARTIAL_RETRY_FOR_FAILED_TASKS_ENABLED: Boolean =
    CommonVars[Boolean]("linkis.partial.retry.for.failed.task.enabled", false).getValue

}
