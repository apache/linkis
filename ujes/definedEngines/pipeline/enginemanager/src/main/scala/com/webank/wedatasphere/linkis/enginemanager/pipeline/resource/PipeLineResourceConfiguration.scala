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

package com.webank.wedatasphere.linkis.enginemanager.pipeline.resource

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

/**
  * Created by johnnwang on 2019/2/26.
  */
object PipeLineResourceConfiguration {
  val PIPELINE_ENGINE_MEMORY = CommonVars[ByteType]("pipeline.engine.memory", new ByteType("2g"))
  val PIPELINE_ENGINE_CORES = CommonVars[Int]("pipeline.engine.cores", 2)
  val PIPElINE_ENGINE_INSTANCE = CommonVars[Int]("pipeline.engine.instance", 1)
}
