package com.webank.wedatasphere.linkis.engineconnplugin.flink.factory

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutor
import com.webank.wedatasphere.linkis.engineconn.once.executor.creation.OnceExecutorFactory
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.executor.FlinkCodeOnceExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType._

/**
  * Created by enjoyyin on 2021/5/27.
  */
class FlinkCodeExecutorFactory extends OnceExecutorFactory {
  override protected def newExecutor(id: Int, engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn, labels: Array[Label[_]]): OnceExecutor = engineConn.getEngineConnSession match {
    case context: FlinkEngineConnContext =>
      new FlinkCodeOnceExecutor(id, context)
  }

  // just set lots of runType, but now only sql is supported.
  override protected def getSupportRunTypes: Array[String] = Array(SQL.toString, SCALA.toString, JAVA.toString)

  override protected def getRunType: RunType = SQL
}
