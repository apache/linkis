package com.webank.wedatasphere.linkis.entrance.executor

import java.io.IOException

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse

/**
 *
 * @author wang_zh
 * @date 2020/5/12
 */
trait EsEngineExecutor extends Logging {

  @throws(classOf[IOException])
  def open : Unit

  def parse(code: String): Array[String]

  def executeLine(code: String, storePath: String, alias: String): ExecuteResponse

  def close: Unit

}
