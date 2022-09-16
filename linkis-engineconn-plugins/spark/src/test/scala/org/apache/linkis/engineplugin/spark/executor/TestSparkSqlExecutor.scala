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

package org.apache.linkis.engineplugin.spark.executor

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.conf.DWCArgumentsParser
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.factory.SparkEngineConnFactory

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

import org.junit.jupiter.api.{Assertions, Test}

class TestSparkSqlExecutor {

  def initService(port: String): Unit = {
    System.setProperty("wds.linkis.server.version", "v1")
    System.setProperty(
      "wds.linkis.engineconn.plugin.default.class",
      "org.apache.linkis.engineplugin.spark.SparkEngineConnPlugin"
    )
    val map = new mutable.HashMap[String, String]()
    map.put("spring.mvc.servlet.path", "/api/rest_j/v1")
    map.put("server.port", port)
    map.put("spring.application.name", "SparkSqlExecutor")
    map.put("eureka.client.register-with-eureka", "false")
    map.put("eureka.client.fetch-registry", "false")
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(map.toMap))
  }

  @Test
  def testCreateContext: Unit = {
    initService("26378")
    val engineFactory = new SparkEngineConnFactory
    val sparkConf = new SparkConf(true)
    val path = this.getClass.getResource("/").getPath
    System.setProperty("java.io.tmpdir", path)
    val sparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("testSparkSqlExecutor")
      .getOrCreate()
    val outputDir = engineFactory.createOutputDir(sparkConf)
    val sparkEngineSession = SparkEngineSession(
      sparkSession.sparkContext,
      sparkSession.sqlContext,
      sparkSession,
      outputDir
    )
    val sparkSqlExecutor = new SparkSqlExecutor(sparkEngineSession, 1L)
    Assertions.assertFalse(sparkSqlExecutor.isEngineInitialized)
    sparkSqlExecutor.init()
    Assertions.assertTrue(sparkSqlExecutor.isEngineInitialized)
    val engineExecutionContext = new EngineExecutionContext(sparkSqlExecutor, Utils.getJvmUser)
    val code = "select * from temp"
    val response = sparkSqlExecutor.executeLine(engineExecutionContext, code)
    Assertions.assertNotNull(response)
  }

  @Test
  def testShowDF: Unit = {
    if (!FsPath.WINDOWS) {
      initService("26379")
      val engineFactory = new SparkEngineConnFactory
      val sparkConf: SparkConf = new SparkConf(true)
      val path = this.getClass.getResource("/").getPath
      System.setProperty("HADOOP_CONF_DIR", path)
      System.setProperty("wds.linkis.filesystem.hdfs.root.path", path)
      System.setProperty("java.io.tmpdir", path)
      val sparkSession = SparkSession
        .builder()
        .master("local[1]")
        .appName("testShowDF")
        .getOrCreate()
      val outputDir = engineFactory.createOutputDir(sparkConf)
      val sparkEngineSession = SparkEngineSession(
        sparkSession.sparkContext,
        sparkSession.sqlContext,
        sparkSession,
        outputDir
      )
      val sparkScalaExecutor = new SparkScalaExecutor(sparkEngineSession, 1L)
      val engineExecutionContext = new EngineExecutionContext(sparkScalaExecutor, Utils.getJvmUser)
      val dataFrame = sparkSession
        .createDataFrame(
          Seq(("ming", 20, 15552211521L), ("hong", 19, 13287994007L), ("zhi", 21, 15552211523L))
        )
        .toDF("name", "age", "phone")
      SQLSession.showDF(
        sparkSession.sparkContext,
        "test",
        dataFrame,
        "",
        10,
        engineExecutionContext
      )
    }
  }

}
