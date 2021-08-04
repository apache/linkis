package com.webank.wedatasphere.linkis.entrance.scheduler.cache

import com.webank.wedatasphere.linkis.scheduler.executer.OutputExecuteResponse

case class CacheOutputExecuteResponse(alias: String, output: String) extends OutputExecuteResponse{
  override def getOutput: String = output
}
