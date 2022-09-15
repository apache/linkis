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

package org.apache.linkis.engineconn.computation.executor.upstream.access

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.{CommonVars, DWCArgumentsParser}
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.creation.DefaultEngineCreationContext
import org.apache.linkis.engineconn.core.util.EngineConnUtils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.utils.EngineConnArgumentsParser
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment
import org.apache.linkis.manager.label.builder.factory.{
  LabelBuilderFactory,
  LabelBuilderFactoryContext
}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.lang3.StringUtils

import java.util

import org.slf4j.{Logger, LoggerFactory}

object ECTaskEntranceInfoAccessHelper {
  val logger: Logger = LoggerFactory.getLogger(ECTaskEntranceInfoAccessHelper.getClass)

  val engineCreationContext = new DefaultEngineCreationContext
  val labelBuilderFactory: LabelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  def initApp(args: Array[String]): Unit = {
    val arguments = EngineConnArgumentsParser.getEngineConnArgumentsParser.parseToObj(args)
    val engineConf = arguments.getEngineConnConfMap
    engineCreationContext.setUser(engineConf.getOrElse("user", Utils.getJvmUser))
    engineCreationContext.setTicketId(engineConf.getOrElse("ticketId", ""))
    val host = CommonVars(Environment.ECM_HOST.toString, "127.0.0.1").getValue
    val port = CommonVars(Environment.ECM_PORT.toString, "80").getValue
    engineCreationContext.setEMInstance(
      ServiceInstance(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue, s"$host:$port")
    )
    val labels = new util.ArrayList[Label[_]]
    val labelArgs = engineConf.filter(_._1.startsWith(EngineConnArgumentsParser.LABEL_PREFIX))
    if (labelArgs.nonEmpty) {
      labelArgs.foreach { case (key, value) =>
        labels.add(
          labelBuilderFactory
            .createLabel[Label[_]](key.replace(EngineConnArgumentsParser.LABEL_PREFIX, ""), value)
        )
      }
      engineCreationContext.setLabels(labels)
    }
    val jMap = new java.util.HashMap[String, String](engineConf.size)
    engineConf.foreach(kv => jMap.put(kv._1, kv._2))
    engineCreationContext.setOptions(jMap)
    engineCreationContext.setArgs(args)
    //    EngineConnObject.setEngineCreationContext(engineCreationContext)
    logger.info(
      "Finished to init engineCreationContext: " + EngineConnUtils.GSON
        .toJson(engineCreationContext)
    )

    logger.info("Spring is enabled, now try to start SpringBoot.")
    logger.info("<--------------------Start SpringBoot App-------------------->")
    val parser = DWCArgumentsParser.parse(engineCreationContext.getArgs)
    DWCArgumentsParser.setDWCOptionMap(parser.getDWCConfMap)
    val existsExcludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.getValue
    if (!StringUtils.isEmpty(existsExcludePackages)) {
      DataWorkCloudApplication.setProperty(
        ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.key,
        existsExcludePackages
      )
    }
    // 加载spring类
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(parser.getSpringConfMap))

    logger.info("<--------------------SpringBoot App init succeed-------------------->")
  }

}
