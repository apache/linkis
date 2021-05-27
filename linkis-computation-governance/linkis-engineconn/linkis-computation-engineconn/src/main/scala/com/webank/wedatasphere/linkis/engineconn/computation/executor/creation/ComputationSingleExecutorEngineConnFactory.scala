package com.webank.wedatasphere.linkis.engineconn.computation.executor.creation

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineconn.core.creation.{AbstractCodeLanguageLabelExecutorFactory, AbstractExecutorFactory}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.SingleLabelExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
  * Created by enjoyyin on 2021/5/1.
  */
trait ComputationSingleExecutorEngineConnFactory
  extends SingleLabelExecutorEngineConnFactory
    with AbstractCodeLanguageLabelExecutorFactory with AbstractExecutorFactory {

  protected override def newExecutor(id: Int,
                            engineCreationContext: EngineCreationContext,
                            engineConn: EngineConn,
                            labels: Array[Label[_]]): ComputationExecutor = null

  override def createExecutor(engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn,
                              labels: Array[Label[_]]): ComputationExecutor =
    createExecutor(engineCreationContext, engineConn)


  override def createExecutor(engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn): ComputationExecutor = {
    super.createExecutor(engineCreationContext, engineConn) match {
      case computationExecutor: ComputationExecutor =>
        computationExecutor.getExecutorLabels().add(getDefaultCodeLanguageLabel)
        computationExecutor
    }
  }

  override protected def createEngineConnSession(engineCreationContext: EngineCreationContext): Any = null

}
