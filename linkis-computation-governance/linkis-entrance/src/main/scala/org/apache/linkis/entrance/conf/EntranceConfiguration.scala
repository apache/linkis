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

package org.apache.linkis.entrance.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

object EntranceConfiguration {

  val ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS =
    CommonVars("wds.linkis.entrance.scheduler.maxParallelismUsers", new Integer(1000))

  val ENTRANCE_LISTENER_BUS_EVENT_QUEUE_CAPACITY =
    CommonVars("wds.linkis.entrance.listenerBus.queue.capacity", new Integer(5000))

  val JOB_MAX_PERSIST_WAIT_TIME =
    CommonVars("wds.linkis.entrance.job.persist.wait.max", new TimeType("5m"))

  /**
   * Default_Cache_Max is used to specify the size of the LoopArray of the CacheLogWriter
   * Default_Cache_Max 是用来指定CacheLogWriter的LoopArray的大小
   */
  val DEFAULT_CACHE_MAX = CommonVars("wds.linkis.entrance.log.cacheMax", 500)

  /**
   * Default_Log_CharSet is used to specify the encoding mode of the log storage.
   * Default_Log_CharSet 是用来指定日志存储的编码方式
   */
  val DEFAULT_LOG_CHARSET = CommonVars("wds.linkis.entrance.log.defaultCharSet", "utf-8")

  /**
   * The logPath in the console module returns the key in the map. console 模块中logPath在返回map中的key
   */
  val CLOUD_CONSOLE_LOGPATH_KEY =
    CommonVars("wds.linkis.console.config.logPath", "wds.linkis.config.logPath")

  /**
   * requestApplicationName(Creator) The default service name, the default is IDE
   * requestApplicationName(Creator) 默认的服务名，默认为IDE
   */
  val DEFAULT_REQUEST_APPLICATION_NAME =
    CommonVars("wds.linkis.default.requestApplication.name", "IDE")

  val SQL_LIMIT_CREATOR = CommonVars("wds.linkis.sql.limit.creator", "IDE")

  /**
   * runType
   */
  val DEFAULT_RUN_TYPE = CommonVars("wds.linkis.default.runType", "sql")

  val DEFAULT_CREATE_SERVICE =
    CommonVars("wds.linkis.default.create.service", "default_create_service")

  val LOG_WARN_EXCLUDE = CommonVars("wds.linkis.warn.log.exclude", "com.netflix")

  val CLEAR_LOG = CommonVars("wds.linkis.log.clear", false)

  /**
   * LOG_EXCLUDE is used to remove the log of the framework log, such as hive spark spring, so that
   * it is not pushed to the front end through websocket. LOG_EXCLUDE 是用来进行把框架日志，比如hive spark
   * spring等日志进行剔除，不让其通过websocket进行推送到前端
   */
  val LOG_EXCLUDE = CommonVars("wds.linkis.log.exclude", "com.netflix")

  /**
   * wds.linkis.dwc.instance is a parameter used to control the number of engines each user starts.
   * wds.linkis.instance 是用来进行控制每个用户启动engine数量的参数
   */
  val WDS_LINKIS_INSTANCE = CommonVars("wds.linkis.rm.instance", 10)

  val WDS_LINKIS_ENTRANCE_RUNNING_JOB =
    CommonVars("wds.linkis.engine.running.job.max", WDS_LINKIS_INSTANCE.getValue)

  val ENTRANCE_INSTANCE_MIN = CommonVars("wds.linkis.entrance.runningjob.min", 5)

  val LOG_EXCLUDE_ALL = CommonVars("wds.linkis.log.exclude.all", "com.netflix")

  val MAX_ASK_EXECUTOR_TIME = CommonVars("wds.linkis.max.ask.executor.time", new TimeType("5m"))

  val ERROR_CODE_FILE_DIR = CommonVars("wds.linkis.errorcode.file.dir", "")

  val ENTRANCE_USER = CommonVars("wds.linkis.entrance.user", "")

  val ERROR_CODE_FILE = CommonVars("wds.linkis.errorcode.file", "")

  // todo Simple processing first, this log is not filtered, and subsequent optimization is performed.(先简单处理 不过滤这个日志，后续进行优化)
  val HIVE_SPECIAL_LOG_INCLUDE =
    CommonVars("wds.linkis.hive.special.log.include", "org.apache.hadoop.hive.ql.exec.Task")

