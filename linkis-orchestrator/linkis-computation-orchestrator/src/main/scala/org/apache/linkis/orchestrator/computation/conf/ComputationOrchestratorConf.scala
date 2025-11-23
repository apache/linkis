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

package org.apache.linkis.orchestrator.computation.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

/**
 */
object ComputationOrchestratorConf {

  val DEFAULT_CREATE_SERVICE =
    CommonVars("wds.linkis.computation.orchestrator.create.service", "dss")

  val DEFAULT_MARK_MAX_ENGINE =
    CommonVars("wds.linkis.computation.orchestrator.mark.max.engine", 3)

  val EXECUTOR_MANAGER_BUILDER_CLASS =
    CommonVars("wds.linkis.orchestrator.computation.code.executor.manager.class", "")

  val MAX_ASK_EXECUTOR_TIME =
    CommonVars("wds.linkis.orchestrator.max.ask.executor.time", new TimeType("10m"))

  val SHELL_DANGER_USAGE = CommonVars(
    "wds.linkis.shell.danger.usage",
    "rm,sh,find,kill,python,for,source,hdfs,hadoop,spark-sql,spark-submit,pyspark,spark-shell,hive,yarn"
  )

  val SHELL_WHITE_USAGE = CommonVars("wds.linkis.shell.white.usage", "cd,ls")

  val SHELL_WHITE_USAGE_ENABLED = CommonVars("wds.linkis.shell.white.usage.enabled", false)

  val CACHE_SERVICE_APPLICATION_NAME =
    CommonVars("wds.linkis.cache.service.application.name", "linkis-ps-publicservice")

  val COMPUTATION_SESSION_FACTORY_CLASS =
    CommonVars("wds.linkis.orchestrator.computation.session.factory.class", "")

  val COMPUTATION_OPERATION_BUILDER_CLASS =
    CommonVars("wds.linkis.orchestrator.computation.operation.builder.class", "")

  val LOG_LEN = CommonVars("wds.linkis.computation.orchestrator.log.len", 100)

  val ENGINECONN_LASTUPDATE_TIMEOUT =
    CommonVars("wds.linkis.orchestrator.engine.lastupdate.timeout", new TimeType("10s"))

  val ENGINECONN_ACTIVITY_MONITOR_INTERVAL =
    CommonVars("wds.linkis.orchestrator.engine.activity_monitor.interval", new TimeType("10s"))

  val TASK_STATUS_COMPLETE_WAIT_TIMEOUT =
    CommonVars("linkis.orchestrator.task.complete.timeout", new TimeType("10s")).getValue.toLong

}
