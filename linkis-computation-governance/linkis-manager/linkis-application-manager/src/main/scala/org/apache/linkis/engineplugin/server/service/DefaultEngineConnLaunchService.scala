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

package org.apache.linkis.engineplugin.server.service

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineplugin.server.loader.EngineConnPluginsLoader
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.launch.entity.{
  EngineConnBuildRequest,
  EngineConnLaunchRequest
}
import org.apache.linkis.manager.engineplugin.common.launch.process.{
  EngineConnResourceGenerator,
  JavaProcessEngineConnLaunchBuilder
}
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary._
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
class DefaultEngineConnLaunchService extends EngineConnLaunchService with Logging {

  @Autowired
  private var engineConnResourceGenerator: EngineConnResourceGenerator = _

  private def getEngineLaunchBuilder(
      engineTypeLabel: EngineTypeLabel,
      engineBuildRequest: EngineConnBuildRequest
  ): EngineConnLaunchBuilder = {
    val engineConnPluginInstance =
      EngineConnPluginsLoader.getEngineConnPluginsLoader().getEngineConnPlugin(engineTypeLabel)
    val builder = engineConnPluginInstance.plugin.getEngineConnLaunchBuilder
    builder match {
      case javaProcessEngineConnLaunchBuilder: JavaProcessEngineConnLaunchBuilder =>
        javaProcessEngineConnLaunchBuilder.setEngineConnResourceGenerator(
          engineConnResourceGenerator
        )
    }
    builder.setBuildRequest(engineBuildRequest)
    builder
  }

  @Receiver
  override def createEngineConnLaunchRequest(
      engineBuildRequest: EngineConnBuildRequest
  ): EngineConnLaunchRequest = {
    val engineTypeOption = engineBuildRequest.labels.asScala.find(_.isInstanceOf[EngineTypeLabel])
    if (engineTypeOption.isDefined) {
      val engineTypeLabel = engineTypeOption.get.asInstanceOf[EngineTypeLabel]
      Utils.tryCatch(
        getEngineLaunchBuilder(engineTypeLabel, engineBuildRequest).buildEngineConn()
      ) { t =>
        logger.error(s"Failed to createEngineConnLaunchRequest(${engineBuildRequest.ticketId})", t)
        throw new EngineConnPluginErrorException(
          FAILED_CREATE_ELR.getErrorCode,
          s"${FAILED_CREATE_ELR.getErrorDesc}, ${ExceptionUtils.getRootCauseMessage(t)}"
        )
      }
    } else {
      throw new EngineConnPluginErrorException(
        ETL_REQUESTED.getErrorCode,
        ETL_REQUESTED.getErrorDesc
      )
    }
  }

}
