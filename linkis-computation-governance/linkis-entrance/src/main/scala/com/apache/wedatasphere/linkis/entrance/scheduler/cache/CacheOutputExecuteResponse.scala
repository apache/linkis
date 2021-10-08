package com.apache.wedatasphere.linkis.entrance.scheduler.cache

import com.apache.wedatasphere.linkis.scheduler.executer.OutputExecuteResponse

case class CacheOutputExecuteResponse(alias: String, output: String) extends OutputExecuteResponse{
  override def getOutput: String = output
}
