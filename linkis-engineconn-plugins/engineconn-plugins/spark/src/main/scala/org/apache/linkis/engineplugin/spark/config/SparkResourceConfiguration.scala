/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineplugin.spark.config

import org.apache.linkis.common.conf.CommonVars


/**
  *
  */
object SparkResourceConfiguration {

  val LINKIS_SPARK_DRIVER_MEMORY = CommonVars[String]("spark.driver.memory", "2g")
  val LINKIS_SPARK_DRIVER_CORES = CommonVars[Int]("spark.driver.cores", 1)

  val LINKIS_SPARK_EXECUTOR_MEMORY = CommonVars[String]("spark.executor.memory", "4g")
  val LINKIS_SPARK_EXECUTOR_CORES = CommonVars[Int]("spark.executor.cores", 2)
  val LINKIS_SPARK_EXECUTOR_INSTANCES = CommonVars[Int]("spark.executor.instances", 3)
  val LINKIS_QUEUE_NAME = CommonVars[String]("wds.linkis.rm.yarnqueue", "default")

}
