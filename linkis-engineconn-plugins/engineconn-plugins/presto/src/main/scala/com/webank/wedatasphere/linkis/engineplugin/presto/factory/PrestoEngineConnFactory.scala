package com.webank.wedatasphere.linkis.engineplugin.presto.factory

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.engineconn.executor.entity.LabelExecutor
import com.webank.wedatasphere.linkis.engineplugin.presto.conf.PrestoConfiguration
import com.webank.wedatasphere.linkis.engineplugin.presto.executer.PrestoEngineConnExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineType, RunType}

class PrestoEngineConnFactory extends ComputationSingleExecutorEngineConnFactory {

  override def newExecutor(id: Int, engineCreationContext: EngineCreationContext, engineConn: EngineConn): LabelExecutor = {
    new PrestoEngineConnExecutor(PrestoConfiguration.ENGINE_DEFAULT_LIMIT.getValue, id)
  }

  override protected def getEngineConnType: EngineType = EngineType.PRESTO

  override protected def getRunType: RunType = RunType.PRESTO_SQL

}
