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

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

/**
 * Test for Spark executor params configuration
 */
class TestSparkExecutorParamsConfiguration {

  @Test
  def testSparkExecutorParamsEnabledDefault(): Unit = {
    val enabled = SparkConfiguration.SPARK_EXECUTOR_PARAMS_ENABLED.getValue
    assertFalse(enabled, "SPARK_EXECUTOR_PARAMS_ENABLED should default to false")
  }

  @Test
  def testSparkExecutorParamsExcludeDefault(): Unit = {
    val exclude = SparkConfiguration.SPARK_EXECUTOR_PARAMS_EXCLUDE.getValue
    assertTrue(exclude.isEmpty, "SPARK_EXECUTOR_PARAMS_EXCLUDE should default to empty string")
  }

  @Test
  def testSparkExecutorParamsExcludeSplit(): Unit = {
    val testExclude = "spark.sql.shuffle.partitions,spark.dynamicAllocation.maxExecutors"
    val excludeParams = testExclude.split(",").map(_.trim).filter(_.nonEmpty).toSet

    assertEquals(2, excludeParams.size, "Should parse 2 excluded params")
    assertTrue(excludeParams.contains("spark.sql.shuffle.partitions"))
    assertTrue(excludeParams.contains("spark.dynamicAllocation.maxExecutors"))
  }

  @Test
  def testSparkExecutorParamsExcludeEmptySplit(): Unit = {
    val testExclude = ""
    val excludeParams = testExclude.split(",").map(_.trim).filter(_.nonEmpty).toSet

    assertEquals(0, excludeParams.size, "Empty exclude should result in empty set")
  }

  @Test
  def testSparkExecutorParamsExcludeWithSpaces(): Unit = {
    val testExclude =
      "spark.executor.instances , spark.driver.memory , spark.dynamicAllocation.enabled"
    val excludeParams = testExclude.split(",").map(_.trim).filter(_.nonEmpty).toSet

    assertEquals(3, excludeParams.size, "Should parse 3 excluded params with spaces")
    assertFalse(excludeParams.contains(" spark.executor.instances"))
    assertTrue(excludeParams.contains("spark.executor.instances"))
    assertTrue(excludeParams.contains("spark.driver.memory"))
    assertTrue(excludeParams.contains("spark.dynamicAllocation.enabled"))
  }

}
