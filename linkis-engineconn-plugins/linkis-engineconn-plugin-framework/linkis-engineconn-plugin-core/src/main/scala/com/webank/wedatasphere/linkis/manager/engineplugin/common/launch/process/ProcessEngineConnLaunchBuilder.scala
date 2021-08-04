/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process

import java.util

import com.webank.wedatasphere.linkis.engineconn.common.conf.EngineConnConf
import com.webank.wedatasphere.linkis.manager.common.protocol.bml.BmlResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EnvConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity._
import com.webank.wedatasphere.linkis.manager.label.entity.engine.UserCreatorLabel

import scala.collection.JavaConversions._


trait ProcessEngineConnLaunchBuilder extends EngineConnLaunchBuilder {

  protected def getCommands(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String]

  protected def getMaxRetries(implicit engineConnBuildRequest: EngineConnBuildRequest): Int = EnvConfiguration.ENGINE_CONN_MAX_RETRIES.getValue

  protected def getEnvironment(implicit engineConnBuildRequest: EngineConnBuildRequest): util.Map[String, String]

  protected def getNecessaryEnvironment(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String]

  protected def getBmlResources(implicit engineConnBuildRequest: EngineConnBuildRequest): util.List[BmlResource]

  protected def getEngineConnManagerHooks(implicit engineConnBuildRequest: EngineConnBuildRequest): util.List[String] = new util.ArrayList[String]

  override def buildEngineConn(engineConnBuildRequest: EngineConnBuildRequest): EngineConnLaunchRequest = {
    implicit val engineConnBuildRequestImplicit: EngineConnBuildRequest = engineConnBuildRequest
    val bmlResources = new util.ArrayList[BmlResource]
    engineConnBuildRequest match {
      case richer: RicherEngineConnBuildRequest => bmlResources.addAll(util.Arrays.asList(richer.getBmlResources: _*))
      case _ =>
    }
    bmlResources.addAll(getBmlResources)
    val environment = getEnvironment
    engineConnBuildRequest.labels.find(_.isInstanceOf[UserCreatorLabel]).map {
      case label: UserCreatorLabel =>
        CommonProcessEngineConnLaunchRequest(engineConnBuildRequest.ticketId, getEngineStartUser(label),
          engineConnBuildRequest.labels, engineConnBuildRequest.engineResource, bmlResources, environment, getNecessaryEnvironment,
          engineConnBuildRequest.engineConnCreationDesc, getEngineConnManagerHooks, getCommands, getMaxRetries)
    }.getOrElse(throw new EngineConnBuildFailedException(20000, "UserCreatorLabel is not exists."))
  }

  protected def getEngineStartUser(label: UserCreatorLabel): String = {
    label.getUser
  }

}
