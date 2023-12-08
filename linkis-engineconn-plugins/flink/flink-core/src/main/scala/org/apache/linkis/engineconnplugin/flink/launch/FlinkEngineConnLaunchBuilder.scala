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

package org.apache.linkis.engineconnplugin.flink.launch

import org.apache.linkis.common.utils.JsonUtils
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import org.apache.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.{
  Environment,
  JavaProcessEngineConnLaunchBuilder
}
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment.{
  variable,
  PWD,
  USER
}
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants.{
  addPathToClassPath,
  CLASS_PATH_SEPARATOR
}
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel

import java.util

import scala.collection.JavaConverters._

class FlinkEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override protected def getCommands(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] = {
    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    properties.put(
      EnvConfiguration.ENGINE_CONN_MEMORY.key,
      FlinkResourceConfiguration.LINKIS_FLINK_CLIENT_MEMORY.getValue(properties) + "M"
    )
    super.getCommands
  }

  override protected def getBmlResources(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.List[BmlResource] = {
    val bmlResources = new util.ArrayList[BmlResource](super.getBmlResources)
    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    val userName = engineConnBuildRequest.labels.asScala
      .find(_.isInstanceOf[UserCreatorLabel])
      .map { case label: UserCreatorLabel => label.getUser }
      .get
    properties.get("flink.app.main.class.jar.bml.json") match {
      case mainClassJarContent: String =>
        val bmlResource = contentToBmlResource(userName, mainClassJarContent)
        logger.info(
          s"Add a BmlResource(${bmlResource.getFileName}, ${bmlResource.getResourceId}, ${bmlResource.getVersion}) for user $userName and ticketId ${engineConnBuildRequest.ticketId}."
        )
        bmlResources.add(bmlResource)
        properties.remove("flink.app.main.class.jar.bml.json")
      case _ =>
    }
    properties.get("flink.app.user.class.path.bml.json") match {
      case classpathContent: String =>
        val contentList = JsonUtils.jackson.readValue(
          classpathContent,
          classOf[util.List[util.Map[String, Object]]]
        )
        contentList.asScala.map(contentToBmlResource(userName, _)).foreach { bmlResource =>
          logger.info(
            s"Add a BmlResource(${bmlResource.getFileName}, ${bmlResource.getResourceId}, ${bmlResource.getVersion}) for user $userName and ticketId ${engineConnBuildRequest.ticketId}."
          )
          bmlResources.add(bmlResource)
        }
        properties.remove("flink.app.user.class.path.bml.json")
      case _ =>
    }
    bmlResources
  }

  override def getEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.Map[String, String] = {
    val environment = new util.HashMap[String, String]
    addPathToClassPath(environment, variable(PWD))
    val linkisEnvironment = super.getEnvironment
    val linkisClassPath = linkisEnvironment.get(Environment.CLASSPATH.toString)
    val v = environment.get(Environment.CLASSPATH.toString) + CLASS_PATH_SEPARATOR + linkisClassPath
    environment.put(Environment.CLASSPATH.toString, v)
    logger.info(environment.asScala.map(e => s"${e._1}->${e._2}").mkString(","))
    environment
  }

  private def contentToBmlResource(userName: String, content: String): BmlResource = {
    val contentMap = JsonUtils.jackson.readValue(content, classOf[util.Map[String, Object]])
    contentToBmlResource(userName, contentMap)
  }

  private def contentToBmlResource(
      userName: String,
      contentMap: util.Map[String, Object]
  ): BmlResource = {
    val bmlResource = new BmlResource
    bmlResource.setFileName(contentMap.get("fileName").asInstanceOf[String])
    bmlResource.setResourceId(contentMap.get("resourceId").asInstanceOf[String])
    bmlResource.setVersion(contentMap.get("version").asInstanceOf[String])
    bmlResource.setOwner(userName)
    bmlResource.setVisibility(BmlResource.BmlResourceVisibility.Private)
    bmlResource
  }

  override protected def getNecessaryEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] =
    Array(FLINK_HOME_ENV, FLINK_CONF_DIR_ENV) ++: super.getNecessaryEnvironment

  override protected def getExtractJavaOpts: String = {
    if (!HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue) super.getExtractJavaOpts
    else super.getExtractJavaOpts + s" -DHADOOP_PROXY_USER=${variable(USER)}".trim
  }

  override protected def ifAddHiveConfigPath: Boolean = true

}
