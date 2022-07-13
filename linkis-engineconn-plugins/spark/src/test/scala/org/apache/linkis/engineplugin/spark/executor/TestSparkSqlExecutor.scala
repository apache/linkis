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
package org.apache.linkis.engineplugin.spark.executor

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.conf.DWCArgumentsParser
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.factory.SparkEngineConnFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.junit.jupiter.api.{Assertions, Test}

import scala.collection.mutable

class TestSparkSqlExecutor{
  @Test
  def testCreateContext: Unit = {
    System.setProperty("wds.linkis.server.version", "v1")
    val map = new mutable.HashMap[String, String]()
    map.put("spring.mvc.servlet.path", "/api/rest_j/v1")
    map.put("server.port", "26378")
    map.put("spring.application.name", "SparkSqlExecutor")
    map.put("eureka.client.register-with-eureka", "false")
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(map.toMap))
    val engineFactory = new SparkEngineConnFactory
    val sparkConf = new SparkConf(true)
    System.setProperty("java.io.tmpdir", "./")
    val sparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("testSparkSqlExecutor").getOrCreate()
    val outputDir = engineFactory.createOutputDir(sparkConf)
    val sparkEngineSession = SparkEngineSession(sparkSession.sparkContext, sparkSession.sqlContext, sparkSession, outputDir)
    val sparkSqlExecutor = new SparkSqlExecutor(sparkEngineSession, 1L)
    Assertions.assertFalse(sparkSqlExecutor.isEngineInitialized)
    sparkSqlExecutor.init()
    Assertions.assertTrue(sparkSqlExecutor.isEngineInitialized)
    val engineExecutionContext = new EngineExecutionContext(sparkSqlExecutor, Utils.getJvmUser)
    val code = " val spark = SparkSession.builder().appName(\"Test extract\")\n      " +
      ".config(\"spark.some.config.option\", \"some-value\").master(\"local[*]\").getOrCreate()\n    " +
      "val dataFrame = spark.createDataFrame(Seq(\n      " +
      "(\"ming\", 20, 15552211521L),\n      " +
      "(\"hong\", 19, 13287994007L),\n      " +
      "(\"zhi\", 21, 15552211523L)\n    )).toDF(\"name\", \"age\", \"phone\") \n" +
      "dataFrame.show()\n"
    val response = sparkSqlExecutor.executeLine(engineExecutionContext, code)
    Assertions.assertNotNull(response)
  }
}
