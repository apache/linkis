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

package org.apache.linkis.manager.engineplugin.common.launch.process

import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.launch.entity._
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary._
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel

import java.util

import scala.collection.JavaConverters._

trait ProcessEngineConnLaunchBuilder extends EngineConnLaunchBuilder {

  protected def getCommands(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String]

  protected def getMaxRetries(implicit engineConnBuildRequest: EngineConnBuildRequest): Int =
    EnvConfiguration.ENGINE_CONN_MAX_RETRIES.getValue

  protected def getEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.Map[String, String]

  protected def getNecessaryEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String]

  protected def getBmlResources(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.List[BmlResource]

  protected def getEngineConnManagerHooks(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.List[String] = new util.ArrayList[String]

  override def buildEngineConn(): EngineConnLaunchRequest = {
    implicit val engineConnBuildRequestImplicit: EngineConnBuildRequest = engineConnBuildRequest
    val bmlResources = new util.ArrayList[BmlResource]
    engineConnBuildRequest match {
      case richer: RicherEngineConnBuildRequest =>
        bmlResources.addAll(util.Arrays.asList(richer.getBmlResources: _*))
      case _ =>
    }
    bmlResources.addAll(getBmlResources)
    val environment = getEnvironment
    engineConnBuildRequest.labels.asScala
      .find(_.isInstanceOf[UserCreatorLabel])
      .map { case label: UserCreatorLabel =>
        CommonProcessEngineConnLaunchRequest(
          engineConnBuildRequest.ticketId,
          getEngineStartUser(label),
          engineConnBuildRequest.labels,
          engineConnBuildRequest.engineResource,
          bmlResources,
          environment,
          getNecessaryEnvironment,
          engineConnBuildRequest.engineConnCreationDesc,
          getEngineConnManagerHooks,
          getCommands,
          getMaxRetries
        )
      }
      .getOrElse(
        throw new EngineConnBuildFailedException(
          UCL_NOT_EXISTS.getErrorCode,
          UCL_NOT_EXISTS.getErrorDesc
        )
      )
  }

  protected def getEngineStartUser(label: UserCreatorLabel): String = {
    label.getUser
  }

}
