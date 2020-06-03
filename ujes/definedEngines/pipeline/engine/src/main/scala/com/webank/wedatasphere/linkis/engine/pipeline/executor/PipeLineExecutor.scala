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

package com.webank.wedatasphere.linkis.engine.pipeline.executor

import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}

/**
 * Created by johnnwang on 2019/1/30.
 */
trait PipeLineExecutor {
  var options: java.util.Map[String, String] = _

  def execute(sourcePath: String, destPath: String, engineExecutorContext: EngineExecutorContext): ExecuteResponse = {
    cleanOptions
    SuccessExecuteResponse()
  }

  def Kind: String

  def init(newOptions: java.util.Map[String, String]): Unit = {
    options = newOptions
  }

  def cleanOptions = options = null
}
