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

package com.webank.wedatasphere.linkis.engine.conf

import java.lang.Boolean

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars, TimeType}

/**
  * Created by enjoyyin on 2018/9/3.
  */
object EngineConfiguration {

  val ENGINE_SUPPORT_PARALLELISM = CommonVars("wds.linkis.engine.support.parallelism", new Boolean(false))
  val ENGINE_MAX_PARALLELISM = CommonVars("wds.linkis.engine.parallelism.max", new Integer(5))
  val ENGINE_MAX_CONSUMER_QUEUE_SIZE = CommonVars("wds.linkis.engine.consumer.queue.max", new Integer(200))

  val ENGINE_RESULT_SET_MAX_CACHE = CommonVars("wds.linkis.engine.resultSet.cache.max", new ByteType("0k"))
  val ENGINE_RESULT_SET_STORE_PATH = CommonVars("wds.linkis.engine.resultSet.default.store.path", "hdfs:///tmp")

  val ENGINE_MAX_FREE_TIME = CommonVars("wds.linkis.engine.max.free.time", new TimeType("1h"))
  val ENGINE_MAX_EXECUTE_NUM = CommonVars("wds.linkis.engine.max.execute.num", 0)

  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.linkis.engine.default.limit", 5000)

  val ENGINE_LOG_CACHE_NUM = CommonVars("wds.linkis.engine.log.cache.default", 500)

  val ENGINE_LOG_NUM_SEND_ONCE = CommonVars("wds.linkis.engine.log.send.once", 100)

  val ENGINE_LOG_SEND_TIME_INTERVAL = CommonVars("wds.linkis.engine.log.send.time.interval", 3)

  val TMP_PATH = CommonVars("wds.linkis.dataworkclod.engine.tmp.path","file:///tmp/")

  val ENGINE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.engine.application.name", "")

  val ENGINE_LOG_PREFIX = CommonVars("wds.linkis.log4j2.prefix", "/appcom/logs/dataworkcloud/" + ENGINE_SPRING_APPLICATION_NAME.getValue)

  val CLEAR_LOG = CommonVars("wds.linkis.log.clear", false)

  val ENGINE_UDF_APP_NAME = CommonVars("wds.linkis.engine.udf.app.name", "cloud-publicservice")

  val ENGINE_LOG_TIME_STAMP_SUFFIX = CommonVars("wds.linkis.engine.log.time.suffix", "true")

  val ENGINE_PUSH_LOG_TO_ENTRANCE = CommonVars("wds.linkis.engine.push.log.enable", true)

  val ENGINE_PUSH_PROGRESS_TO_ENTRANCE = CommonVars("wds.linkis.engine.push.progress.enable", true)

  val ENGINE_PRE_EXECUTE_HOOK_CLASSES = CommonVars("wds.linkis.engine.pre.hook.class", "com.webank.wedatasphere.linkis.bml.hook.BmlEnginePreExecuteHook")
}
