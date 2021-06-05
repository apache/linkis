package com.webank.wedatasphere.linkis.engineconn.once.executor.creation

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.engineconn.core.creation.AbstractCodeLanguageLabelExecutorFactory
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutor
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.{MultiExecutorEngineConnFactory, SingleExecutorEngineConnFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineConnMode._
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineConnModeLabel

/**
  * Created by enjoyyin on 2021/5/2.
  */
trait OnceExecutorFactory extends AbstractCodeLanguageLabelExecutorFactory {

  override protected def newExecutor(id: Int,
                                     engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn,
                                     labels: Array[Label[_]]): OnceExecutor

  override def canCreate(labels: Array[Label[_]]): Boolean =
    super.canCreate(labels) || (labels.exists {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode: EngineConnMode = engineConnModeLabel.getEngineConnMode
        Array(Once, Computation_With_Once, Once_With_Cluster).contains(mode)
      case _ => false
    } && onlyOneOnceExecutorFactory())

  private def onlyOneOnceExecutorFactory(): Boolean = EngineConnObject.getEngineConnPlugin.getEngineConnFactory match {
    case _: SingleExecutorEngineConnFactory with OnceExecutorFactory => true
    case engineConnFactory: MultiExecutorEngineConnFactory =>
      engineConnFactory.getExecutorFactories.count(_.isInstanceOf[OnceExecutorFactory]) == 1
    case _ => false
  }

}
