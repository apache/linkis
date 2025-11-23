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

package org.apache.linkis.engineconnplugin.flink.setting

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary._
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.FlinkInitFailedException
import org.apache.linkis.engineconnplugin.flink.context.{EnvironmentContext, FlinkEngineConnContext}

import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.flink.yarn.configuration.YarnConfigOptions
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment
import org.apache.hadoop.yarn.conf.YarnConfiguration

import java.io.File
import java.text.MessageFormat
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable

import com.google.common.collect.Lists

// Used to set Hudi configurations if Hudi is enabled.
class HudiSettings extends Settings with Logging {

  private def setExtraSettings(
      properties: mutable.Map[String, String],
      prefix: String,
      set: (String, String) => Unit
  ): Unit = {
    properties.filter(_._1.startsWith(prefix)).foreach { case (key, value) =>
      val realKey = key.substring(prefix.length)
      logger.info(s"set $realKey=$value.")
      set(realKey, value)
    }
  }

  override def setEnvironmentContext(
      engineCreationContext: EngineCreationContext,
      context: EnvironmentContext
  ): Unit = {
    if (!HudiSettings.HUDI_ENABLE.getValue(engineCreationContext.getOptions)) {
      return
    }
    logger.info("hudi is enabled, now try to set hudi configurations...")
    val shipFiles = context.getFlinkConfig.get(YarnConfigOptions.SHIP_FILES)
    val hudiJarPaths =
      Lists.newArrayList(HudiSettings.getHudiJarPaths(engineCreationContext.getOptions): _*)
    if (CollectionUtils.isEmpty(hudiJarPaths)) {
      throw new FlinkInitFailedException(HUDIJARS_NOT_EXISTS.getErrorDesc)
    }
    logger.info(s"hudi jar is in $hudiJarPaths.")
    context.getDependencies.addAll(
      hudiJarPaths.asScala.map(path => new File(path).toURI.toURL).asJava
    )
    if (CollectionUtils.isEmpty(shipFiles)) {
      context.getFlinkConfig.set(YarnConfigOptions.SHIP_FILES, hudiJarPaths)
    } else {
      val newShipFiles = new util.ArrayList[String](shipFiles)
      newShipFiles.addAll(hudiJarPaths)
      context.getFlinkConfig.set(YarnConfigOptions.SHIP_FILES, newShipFiles)
    }
    setExtraSettings(
      CommonVars.properties.asScala,
      HudiSettings.FLINK_HUDI_PREFIX,
      context.getFlinkConfig.setString
    )
    setExtraSettings(
      engineCreationContext.getOptions.asScala,
      HudiSettings.FLINK_HUDI_PREFIX,
      context.getFlinkConfig.setString
    )
  }

  override def setExecutionContext(
      engineCreationContext: EngineCreationContext,
      context: FlinkEngineConnContext
  ): Unit = {
    if (!HudiSettings.HUDI_ENABLE.getValue(engineCreationContext.getOptions)) {
      return
    }
    val configuration = context.getExecutionContext.getClusterClientFactory.getYarnConfiguration(
      context.getExecutionContext.getFlinkConfig
    )
    val classpath = YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH.toBuffer
    classpath += (Environment.HADOOP_COMMON_HOME.$() + "/share/hadoop/mapreduce/*")
    val addExtraClasspath: String => Unit = extraClasspath =>
      if (StringUtils.isNotBlank(extraClasspath)) {
        classpath ++= extraClasspath.split(",")
      }
    addExtraClasspath(
      HudiSettings.HUDI_EXTRA_YARN_CLASSPATH.getValue(engineCreationContext.getOptions)
    )
    addExtraClasspath(HudiSettings.HUDI_EXTRA_YARN_CLASSPATH.getValue)
    configuration.set(YarnConfiguration.YARN_APPLICATION_CLASSPATH, classpath.toSet.mkString(","))
    logger.info(
      s"set ${YarnConfiguration.YARN_APPLICATION_CLASSPATH}=${configuration.get(YarnConfiguration.YARN_APPLICATION_CLASSPATH)}."
    )
    setExtraSettings(
      CommonVars.properties.asScala,
      HudiSettings.FLINK_YARN_PREFIX,
      configuration.set
    )
    setExtraSettings(
      engineCreationContext.getOptions.asScala,
      HudiSettings.FLINK_YARN_PREFIX,
      configuration.set
    )
  }

}

object HudiSettings {
  private val FLINK_HUDI_PREFIX = "_FLINK_HUDI_."
  private val FLINK_YARN_PREFIX = "_FLINK_HUDI_YARN_."
  private val HUDI_ENABLE = CommonVars("linkis.flink.hudi.enable", false)
  private val HUDI_EXTRA_YARN_CLASSPATH = CommonVars("linkis.flink.hudi.extra.yarn.classpath", "")

  private def getHudiJarPaths(options: util.Map[String, String]): Array[String] = {
    val hudiJarPath = CommonVars("linkis.flink.hudi.jar.path", "").getValue(options)
    if (StringUtils.isNotBlank(hudiJarPath)) {
      return hudiJarPath.split(",")
    }
    val lib = new File(EngineConnConf.getWorkHome, "lib")
    if (!lib.exists() || !lib.isDirectory) {
      throw new FlinkInitFailedException(MessageFormat.format(PATH_NOT_EXIST.getErrorDesc, lib))
    }
    lib
      .listFiles()
      .filter(file => file.getName.startsWith("hudi-") && file.getName.endsWith(".jar"))
      .map(_.getPath)
  }

}
