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
package com.webank.wedatasphere.linkis.engine.extension

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse

import scala.collection.mutable.ArrayBuffer


trait SparkPostExecutionHook {
  def hookName:String
  def callPostExecutionHook(engineExecutorContext: EngineExecutorContext, executeResponse: ExecuteResponse, code: String): Unit
}

object SparkPostExecutionHook extends Logging{
  private val postHooks = ArrayBuffer[SparkPostExecutionHook]()

  def register(postExecutionHook: SparkPostExecutionHook):Unit = {
    info(s"Get a postExecutionHook of ${postExecutionHook.hookName} register")
    postHooks.append(postExecutionHook)
  }

  def getSparkPostExecutionHooks():Array[SparkPostExecutionHook] = {
    postHooks.toArray
  }
}