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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}


object ComputationExecutorConf {

  val ENGINECONN_RESULT_SET_STORE_PATH = CommonVars("wds.linkis.engineconn.resultSet.default.store.path", "hdfs:///tmp")

  val ENGINE_RESULT_SET_MAX_CACHE = CommonVars("wds.linkis.engine.resultSet.cache.max", new ByteType("0k"))

  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.linkis.engine.default.limit", 5000)

  val ENGINE_LOCK_DEFAULT_EXPIRE_TIME = CommonVars("wds.linkis.engine.lock.expire.time", 2 * 60 * 1000)



}
