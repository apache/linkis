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

package com.webank.wedatasphere.linkis.enginemanager.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}

/**
  * Created by johnnwang on 2018/9/28.
  */
object EngineManagerConfiguration {
  val ENGINE_MANAGER_MAX_THREADS = CommonVars[Int]("wds.linkis.engine.manager.threadpool.max", 100)
  val ENGINE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.engine.application.name", "")
  val CONSOLE_CONFIG_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.console.config.application.name", "cloud-publicservice")

  val ENGINE_SCAN_INTERVAL = CommonVars("wds.linkis.engine.scan.interval", new TimeType("2m"))//清除僵死Engine
  val ENGINE_CAN_SCAN_AFTER_INIT = CommonVars("wds.linkis.engine.can.scan.when", new TimeType("2m"))
  val UNKNOWN_ENGINE_SCAN_INTERVAL = CommonVars("wds.linkis.engine.unknown.scan.interval", new TimeType("2m")) //清除失去掌控的Engine
  val ENGINE_UDF_APP_NAME = CommonVars("wds.linkis.engine.udf.app.name", "cloud-publicservice")
  val ENGINE_UDF_BUILT_IN_PATH = CommonVars("wds.linkis.engine.udf.app.name", "/commonlib/webank_bdp_udf.jar")
  val ENGINE_UDF_BUFFER = CommonVars("wds.linkis.engine.udf.buffer", "/tmp/udf/")
}
