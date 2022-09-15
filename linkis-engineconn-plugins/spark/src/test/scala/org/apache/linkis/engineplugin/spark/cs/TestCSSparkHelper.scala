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

package org.apache.linkis.engineplugin.spark.cs

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.executor.SparkScalaExecutor
import org.apache.linkis.engineplugin.spark.factory.SparkEngineConnFactory

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import org.junit.jupiter.api.{Assertions, Test}

class TestCSSparkHelper {

  @Test
  def testCSSparkHelper: Unit = {
    val engineFactory = new SparkEngineConnFactory
    val sparkConf: SparkConf = new SparkConf(true)
    val sparkSession = SparkSession
      .builder()
      .master("local[1]")
      .appName("test")
      .getOrCreate()
    val outputDir = engineFactory.createOutputDir(sparkConf)
    val sparkEngineSession = SparkEngineSession(
      sparkSession.sparkContext,
      sparkSession.sqlContext,
      sparkSession,
      outputDir
    )
    val sparkScalaExecutor = new SparkScalaExecutor(sparkEngineSession, 1L)
    Assertions.assertFalse(sparkScalaExecutor.isEngineInitialized)
    sparkScalaExecutor.init()
    Assertions.assertTrue(sparkScalaExecutor.isEngineInitialized)
    val engineExecutionContext = new EngineExecutionContext(sparkScalaExecutor, Utils.getJvmUser)
    CSSparkHelper.setContextIDInfoToSparkConf(engineExecutionContext, sparkSession.sparkContext)
//    Assertions.assertNotNull(sparkSession.sparkContext.getLocalProperty(CSCommonUtils.CONTEXT_ID_STR))
  }

}
