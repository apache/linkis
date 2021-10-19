package org.apache.linkis.entrance.scheduler.cache

import org.apache.linkis.scheduler.executer.OutputExecuteResponse

case class CacheOutputExecuteResponse(alias: String, output: String) extends OutputExecuteResponse{
  override def getOutput: String = output
}
