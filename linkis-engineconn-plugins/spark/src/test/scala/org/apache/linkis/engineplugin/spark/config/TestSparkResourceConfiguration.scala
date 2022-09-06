/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineplugin.spark.config

import org.junit.jupiter.api.{Assertions, Test}

class TestSparkResourceConfiguration {

  @Test
  def testSparkResourceConfiguration: Unit = {
    Assertions.assertEquals("2g", SparkResourceConfiguration.LINKIS_SPARK_DRIVER_MEMORY.getValue)
    Assertions.assertEquals(1, SparkResourceConfiguration.LINKIS_SPARK_DRIVER_CORES.getValue)
    Assertions.assertEquals("4g", SparkResourceConfiguration.LINKIS_SPARK_EXECUTOR_MEMORY.getValue)
    Assertions.assertEquals(2, SparkResourceConfiguration.LINKIS_SPARK_EXECUTOR_CORES.getValue)
    Assertions.assertEquals(3, SparkResourceConfiguration.LINKIS_SPARK_EXECUTOR_INSTANCES.getValue)
    Assertions.assertEquals("default", SparkResourceConfiguration.LINKIS_QUEUE_NAME.getValue)
  }

}
