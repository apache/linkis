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

import org.apache.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor, YarnExecutor}
import org.apache.linkis.engineplugin.spark.context.SparkEngineConnContext
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary
import org.apache.linkis.engineplugin.spark.exception.JobExecutionException
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.label.entity.Label

import java.util

trait SparkExecutor extends YarnExecutor with LabelExecutor with ResourceExecutor {

  private var applicationId: String = _
  private var yarnMode: String = "Client"
  private var queue: String = _

  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]

  override def getApplicationId: String = applicationId

  def setApplicationId(applicationId: String): Unit = this.applicationId = applicationId

  override def getYarnMode: String = yarnMode

  def setYarnMode(yarnMode: String): Unit = this.yarnMode = yarnMode

  override def getQueue: String = queue

  def setQueue(queue: String): Unit = this.queue = queue

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource =
    throw new JobExecutionException(
      SparkErrorCodeSummary.NOT_SUPPORT_METHOD.getErrorCode,
      SparkErrorCodeSummary.NOT_SUPPORT_METHOD.getErrorDesc
    )

  protected val sparkEngineConnContext: SparkEngineConnContext

  queue = sparkEngineConnContext.getEnvironmentContext.getSparkConfig.getQueue

  def supportCallBackLogs(): Boolean = true
}
