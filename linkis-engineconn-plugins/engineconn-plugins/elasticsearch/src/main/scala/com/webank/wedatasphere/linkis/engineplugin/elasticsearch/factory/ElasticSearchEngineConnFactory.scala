package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.factory

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.{ExecutorFactory, MultiExecutorEngineConnFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType

class ElasticSearchEngineConnFactory extends MultiExecutorEngineConnFactory with Logging {
  private val executorFactoryArray =   Array[ExecutorFactory](new ElasticSearchJsonExecutorFactory, new ElasticSearchSqlExecutorFactory)

  override def getExecutorFactories: Array[ExecutorFactory] = {
    executorFactoryArray
  }

  override protected def getDefaultExecutorFactoryClass: Class[_ <: ExecutorFactory] =
    classOf[ElasticSearchJsonExecutorFactory]

  override protected def getEngineConnType: EngineType = EngineType.ELASTICSEARCH

  override protected def createEngineConnSession(engineCreationContext: EngineCreationContext): Any = null
}
