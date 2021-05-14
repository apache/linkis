/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.orchestrator.ecm.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}


object ECMPluginConf {

  val ECM_ENGINE_PARALLELISM = CommonVars("wds.linkis.orchestrator.ecm.engine.parallelism", 1)

  val ECM_MARK_ATTEMPTS = CommonVars("wds.linkis.orchestrator.ecm.mark.apply.attempts", 2)

  val ECM_MARK_APPLY_TIME = CommonVars("wds.linkis.orchestrator.ecm.mark.apply.time", new TimeType("5m"))

  val ECM_ERROR_CODE = 12001

  val ECM_CACHE_ERROR_CODE = 12002

  val ECM_ENGNE_CREATION_ERROR_CODE = 12003


}