  val SHARE_FILE_PRE = CommonVars("wds.linkis.share.file.prefix", "")

  val HIVE_THREAD_NAME = CommonVars("wds.linkis.hive.thread.name", "[Thread")

  val HIVE_STAGE_NAME = CommonVars("wds.linkis.hive.stage.name", "Stage-")

  val SPARK_SPECIAL_LOG_INCLUDE = CommonVars(
    "wds.linkis.spark.special.log.include",
    "org.apache.linkis.engine.spark.utils.JobProgressUtil"
  )

  val SPARK_PROGRESS_NAME = CommonVars(
    "wds.linkis.spark.progress.name",
    "org.apache.linkis.engine.spark.utils.JobProgressUtil$"
  )

  val END_FLAG = CommonVars("bdp.dataworkcloud.entrance.end.flag", "info -")

  val HIVE_CREATE_TABLE_LOG = CommonVars("wds.linkis.hive.create.table.log", "numFiles")

  val HIVE_PRINT_INFO_LOG = CommonVars("wds.linkis.hive.printinfo.log", "printInfo -")

  val IS_BDP_ENV = CommonVars("wds.linkis.entrance.bdp.env", "true")

  val SHELL_DANGER_CHECK_SWITCH =
    CommonVars("wds.linkis.entrance.shell.danger.check.enabled", true)

  val SHELL_DANGER_USAGE = CommonVars(
    "wds.linkis.shell.danger.usage",
    "rm,sh,find,kill,python,for,source,hdfs,hadoop,spark-sql,spark-submit,pyspark,spark-shell,hive,yarn"
  )

  val SHELL_WHITE_USAGE = CommonVars(
    "wds.linkis.shell.white.usage",
    "sqoop,cd,ll,ls,echo,cat,tree,diff,who,grep,whoami,set,pwd,cut,file,head,less,if,while"
  )

  val FLOW_EXECUTION_CREATOR = CommonVars("wds.linkis.entrance.flow.creator", "nodeexecution")

  val SCHEDULER_CREATOR = CommonVars("wds.linkis.entrance.scheduler.creator", "Schedulis")

  val SKIP_AUTH = CommonVars("wds.linkis.entrance.skip.auth", false)

  val PROGRESS_PUSH = CommonVars[String]("wds.linkis.entrance.push.progress", "false")

  val CONCURRENT_FACTORY_MAX_CAPACITY =
    CommonVars("wds.linkis.concurrent.group.factory.capacity", 1000)

  val CONCURRENT_MAX_RUNNING_JOBS =
    CommonVars("wds.linkis.concurrent.group.factory.running.jobs", 30)

  val CONCURRENT_EXECUTOR_TIME =
    CommonVars("wds.linkis.concurrent.group.factory.executor.time", 5 * 60 * 1000)

  val ENTRANCE_ENGINE_LASTUPDATE_TIMEOUT =
    CommonVars("wds.linkis.entrance.engine.lastupdate.timeout", new TimeType("5s"))

  val ENTRANCE_ENGINE_ACTIVITY_TIMEOUT =
    CommonVars("wds.linkis.entrance.engine.timeout", new TimeType("10s"))

  val ENTRANCE_ENGINE_ACTIVITY_MONITOR_INTERVAL =
    CommonVars("wds.linkis.entrance.engine.activity_monitor.interval", new TimeType("3s"))

  // Whether to turn on timeout detection
  val ENABLE_JOB_TIMEOUT_CHECK = CommonVars("wds.linkis.enable.job.timeout.check", true)

  // unit is seconds
  val TIMEOUT_SCAN_INTERVAL = CommonVars("wds.linkis.timeout.thread.scan.interval", 120)

  // unit is MINUTES
  val USER_PARALLEL_REFLESH_TIME = CommonVars("wds.linkis.user.parallel.reflesh.time", 30)

  val JOBINFO_UPDATE_RETRY =
    CommonVars[java.lang.Boolean]("wds.linkis.entrance.jobinfo.update.retry", true)

  val JOBINFO_UPDATE_RETRY_MAX_TIME =
    CommonVars[Integer]("wds.linkis.entrance.jobinfo.update.retry.max.times", 3)

