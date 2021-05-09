package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.factory

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.MultiExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType

class ElasticSearchEngineConnFactory extends MultiExecutorEngineConnFactory with Logging{

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val engineConn = new DefaultEngineConn(engineCreationContext)
    engineConn.setEngineType(EngineType.ELASTICSEARCH.toString)
    engineConn
  }

}
