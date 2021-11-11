/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.executor.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

object EngineConnExecutorConfiguration {


  val TMP_PATH = CommonVars("wds.linkis.dataworkclod.engine.tmp.path", "file:///tmp/")

  val ENGINE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.engine.application.name", "")

  val ENGINE_LOG_PREFIX = CommonVars("wds.linkis.log4j2.prefix", "/appcom/logs/linkis/" + ENGINE_SPRING_APPLICATION_NAME.getValue)

  val CLEAR_LOG = CommonVars("wds.linkis.log.clear", false)


  val ENGINE_IGNORE_WORDS = CommonVars("wds.linkis.engine.ignore.words", "org.apache.spark.deploy.yarn.Client")

  val ENGINE_PASS_WORDS = CommonVars("wds.linkis.engine.pass.words", "org.apache.hadoop.hive.ql.exec.Task")




  val ENTRANCE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.entrance.application.name", "linkis-cg-entrance")

  val ENGINE_SERVER_LISTENER_ASYNC_QUEUE_CAPACITY = CommonVars("wds.linkis.engine.listener.async.queue.size.max", 300)

  val ENGINE_SERVER_LISTENER_ASYNC_CONSUMER_THREAD_MAX = CommonVars("wds.linkis.engine.listener.async.consumer.thread.max", 5)

  val ENGINE_SERVER_LISTENER_ASYNC_CONSUMER_THREAD_FREE_TIME_MAX = CommonVars("wds.linkis.engine.listener.async.consumer.freetime.max", new TimeType("5000ms"))

  // todo better to rename
  val EXECUTOR_MANAGER_SERVICE_CLAZZ = CommonVars("wds.linkis.engineconn.executor.manager.service.class", "org.apache.linkis.engineconn.acessible.executor.service.DefaultManagerService")

  val EXECUTOR_MANAGER_CLASS = CommonVars("wds.linkis.engineconn.executor.manager.class", "org.apache.linkis.engineconn.core.executor.LabelExecutorManagerImpl")

  /*val EXECUTOR_MANAGER_CLAZZ = CommonVars("wds.linkis.engineconn.executor.manager.claazz", "")*/

  val DEFAULT_EXECUTOR_NAME = CommonVars("wds.linkis.engineconn.executor.default.name", "ComputationExecutor")
}
