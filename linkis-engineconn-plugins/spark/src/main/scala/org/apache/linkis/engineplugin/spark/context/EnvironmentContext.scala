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

package org.apache.linkis.engineplugin.spark.context

import org.apache.linkis.engineplugin.spark.client.context.SparkConfig

import java.net.URL
import java.util
import java.util.Objects

class EnvironmentContext(
    yarnConfDir: String,
    sparkConfDir: String,
    sparkHome: String,
    dependencies: util.List[URL]
) {

  private var sparkConfig: SparkConfig = _

  def this(
      sparkConfig: SparkConfig,
      yarnConfDir: String,
      sparkConfDir: String,
      sparkHome: String,
      dependencies: util.List[URL]
  ) {
    this(yarnConfDir, sparkConfDir, sparkHome, dependencies)
    this.sparkConfig = sparkConfig
  }

  def getYarnConfDir: String = yarnConfDir

  def getSparkConfDir: String = sparkConfDir

  def getSparkHome: String = sparkHome

  def getSparkConfig: SparkConfig = sparkConfig

  def getDependencies: util.List[URL] = dependencies

  override def equals(o: Any): Boolean = o match {
    case context: EnvironmentContext =>
      if (this eq context) return true
      Objects.equals(dependencies, context.getDependencies) &&
      Objects.equals(sparkConfig, context.sparkConfig)
    case _ => false
  }

  override def hashCode: Int = Objects.hash(dependencies, sparkConfig)
}
