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

class TestSparkConfiguration {

  @Test
  def tstSparkConfiguration: Unit = {
    Assertions.assertEquals(100, SparkConfiguration.PROCESS_MAX_THREADS.getValue)
    Assertions.assertEquals("yarn", SparkConfiguration.SPARK_MASTER.getValue)
    Assertions.assertEquals(
      "/appcom/config/spark-config",
      SparkConfiguration.SPARK_CONF_DIR.getValue
    )
    Assertions.assertEquals("/appcom/Install/spark", SparkConfiguration.SPARK_HOME.getValue)
    Assertions.assertEquals("spark-submit", SparkConfiguration.SPARK_SUBMIT_PATH.getValue)
    Assertions.assertEquals(
      "linkis-engineconn-core-1.2.0.jar",
      SparkConfiguration.DEFAULT_SPARK_JAR_NAME.getValue
    )
    Assertions.assertEquals(5000, SparkConfiguration.DOLPHIN_LIMIT_LEN.getValue)
    Assertions.assertEquals(true, SparkConfiguration.ENABLE_REPLACE_PACKAGE_NAME.getValue)
    Assertions.assertEquals(
      "linkis-engineconn-core-1.2.0.jar",
      SparkConfiguration.ENGINE_JAR.getValue
    )

    Assertions.assertEquals(false, SparkConfiguration.MAPRED_OUTPUT_COMPRESS.getValue)
    Assertions.assertEquals(
      "error writing class;OutOfMemoryError",
      SparkConfiguration.ENGINE_SHUTDOWN_LOGS.getValue
    )
    Assertions.assertEquals(true, SparkConfiguration.IS_VIEWFS_ENV.getValue)
    Assertions.assertEquals(true, SparkConfiguration.LINKIS_SPARK_USEHIVECONTEXT.getValue)

    Assertions.assertEquals(
      "/appcom/Install/anaconda3/bin/python",
      SparkConfiguration.PYSPARK_PYTHON3_PATH.getValue
    )
    Assertions.assertEquals(
      "linkis-ps-datasource",
      SparkConfiguration.MDQ_APPLICATION_NAME.getValue
    )
    Assertions.assertEquals(3000L, SparkConfiguration.SQL_EXTENSION_TIMEOUT.getValue)
    Assertions.assertEquals(30, SparkConfiguration.SPARK_NF_FRACTION_LENGTH.getValue)
    Assertions.assertEquals(Int.MaxValue, SparkConfiguration.SHOW_DF_MAX_RES.getValue)
    Assertions.assertEquals(
      "com.webank.wedatasphere.linkis",
      SparkConfiguration.REPLACE_PACKAGE_HEADER.getValue
    )
  }

}
