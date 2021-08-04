package com.webank.wedatasphere.linkis.engineconnplugin.flink.factory

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutor
import com.webank.wedatasphere.linkis.engineconn.once.executor.creation.OnceExecutorFactory
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.executor.FlinkJarOnceExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType

/**
  * Created by enjoyyin on 2021/4/12.
  */
class FlinkApplicationExecutorFactory extends OnceExecutorFactory {

  override protected def newExecutor(id: Int,
                           engineCreationContext: EngineCreationContext,
                           engineConn: EngineConn,
                           labels: Array[Label[_]]): OnceExecutor = engineConn.getEngineConnSession match {
    case context: FlinkEngineConnContext =>
      new FlinkJarOnceExecutor(id, context)
  }

  override protected def getRunType: RunType = RunType.JAR
}
