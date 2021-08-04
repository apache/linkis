package com.webank.wedatasphere.linkis.engineconnplugin.flink.config

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * Created by enjoyyin on 2021/4/7.
  */
object FlinkResourceConfiguration {

  val LINKIS_FLINK_CLIENT_MEMORY = CommonVars[Int]("flink.client.memory", 4) //单位为G
  val LINKIS_FLINK_CLIENT_CORES = 1 //Fixed to 1（固定为1） CommonVars[Int]("wds.linkis.driver.cores", 1)


  val LINKIS_FLINK_JOB_MANAGER_MEMORY = CommonVars[Int]("flink.jobmanager.memory", 2) //单位为G
  val LINKIS_FLINK_TASK_MANAGER_MEMORY = CommonVars[Int]("flink.taskmanager.memory", 4) //单位为G
  val LINKIS_FLINK_TASK_SLOTS = CommonVars[Int]("flink.taskmanager.numberOfTaskSlots", 2)
  val LINKIS_FLINK_TASK_MANAGER_CPU_CORES = CommonVars[Int]("flink.taskmanager.cpu.cores", 2)
  val LINKIS_FLINK_CONTAINERS = CommonVars[Int]("flink.container.num", 2)
  val LINKIS_QUEUE_NAME = CommonVars[String]("wds.linkis.rm.yarnqueue", "default")


  val FLINK_APP_DEFAULT_PARALLELISM = CommonVars("wds.linkis.engineconn.flink.app.parallelism", 4)

}
