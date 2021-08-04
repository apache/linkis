package com.webank.wedatasphere.linkis.engineconnplugin.flink.factory

import com.webank.wedatasphere.linkis.engineconn.common.conf.EngineConnConf
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationExecutorFactory
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.executor.FlinkSQLComputationExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.Label

import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType
import org.apache.flink.yarn.configuration.YarnConfigOptions

import scala.collection.JavaConversions._

/**
  * Created by enjoyyin on 2021/4/12.
  */
class FlinkSQLExecutorFactory extends ComputationExecutorFactory {

  override protected def newExecutor(id: Int,
                           engineCreationContext: EngineCreationContext,
                           engineConn: EngineConn,
                           labels: Array[Label[_]]): ComputationExecutor = engineConn.getEngineConnSession match {
    case context: FlinkEngineConnContext =>
      context.getEnvironmentContext.getFlinkConfig.set(YarnConfigOptions.PROPERTIES_FILE_LOCATION, EngineConnConf.getWorkHome)
      val executor = new FlinkSQLComputationExecutor(id, context)
//      val containsEnvLabel = if(labels != null) labels.exists(_.isInstanceOf[EnvLabel]) else false
//      if(!containsEnvLabel) {
//        executor.getExecutorLabels().add(getEnvLabel(engineCreationContext))
//      }
//      if(executor.getEnvLabel.getEnvType == EnvLabel.DEV) {
//        context.getEnvironmentContext.getDefaultEnv
//          .setExecution(Map("max-table-reFlinkSQLComputationExecutorsult-rows" -> FlinkEnvConfiguration.FLINK_SQL_DEV_SELECT_MAX_LINES.getValue.asInstanceOf[Object]))
//      }
      executor
  }



  override protected def getRunType: RunType = RunType.SQL
}
