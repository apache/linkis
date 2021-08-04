package com.webank.wedatasphere.linkis.engineconnplugin.flink.executor

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.YarnApplicationClusterDescriptorAdapter
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.commons.lang.StringUtils

import scala.concurrent.duration.Duration

/**
  * Created by enjoyyin on 2021/4/21.
  */
class FlinkJarOnceExecutor(override val id: Long,
                           override protected val flinkEngineConnContext: FlinkEngineConnContext)
  extends FlinkOnceExecutor[YarnApplicationClusterDescriptorAdapter] {


  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext,
                        options: Map[String, String]): Unit = {
    val args = FLINK_APPLICATION_ARGS.getValue(options)
    val programArguments = if(StringUtils.isNotEmpty(args)) args.split(" ") else Array.empty[String]
    val mainClass = FLINK_APPLICATION_MAIN_CLASS.getValue(options)
    info(s"Ready to submit flink application, mainClass: $mainClass, args: $args.")
    clusterDescriptor.deployCluster(programArguments, mainClass)
  }

  override protected def waitToRunning(): Unit = {
    Utils.waitUntil(() => clusterDescriptor.initJobId(), Duration.Inf)
    super.waitToRunning()
  }
}