/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconnplugin.sqoop.executor

import com.webank.wedatasphere.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor, YarnExecutor}
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.context.SqoopEnvConfiguration.LINKIS_QUEUE_NAME
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.exception.JobExecutionException
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.context.SqoopEngineConnContext
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.label.entity.Label

import java.util

trait SqoopExecutor extends YarnExecutor with LabelExecutor with ResourceExecutor{
  private var yarnMode: String = "Client"
  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]
  override def getApplicationId: String = Sqoop.getApplicationId

  override def getApplicationURL: String = Sqoop.getApplicationURL

  override def getYarnMode: String = yarnMode
  def setYarnMode(yarnMode: String): Unit = this.yarnMode = yarnMode

  override def getQueue: String = LINKIS_QUEUE_NAME.getValue

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = throw new JobExecutionException("Not support method for requestExpectedResource.")

  protected val sqoopEngineConnContext: SqoopEngineConnContext
}
