/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration

/**
  * Created by enjoyyin on 2018/9/10.
  */
object EntranceConfiguration {

  val ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS = CommonVars("wds.linkis.entrance.scheduler.maxParallelismUsers", new Integer(1000))
  val ENTRANCE_LISTENER_BUS_EVENT_QUEUE_CAPACITY = CommonVars("wds.linkis.entrance.listenerBus.queue.capacity", new Integer(5000))

  val JOB_MAX_PERSIST_WAIT_TIME = CommonVars("wds.linkis.entrance.job.persist.wait.max", new TimeType("5m"))
  val JOB_STATUS_HEARTBEAT_TIME = CommonVars("wds.linkis.entrance.job.heartbeat", new TimeType("2m"))
  val ENGINE_STATUS_HEARTBEAT_TIME = CommonVars("wds.linkis.entrance.engine.heartbeat", new TimeType("2m"))
  val ENGINE_LIST_FRESH_INTERVAL = CommonVars("wds.linkis.entrance.engine.fresh.interval", new TimeType("2m"))
  val UN_HEALTH_ENGINE_SCAN_TIME = CommonVars("wds.linkis.entrance.un-health.engine.scan.interval", new TimeType("5m"))
  val ENGINE_LOCK_SCAN_TIME = CommonVars("wds.linkis.entrance.engine-lock.scan.time", new TimeType("1m"))

  val ENGINE_MANAGER_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.enginemanager.application.name", "")
  val ENGINE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.engine.application.name", "")

  val ENGINE_LOCK_MAX_HOLDER_TIME = CommonVars("wds.linkis.engine.lock.timeout", new TimeType("2m"))
  val ENGINE_CREATE_MAX_WAIT_TIME = CommonVars("wds.linkis.engine.creation.wait.time.max", new TimeType("2m"))

  val CONCURRENT_ENGINE_MAX_PARALLELISM = CommonVars("wds.linkis.entrance.engine.maxParallelismJobs", new Integer(5))

  val RESULT_SET_STORE_PATH = CommonVars("wds.linkis.resultSet.store.path", "")

  /**
    * QUERY_PERSISTENCE_SPRING_APPLICATION_NAME is the name of the application that represents the query module in springcloud
    * QUERY_PERSISTENCE_SPRING_APPLICATION_NAME 是表示query模块在springcloud中的应用名称
    */
  val QUERY_PERSISTENCE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.query.application.name", "cloud-publicservice")
  /**
    * DEFAULT_LOGPATH_PREFIX is the prefix that represents the default log storage path
    * DEFAULT_LOGPATH_PREFIX 是表示默认的日志存储路径的前缀
    */
  val DEFAULT_LOGPATH_PREFIX = CommonVars("wds.linkis.entrance.config.logPath", "file:///" + ServerConfiguration.BDP_SERVER_HOME.getValue + "/logs/applications/")
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
    * The application name of the console module in spring-cloud
    * console 模块在spring-cloud中的应用名称
    */
  val CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.console.configuration.application.name", "cloud-publicservice")
  val CLOUD_CONSOLE_VARIABLE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.console.variable.application.name", "cloud-publicservice")
  /**
    * The logPath in the console module returns the key in the map.
    * console 模块中logPath在返回map中的key
    */
  val CLOUD_CONSOLE_LOGPATH_KEY = CommonVars("wds.linkis.console.config.logPath", "wds.linkis.config.logPath")
  /**
    * requestApplicationName(Creator) The default service name, the default is IDE
    * requestApplicationName(Creator) 默认的服务名，默认为IDE
    */
  val DEFAULT_REQUEST_APPLICATION_NAME = CommonVars("wds.linkis.default.requestApplication.name", "IDE")
  /**
    * runType
    */
  val DEFAULT_RUN_TYPE = CommonVars("wds.linkis.default.runType", "sql")

  val LOG_WARN_EXCLUDE = CommonVars("wds.linkis.warn.log.exclude", "org.apache,hive.ql,hive.metastore,com.netflix,com.webank.wedatasphere")

  val CLEAR_LOG = CommonVars("wds.linkis.log.clear", false)

  /**
    * LOG_EXCLUDE is used to remove the log of the framework log, such as hive spark spring, so that it is not pushed to the front end through websocket.
    * LOG_EXCLUDE 是用来进行把框架日志，比如hive spark spring等日志进行剔除，不让其通过websocket进行推送到前端
    */
  val LOG_EXCLUDE = CommonVars("wds.linkis.log.exclude", "org.apache,hive.ql,hive.metastore,com.netflix,com.webank.wedatasphere,com.webank")

  /**
    * wds.linkis.dwc.instance is a parameter used to control the number of engines each user starts.
    *wds.linkis.instance 是用来进行控制每个用户启动engine数量的参数
    */
  val WDS_LINKIS_INSTANCE = CommonVars("wds.linkis.instance", 3)

  val LOG_EXCLUDE_ALL = CommonVars("wds.linkis.log.exclude.all", "com.netflix")

  val MAX_ASK_EXECUTOR_TIME = CommonVars("wds.linkis.max.ask.executor.time", new TimeType("5m"))

  val ERROR_CODE_FILE_DIR = CommonVars("wds.linkis.errorcode.file.dir", "")

  val ENTRANCE_USER = CommonVars("wds.linkis.entrance.user", "")

  val ERROR_CODE_FILE = CommonVars("wds.linkis.errorcode.file", "")

  //todo enjoyyin Simple processing first, this log is not filtered, and subsequent optimization is performed.(先简单处理 不过滤这个日志，后续进行优化)
  val HIVE_SPECIAL_LOG_INCLUDE = CommonVars("wds.linkis.hive.special.log.include", "org.apache.hadoop.hive.ql.exec.Task")

  val SHARE_FILE_PRE = CommonVars("wds.linkis.share.file.prefix", "")

  val HIVE_THREAD_NAME = CommonVars("wds.linkis.hive.thread.name", "[Thread")

  val HIVE_STAGE_NAME = CommonVars("wds.linkis.hive.stage.name", "Stage-")

  val SPARK_SPECIAL_LOG_INCLUDE = CommonVars("wds.linkis.spark.special.log.include", "com.webank.wedatasphere.linkis.engine.spark.utils.JobProgressUtil")


  val SPARK_PROGRESS_NAME = CommonVars("wds.linkis.spark.progress.name", "com.webank.wedatasphere.linkis.engine.spark.utils.JobProgressUtil$")

  val END_FLAG = CommonVars("bdp.dataworkcloud.entrance.end.flag", "info -")

  val HIVE_CREATE_TABLE_LOG = CommonVars("wds.linkis.hive.create.table.log", "numFiles")

  val HIVE_PRINT_INFO_LOG = CommonVars("wds.linkis.hive.printinfo.log", "printInfo -")



  val IS_BDP_ENV = CommonVars("wds.linkis.entrance.bdp.env", "true")


  val SHELL_DANGER_USAGE = CommonVars("wds.linkis.shell.danger.usage", "rm,sh,find,kill,python,for,source,hdfs,hadoop,spark-sql,spark-submit,pyspark,spark-shell,hive,yarn")
  val SHELL_WHITE_USAGE = CommonVars("wds.linkis.shell.white.usage", "cd,ls")

  val FLOW_EXECUTION_CREATOR = CommonVars("wds.linkis.entrance.flow.creator", "nodeexecution")

  val SCHEDULER_CREATOR = CommonVars("wds.linkis.entrance.scheduler.creator", "scheduler")
}
