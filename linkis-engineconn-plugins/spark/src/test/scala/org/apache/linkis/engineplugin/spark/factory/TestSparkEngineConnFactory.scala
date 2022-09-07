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

package org.apache.linkis.engineplugin.spark.factory

import org.apache.spark.SparkConf

import org.junit.jupiter.api.{Assertions, BeforeEach, Test}

class TestSparkEngineConnFactory {
  private var engineFactory: SparkEngineConnFactory = _

  @BeforeEach
  def before(): Unit = {
    engineFactory = new SparkEngineConnFactory
  }

  @Test
  def testCreateContext: Unit = {
    val sparkConf: SparkConf = new SparkConf(true)
    sparkConf.setAppName("test").setMaster("local[1]")
    val outputDir = engineFactory.createOutputDir(sparkConf)
    Assertions.assertNotNull(outputDir)
    val sparkSession = engineFactory.createSparkSession(outputDir, sparkConf)
    Assertions.assertNotNull(sparkSession)
  }

}
