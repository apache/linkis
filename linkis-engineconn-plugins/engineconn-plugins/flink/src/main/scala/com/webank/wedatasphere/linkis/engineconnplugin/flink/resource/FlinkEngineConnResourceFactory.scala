package com.webank.wedatasphere.linkis.engineconnplugin.flink.resource

import com.webank.wedatasphere.linkis.common.utils.ByteTimeUtils
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration._
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{DriverAndYarnResource, LoadInstanceResource, Resource, YarnResource}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.AbstractEngineResourceFactory

/**
  * Created by enjoyyin on 2021/3/25.
  */
class FlinkEngineConnResourceFactory extends AbstractEngineResourceFactory {

  override def getRequestResource(properties: java.util.Map[String, String]): Resource = {
    val containers = if(properties.containsKey(LINKIS_FLINK_CONTAINERS)) {
      val containers = LINKIS_FLINK_CONTAINERS.getValue(properties)
      properties.put(FLINK_APP_DEFAULT_PARALLELISM.key, String.valueOf(containers * LINKIS_FLINK_TASK_SLOTS.getValue(properties)))
      containers
    } else math.round(FLINK_APP_DEFAULT_PARALLELISM.getValue(properties) * 1.0f / LINKIS_FLINK_TASK_SLOTS.getValue(properties))
    val yarnMemory = ByteTimeUtils.byteStringAsBytes(LINKIS_FLINK_TASK_MANAGER_MEMORY.getValue(properties) * containers + "G") +
      ByteTimeUtils.byteStringAsBytes(LINKIS_FLINK_JOB_MANAGER_MEMORY.getValue(properties) + "G")
    val yarnCores = LINKIS_FLINK_TASK_MANAGER_CPU_CORES.getValue(properties) * containers + 1
    new DriverAndYarnResource(
      new LoadInstanceResource(ByteTimeUtils.byteStringAsBytes(LINKIS_FLINK_CLIENT_MEMORY.getValue(properties) + "G"),
        LINKIS_FLINK_CLIENT_CORES,
        1),
      new YarnResource(yarnMemory, yarnCores, 0, LINKIS_QUEUE_NAME.getValue(properties))
    )
  }

}
