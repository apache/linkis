/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.enginemanager.configuration

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * Created by allenlliu on 2019/4/8.
  */
object SparkResourceConfiguration {
  val DWC_SPARK_DRIVER_MEMORY = CommonVars[Int]("spark.driver.memory", 2)  //单位为G
  val DWC_SPARK_DRIVER_CORES = 1 //Fixed to 1（固定为1） CommonVars[Int]("wds.linkis.driver.cores", 1)

  val DWC_SPARK_EXECUTOR_MEMORY = CommonVars[Int]("spark.executor.memory", 4) //单位为G
  val DWC_SPARK_EXECUTOR_CORES = CommonVars[Int]("spark.executor.cores", 2)
  val DWC_SPARK_EXECUTOR_INSTANCES = CommonVars[Int]("spark.executor.instances", 3)
  val DWC_QUEUE_NAME = CommonVars[String]("wds.linkis.yarnqueue", "ide")
}
