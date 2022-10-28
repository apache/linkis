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

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.executor.SparkScalaExecutor
import org.apache.linkis.engineplugin.spark.factory.SparkEngineConnFactory

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import org.junit.jupiter.api.{Assertions, Test}

class TestCSSparkPostExecutionHook {

  @Test
  def testCreateContext: Unit = {
    val hook = new CSSparkPostExecutionHook
    val hookPre = new CSSparkPreExecutionHook
    val engineFactory = new SparkEngineConnFactory
    val sparkConf: SparkConf = new SparkConf(true)
    val path = this.getClass.getResource("/").getPath
    System.setProperty("java.io.tmpdir", path)
    System.setProperty("wds.linkis.filesystem.hdfs.root.path", path)
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

    if (!FsPath.WINDOWS) {
      sparkScalaExecutor.init()
      Assertions.assertTrue(sparkScalaExecutor.isEngineInitialized)
      val engineExecutionContext = new EngineExecutionContext(sparkScalaExecutor, Utils.getJvmUser)
      val code = "val dataFrame = spark.createDataFrame(Seq(\n      " +
        "(\"ming\", 20, 15552211521L),\n      " +
        "(\"hong\", 19, 13287994007L),\n      " +
        "(\"zhi\", 21, 15552211523L)\n    )).toDF(\"name\", \"age\", \"phone\") \n" +
        "dataFrame.show()\n";
      hookPre.callPreExecutionHook(engineExecutionContext, code)
      val response = sparkScalaExecutor.executeLine(engineExecutionContext, code)
      hook.callPostExecutionHook(engineExecutionContext, response, code)
    }
  }

}
