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

package org.apache.linkis.orchestrator.ecm.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

/**
 */
object ECMPluginConf {

  val ECM_ENGINE_PARALLELISM = CommonVars("wds.linkis.orchestrator.ecm.engine.parallelism", 1)

  val ECM_MARK_ATTEMPTS = CommonVars("wds.linkis.orchestrator.ecm.mark.apply.attempts", 2)

  val ECM_MARK_APPLY_TIME =
    CommonVars("wds.linkis.orchestrator.ecm.mark.apply.time", new TimeType("11m"))

  val ECM_ERROR_CODE = 12001

  val ECM_CACHE_ERROR_CODE = 12002

  val ECM_ENGNE_CREATION_ERROR_CODE = 12003

  val ECM_ENGINE_CACHE_ERROR = 12004

  val ECM_MARK_CACHE_ERROR_CODE = 12005

  val DEFAULT_LOADBALANCE_CAPACITY =
    CommonVars("wds.linkis.orchestrator.ecm.loadbalance.capacity.default", 3)

  val EC_ASYNC_RESPONSE_CLEAR_TIME =
    CommonVars("wds.linkis.ecp.ec.response.time", new TimeType("3m"))

}