  val JOBINFO_UPDATE_RETRY_INTERVAL =
    CommonVars[Integer]("wds.linkis.entrance.jobinfo.update.retry.interval", 2 * 60 * 1000)

  val CODE_PARSER_SELECTIVE_IGNORED =
    CommonVars[java.lang.Boolean]("wds.linkis.entrance.code.parser.selective.ignored", true)

  val YARN_QUEUE_CORES_MAX = CommonVars[Integer]("wds.linkis.entrance.yarn.queue.core.max", 300)

  val YARN_QUEUE_MEMORY_MAX =
    CommonVars[Integer]("wds.linkis.entrance.yarn.queue.memory.max.g", 1000)

  val ENABLE_HDFS_LOG_CACHE =
    CommonVars[Boolean]("linkis.entrance.enable.hdfs.log.cache", true).getValue

  val CLI_HEARTBEAT_THRESHOLD_SECONDS =
    CommonVars[Long]("linkis.entrance.cli.heartbeat.threshold.sec", 30L).getValue

  val LOG_PUSH_INTERVAL_TIME =
    CommonVars("wds.linkis.entrance.log.push.interval.time", 5 * 60 * 1000)

  val GROUP_CACHE_MAX = CommonVars("wds.linkis.consumer.group.cache.capacity", 5000)

  val GROUP_CACHE_EXPIRE_TIME = CommonVars("wds.linkis.consumer.group.expire.time", 50)

  val CLIENT_MONITOR_CREATOR =
    CommonVars("wds.linkis.entrance.client.monitor.creator", "LINKISCLI,BdpClient")

  val CREATOR_IP_SWITCH =
    CommonVars("wds.linkis.entrance.user.creator.ip.interceptor.switch", false)

  val TEMPLATE_CONF_SWITCH =
    CommonVars("wds.linkis.entrance.template.conf.interceptor.switch", true)

  val TEMPLATE_CONF_ADD_ONCE_LABEL_ENABLE =
    CommonVars("wds.linkis.entrance.template.add.once.label.enable", false)

  val SUPPORT_TEMPLATE_CONF_RETRY_ENABLE =
    CommonVars("linkis.entrance.template.retry.enable", false)

  val ENABLE_ENTRANCE_DIRTY_DATA_CLEAR: CommonVars[Boolean] =
    CommonVars[Boolean]("linkis.entrance.auto.clean.dirty.data.enable", true)

  val ENTRANCE_CREATOR_JOB_LIMIT: CommonVars[Int] =
    CommonVars[Int](
      "linkis.entrance.creator.job.concurrency.limit",
      10000,
      "Creator task concurrency limit parameters"
    )

  val ENTRANCE_CREATOR_JOB_LIMIT_CONF_CACHE =
    CommonVars("linkis.entrance.creator.job.concurrency.limit.conf.cache.time", 30L)

  val ENTRANCE_TASK_TIMEOUT =
    CommonVars("linkis.entrance.task.timeout", new TimeType("48h"))

  val ENTRANCE_TASK_TIMEOUT_SCAN =
    CommonVars("linkis.entrance.task.timeout.scan", new TimeType("12h"))

  val ENABLE_HDFS_JVM_USER =
    CommonVars[Boolean]("linkis.entrance.enable.hdfs.jvm.user", true).getValue

  val ENTRANCE_FAILOVER_ENABLED = CommonVars("linkis.entrance.failover.enable", false).getValue

  val ENTRANCE_FAILOVER_SCAN_INIT_TIME =
    CommonVars("linkis.entrance.failover.scan.init.time", 3 * 1000).getValue

  val ENTRANCE_FAILOVER_SCAN_INTERVAL =
    CommonVars("linkis.entrance.failover.scan.interval", 30 * 1000).getValue

  val ENTRANCE_FAILOVER_DATA_NUM_LIMIT =
    CommonVars("linkis.entrance.failover.data.num.limit", 10).getValue

  val ENTRANCE_FAILOVER_DATA_INTERVAL_TIME =
    CommonVars("linkis.entrance.failover.data.interval.time", new TimeType("1d").toLong).getValue

