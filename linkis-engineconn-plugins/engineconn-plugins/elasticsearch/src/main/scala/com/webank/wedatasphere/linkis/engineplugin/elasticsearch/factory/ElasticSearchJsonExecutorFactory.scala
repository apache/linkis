package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.factory

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationExecutorFactory
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.ElasticSearchEngineConnExecutor
import com.webank.wedatasphere.linkis.governance.common.paser.JsonCodeParser
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType

class ElasticSearchJsonExecutorFactory extends ComputationExecutorFactory {

  override protected def newExecutor(id: Int, engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn, labels: Array[Label[_]]): ComputationExecutor = {
    val executor = new ElasticSearchEngineConnExecutor(ElasticSearchConfiguration.ENGINE_DEFAULT_LIMIT.getValue,
      id, RunType.ES_JSON.toString)
    executor.setCodeParser(new JsonCodeParser)
    executor
  }

  override protected def getRunType: RunType = RunType.ES_JSON

}
