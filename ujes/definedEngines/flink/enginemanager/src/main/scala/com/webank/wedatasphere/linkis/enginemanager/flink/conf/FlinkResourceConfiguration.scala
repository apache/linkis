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

package com.webank.wedatasphere.linkis.enginemanager.flink.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 * 
 */
object FlinkResourceConfiguration {
  val FLINK_ENGINE_REQUEST_MEMORY = CommonVars[ByteType]("flink.client.memory", new ByteType("2g"))
  val FLINK_ENGINE_REQUEST_CORES = CommonVars[Int]("wds.linkis.flinkengine.cores.request", 1)
  val FLINK_ENGINE_REQUEST_INSTANCE = CommonVars[Int]("wds.linkis.flinkengine.instance.request", 1)
}
