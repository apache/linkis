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

package com.webank.wedatasphere.linkis.engine.executors

import java.io.IOException

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.spark.common.Kind
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse

/**
  * Created by allenlliu on 2019/4/8.
  */

abstract class SparkExecutor extends Logging {

  @throws(classOf[IOException])
  def open : Unit

  def execute(sparkEngineExecutor: SparkEngineExecutor, code: String,engineExecutorContext: EngineExecutorContext,jobGroup:String): ExecuteResponse

  def close: Unit

  def kind: Kind



}
