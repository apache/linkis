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

package org.apache.linkis.engineconnplugin.sqoop.executor

import org.apache.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor, YarnExecutor}
import org.apache.linkis.engineconnplugin.sqoop.client.Sqoop
import org.apache.linkis.engineconnplugin.sqoop.client.errorcode.SqoopErrorCodeSummary.NOT_SUPPORT_METHOD
import org.apache.linkis.engineconnplugin.sqoop.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopEngineConnContext
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopResourceConfiguration.LINKIS_QUEUE_NAME
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.label.entity.Label

import java.util

trait SqoopExecutor extends YarnExecutor with LabelExecutor with ResourceExecutor {
  private var yarnMode: String = "Client"
  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]
  override def getApplicationId: String = Sqoop.getApplicationId

  override def getApplicationURL: String = Sqoop.getApplicationURL

  override def getYarnMode: String = yarnMode
  def setYarnMode(yarnMode: String): Unit = this.yarnMode = yarnMode

  override def getQueue: String = LINKIS_QUEUE_NAME.getValue

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource =
    throw new JobExecutionException(NOT_SUPPORT_METHOD.getErrorDesc)

  protected val sqoopEngineConnContext: SqoopEngineConnContext
}
