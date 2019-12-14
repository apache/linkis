package com.webank.wedatasphere.linkis.engine.extension

import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest

/**
  * created by cooperyang on 2019/11/29
  * Description:
  */
trait EnginePreExecuteHook {
  val hookName:String
  def callPreExecuteHook(engineExecutorContext:EngineExecutorContext, executeRequest: ExecuteRequest)
}