  // if true, the waitForRetry job in runningJobs can be failover
  val ENTRANCE_FAILOVER_RETRY_JOB_ENABLED =
    CommonVars("linkis.entrance.failover.retry.job.enable", false)

  val ENTRANCE_UPDATE_BATCH_SIZE = CommonVars("linkis.entrance.update.batch.size", 100)

  // if true, the job in ConsumeQueue can be failover
  val ENTRANCE_SHUTDOWN_FAILOVER_CONSUME_QUEUE_ENABLED =
    CommonVars("linkis.entrance.shutdown.failover.consume.queue.enable", false).getValue

  val ENTRANCE_GROUP_SCAN_ENABLED = CommonVars("linkis.entrance.group.scan.enable", false)

  val ENTRANCE_GROUP_SCAN_INIT_TIME = CommonVars("linkis.entrance.group.scan.init.time", 3 * 1000)

  val ENTRANCE_GROUP_SCAN_INTERVAL = CommonVars("linkis.entrance.group.scan.interval", 60 * 1000)

  val ENTRANCE_FAILOVER_RETAIN_METRIC_ENGINE_CONN_ENABLED =
    CommonVars("linkis.entrance.failover.retain.metric.engine.conn.enable", false)

  val ENTRANCE_FAILOVER_RETAIN_METRIC_YARN_RESOURCE_ENABLED =
    CommonVars("linkis.entrance.failover.retain.metric.yarn.resource.enable", false)

  // if true, job whose status is running will be set to Cancelled
  val ENTRANCE_FAILOVER_RUNNING_KILL_ENABLED =
    CommonVars("linkis.entrance.failover.running.kill.enable", false)

  val LINKIS_ENTRANCE_SKIP_ORCHESTRATOR =
    CommonVars("linkis.entrance.skip.orchestrator", false).getValue

  val ENABLE_HDFS_RES_DIR_PRIVATE =
    CommonVars[Boolean]("linkis.entrance.enable.hdfs.res.dir.private", false).getValue

  val UNSUPPORTED_RETRY_CODES =
    CommonVars("linkis.entrance.unsupported.retry.codes", "NOCODE").getValue

  val SUPPORTED_RETRY_ERROR_CODES =
    CommonVars(
      "linkis.entrance.supported.retry.error.codes",
      "01002,01003,13005,13006,13012"
    ).getValue

  val SUPPORTED_RETRY_ERROR_DESC =
    CommonVars(
      "linkis.entrance.supported.retry.error.desc",
      "Spark application has already stopped,Spark application sc has already stopped,Failed to allocate a page,dataFrame to local exception,org.apache.spark.sql.catalyst.expressions.codegen.CodeGenerator"
    ).getValue

  val SUPPORT_ADD_RETRY_CODE_KEYS =
    CommonVars(
      "linkis.entrance.supported.add.retry.code.keys",
      "dataFrame to local exception,org.apache.spark.sql.catalyst.expressions.codegen.CodeGenerator"
    ).getValue

  val TASK_RETRY_ENABLED: Boolean =
    CommonVars[Boolean]("linkis.task.retry.enabled", true).getValue

  val AI_SQL_DEFAULT_SPARK_ENGINE_TYPE: String =
    CommonVars[String]("linkis.ai.sql.default.spark.engine.type", "spark-3.4.4").getValue

  val AI_SQL_DEFAULT_HIVE_ENGINE_TYPE: String =
    CommonVars[String]("linkis.ai.sql.default.hive.engine.type", "hive-2.3.3").getValue

  val AI_SQL_HIVE_TEMPLATE_KEYS: String =
    CommonVars[String]("linkis.ai.sql.hive.template.keys", "hive,mapreduce").getValue

  val AI_SQL_CREATORS: String =
    CommonVars[String]("linkis.ai.sql.support.creators", "IDE,MCP").getValue

  val AI_SQL_KEY: CommonVars[String] =
    CommonVars[String]("linkis.ai.sql.enable", "true")

  val RETRY_NUM_KEY: CommonVars[Int] =
    CommonVars[Int]("linkis.ai.retry.num", 1)

  val AI_SQL_RETRY_ONCE: CommonVars[Boolean] =
    CommonVars[Boolean]("linkis.ai.sql.once.enable", true)

  val SPARK3_VERSION_COERCION_USERS: String =
    CommonVars[String]("spark.version.coercion.users", "").getHotValue()

