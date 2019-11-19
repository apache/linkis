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