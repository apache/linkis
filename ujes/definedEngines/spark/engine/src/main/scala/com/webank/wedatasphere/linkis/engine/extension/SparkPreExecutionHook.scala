package com.webank.wedatasphere.linkis.engine.extension

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext

import scala.collection.mutable.ArrayBuffer


trait SparkPreExecutionHook {
  def hookName:String
  def callPreExecutionHook(engineExecutorContext: EngineExecutorContext, code: String): String
}

object SparkPreExecutionHook extends Logging{
  private val preHooks = ArrayBuffer[SparkPreExecutionHook]()

  def register(preExecutionHook: SparkPreExecutionHook):Unit = {
    info(s"Get a preExecutionHook of ${preExecutionHook.hookName} register")
    preHooks.append(preExecutionHook)
  }

  def getSparkPreExecutionHooks():Array[SparkPreExecutionHook] = {
    preHooks.toArray
  }
}