  val SPARK3_VERSION_COERCION_DEPARTMENT: String =
    CommonVars[String]("spark.version.coercion.department.id", "").getHotValue()

  val SPARK3_VERSION_COERCION_SWITCH: Boolean =
    CommonVars[Boolean]("spark.version.coercion.switch", false).getValue

  val PYTHON_SAFE_CHECK_SWITCH = CommonVars("linkis.python.safe.check.switch", false).getValue

  val DOCTOR_URL = CommonVars("linkis.doctor.url", "").getValue

  val DOCTOR_DYNAMIC_ENGINE_URL = CommonVars(
    "linkis.aisql.doctor.api",
    "/api/v1/external/engine/diagnose?app_id=$app_id&timestamp=$timestamp&nonce=$nonce&signature=$signature"
  ).getValue

  val DOCTOR_ENCRYPT_SQL_URL = CommonVars(
    "linkis.encrypt.doctor.api",
    "/api/v1/external/plaintext/diagnose?app_id=$app_id&timestamp=$timestamp&nonce=$nonce&signature=$signature"
  ).getValue

  val DOCTOR_SIGNATURE_TOKEN = CommonVars("linkis.doctor.signature.token", "").getValue

  val DOCTOR_NONCE = CommonVars.apply("linkis.doctor.signature.nonce", "").getValue

  val LINKIS_SYSTEM_NAME = CommonVars("linkis.system.name", "").getValue

  val DOCTOR_CLUSTER = CommonVars("linkis.aisql.doctor.cluster", "").getValue

  val AI_SQL_DYNAMIC_ENGINE_SWITCH =
    CommonVars("linkis.aisql.dynamic.engine.type.switch", false).getValue

  val DOCTOR_REQUEST_TIMEOUT = CommonVars("linkis.aisql.doctor.http.timeout", 30000).getValue

  val DOCTOR_HTTP_MAX_CONNECT = CommonVars("linkis.aisql.doctor.http.max.connect", 20).getValue

  val SPARK_EXECUTOR_CORES = CommonVars.apply("spark.executor.cores", "2");

  var SPARK_EXECUTOR_MEMORY = CommonVars.apply("spark.executor.memory", "6G");

  var SPARK_DYNAMIC_ALLOCATION_MAX_EXECUTORS =
    CommonVars.apply("spark.dynamicAllocation.maxExecutors", "50");

  var SPARK_EXECUTOR_INSTANCES = CommonVars.apply("spark.executor.instances", "1");

  var SPARK_EXECUTOR_MEMORY_OVERHEAD = CommonVars.apply("spark.executor.memoryOverhead", "2G");

  var SPARK3_PYTHON_VERSION = CommonVars.apply("spark.python.version", "python3");

  var SPARK_DYNAMIC_ALLOCATION_ENABLED =
    CommonVars.apply("spark.dynamic.allocation.enabled", false).getValue

  var SPARK_DYNAMIC_ALLOCATION_ADDITIONAL_CONFS =
    CommonVars.apply("spark.dynamic.allocation.additional.confs", "").getValue

  var DOCTOR_SENSITIVE_SQL_CHECK_SWITCH =
    CommonVars[Boolean]("linkis.doctor.sensitive.sql.check.switch", false).getValue

  var DOCTOR_SENSITIVE_SQL_CHECK_RUNTYPE =
    CommonVars[String]("linkis.doctor.sensitive.sql.check.run.Type", "sql,python").getValue

  var DOCTOR_SENSITIVE_SQL_CHECK_CREATOR =
    CommonVars[String]("linkis.doctor.sensitive.sql.check.creator", "").getValue

  var DOCTOR_SENSITIVE_SQL_CHECK_DEPARTMENT =
    CommonVars[String]("linkis.doctor.sensitive.sql.check.department", "").getValue

  var DOCTOR_SENSITIVE_SQL_CHECK_WHITELIST =
    CommonVars[String]("linkis.doctor.sensitive.sql.check.whitelist", "").getValue

  var DOCTOR_SENSITIVE_SQL_CHECK_ENGINETYPE =
    CommonVars[String]("linkis.doctor.sensitive.sql.check.engine.type", "hive,spark").getValue

}
