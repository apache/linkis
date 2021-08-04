package com.webank.wedatasphere.linkis.engineconnplugin.flink.ql

import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.Operation
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext

/**
  * Created by enjoyyin on 2021/6/7.
  */
trait Grammar extends Operation {

  def canParse(sql: String): Boolean

  def copy(context: FlinkEngineConnContext, sql: String): Grammar

}
