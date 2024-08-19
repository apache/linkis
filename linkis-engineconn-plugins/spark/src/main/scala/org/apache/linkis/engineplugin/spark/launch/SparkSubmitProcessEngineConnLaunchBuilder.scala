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

package org.apache.linkis.engineplugin.spark.launch

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration.{
  ENGINE_JAR,
  LINKIS_SPARK_CONF,
  SPARK_APP_NAME,
  SPARK_DEFAULT_EXTERNAL_JARS_PATH,
  SPARK_DEPLOY_MODE,
  SPARK_DRIVER_CLASSPATH,
  SPARK_DRIVER_EXTRA_JAVA_OPTIONS,
  SPARK_PYTHON_VERSION,
  SPARK_SUBMIT_PATH,
  SPARK_YARN_CLUSTER_JARS
}
import org.apache.linkis.engineplugin.spark.config.SparkResourceConfiguration._
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary
import org.apache.linkis.engineplugin.spark.exception.SparkEngineException
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.manager.common.entity.resource.DriverAndYarnResource
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment._
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder
import org.apache.linkis.manager.label.constant.LabelValueConstant
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.UserWithCreator

import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class SparkSubmitProcessEngineConnLaunchBuilder(builder: JavaProcessEngineConnLaunchBuilder)
    extends Logging {

  def getCommands(
      engineConnBuildRequest: EngineConnBuildRequest,
      mainClass: String,
      gcLogDir: String,
      logDir: String
  ): Array[String] = {
    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    val sparkConf = getValueAndRemove(properties, LINKIS_SPARK_CONF)
    // sparkcsonf DEMO:spark.sql.shuffle.partitions=10;spark.memory.fraction=0.6
    if (StringUtils.isNotBlank(sparkConf)) {
      val strArrary = sparkConf.split(";").toList
      strArrary.foreach { keyAndValue =>
        val key = keyAndValue.split("=")(0).trim
        val value = keyAndValue.split("=")(1).trim
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
          engineConnBuildRequest.engineConnCreationDesc.properties.put(key, value)
        } else {
          logger.warn(s"spark conf has empty value, key:${key}, value:${value}")
        }
      }
    }
    val className = getValueAndRemove(properties, "className", mainClass)
    val driverCores = getValueAndRemove(properties, LINKIS_SPARK_DRIVER_CORES)
    val driverMemory = getValueAndRemove(properties, LINKIS_SPARK_DRIVER_MEMORY)
    val executorCores = getValueAndRemove(properties, LINKIS_SPARK_EXECUTOR_CORES)
    val executorMemory = getValueAndRemove(properties, LINKIS_SPARK_EXECUTOR_MEMORY)
    val numExecutors = getValueAndRemove(properties, LINKIS_SPARK_EXECUTOR_INSTANCES)

    val userEngineResource = engineConnBuildRequest.engineResource
    val darResource = userEngineResource.getLockedResource.asInstanceOf[DriverAndYarnResource]
    val files: ArrayBuffer[String] = getValueAndRemove(properties, "files", "")
      .split(",")
      .filter(isNotBlankPath)
      .toBuffer
      .asInstanceOf[ArrayBuffer[String]]
    val jars = new ArrayBuffer[String]()
    jars ++= getValueAndRemove(properties, "jars", "").split(",").filter(isNotBlankPath)
    jars ++= getValueAndRemove(properties, SPARK_DEFAULT_EXTERNAL_JARS_PATH)
      .split(",")
      .filter(x => {
        val isPath = isNotBlankPath(x)
        // filter by isFile cannot support this case:
        // The cg-linkismanager startup user is inconsistent with the engineconn startup user

        // val isFile = (new java.io.File(x)).isFile
        logger.info(s"file:${x}, check isPath:${isPath}")
        isPath
      })
    val pyFiles = getValueAndRemove(properties, "py-files", "").split(",").filter(isNotBlankPath)
    val archives = getValueAndRemove(properties, "archives", "").split(",").filter(isNotBlankPath)

    val queue = if (null != darResource) {
      darResource.getYarnResource.getQueueName
    } else {
      "default"
    }

    val driverClassPath =
      Array(getValueAndRemove(properties, SPARK_DRIVER_CLASSPATH), variable(CLASSPATH))

    var userWithCreator: UserWithCreator = UserWithCreator("DefaultUser", "DefaultCreator")
    engineConnBuildRequest.labels.asScala.foreach {
      case label: UserCreatorLabel =>
        userWithCreator = UserWithCreator(label.getUser, label.getCreator)
      case _ =>
    }
    val appName = getValueAndRemove(properties, SPARK_APP_NAME) + "_" + userWithCreator.creator

    val commandLine: ArrayBuffer[String] = ArrayBuffer[String]()
    commandLine += SPARK_SUBMIT_PATH.getValue

    def addOpt(option: String, value: String): Unit = {
      if (StringUtils.isNotBlank(value)) {
        commandLine += option
        commandLine += value
      }
    }

    def addProxyUser(): Unit = {
      if (!HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue) return
      val proxyUser = getValueAndRemove(properties, "proxyUser", "")
      if (StringUtils.isNotBlank(proxyUser)) {
        addOpt("--proxy-user", proxyUser)
      } else {
        addOpt("--proxy-user", userWithCreator.user)
      }
    }

    def getMemory(memory: String): String = if (StringUtils.isNumeric(memory)) {
      memory + "g"
    } else {
      memory
    }

    var deployMode: String = SparkConfiguration.SPARK_YARN_CLIENT

    val label = LabelUtil.getEngingeConnRuntimeModeLabel(engineConnBuildRequest.labels)
    val isYarnClusterMode: Boolean =
      if (null != label && label.getModeValue.equals(LabelValueConstant.YARN_CLUSTER_VALUE)) true
      else false

    if (isYarnClusterMode) {
      deployMode = SparkConfiguration.SPARK_YARN_CLUSTER
      files ++= Array(s"${variable(PWD)}/conf/linkis-engineconn.properties")

      var clusterJars: String = getValueAndRemove(properties, SPARK_YARN_CLUSTER_JARS)

      if (StringUtils.isBlank(clusterJars)) {
        throw new SparkEngineException(
          SparkErrorCodeSummary.LINKIS_SPARK_YARN_CLUSTER_JARS_ERROR.getErrorCode,
          SparkErrorCodeSummary.LINKIS_SPARK_YARN_CLUSTER_JARS_ERROR.getErrorDesc
        )
      }

      if (clusterJars.endsWith("/")) {
        clusterJars = clusterJars.dropRight(1)
      }
      jars += s"$clusterJars/*"
    }

    addOpt("--master", "yarn")
    addOpt("--deploy-mode", deployMode)
    addOpt("--name", appName)
    addProxyUser()

    if (jars.isEmpty) {
      jars += ""
    }
    jars += variable(UDF_JARS)

    addOpt("--jars", jars.mkString(","))
    addOpt("--py-files", pyFiles.mkString(","))
    addOpt("--files", files.mkString(","))
    addOpt("--archives", archives.mkString(","))
    addOpt("--driver-class-path", driverClassPath.mkString(":"))
    addOpt("--driver-memory", getMemory(driverMemory))
    addOpt("--driver-cores", driverCores.toString)
    addOpt("--executor-memory", getMemory(executorMemory))
    addOpt("--executor-cores", executorCores.toString)
    addOpt("--num-executors", numExecutors.toString)
    addOpt("--queue", queue)

    getConf(engineConnBuildRequest, gcLogDir, logDir, isYarnClusterMode).foreach {
      case (key, value) =>
        addOpt("--conf", s"""$key="$value"""")
    }

    addOpt("--class", className)
    addOpt("1>", s"${variable(LOG_DIRS)}/stdout")
    addOpt("2>>", s"${variable(LOG_DIRS)}/stderr")
    addOpt("", s" ${variable(PWD)}/lib/${ENGINE_JAR.getValue}")

    commandLine.toArray.filter(StringUtils.isNotEmpty)
  }

  def getConf(
      engineConnBuildRequest: EngineConnBuildRequest,
      gcLogDir: String,
      logDir: String,
      isYarnClusterMode: Boolean
  ): ArrayBuffer[(String, String)] = {
    val driverJavaSet = new StringBuilder(" -server")
    if (StringUtils.isNotEmpty(EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue)) {
      EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue
        .format(gcLogDir)
        .split("\\s+")
        .foreach(l => {
          driverJavaSet.append(" ").append(l)
        })
    }
    logDir.trim
      .split(" ")
      .foreach(l => {
        driverJavaSet.append(" ").append(l)
      })
    if (isYarnClusterMode) {
      driverJavaSet.append(" -Djava.io.tmpdir=/tmp")
    } else {
      driverJavaSet.append(" -Djava.io.tmpdir=" + variable(TEMP_DIRS))
    }
    if (EnvConfiguration.ENGINE_CONN_DEBUG_ENABLE.getValue) {
      driverJavaSet.append(
        s" -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${variable(RANDOM_PORT)}"
      )
    }

    val conf: ArrayBuffer[(String, String)] = ArrayBuffer()
    conf += (SPARK_DRIVER_EXTRA_JAVA_OPTIONS.key -> driverJavaSet.toString())

    // deal spark conf and spark.hadoop.*
    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    val iterator = properties.entrySet().iterator()
    val sparkConfKeys = ArrayBuffer[String]()
    while (iterator.hasNext) {
      val keyValue = iterator.next()
      if (
          !SPARK_PYTHON_VERSION.key.equals(keyValue.getKey) &&
          !SPARK_DEPLOY_MODE.key.equals(keyValue.getKey) &&
          keyValue.getKey.startsWith("spark.") &&
          StringUtils.isNotBlank(keyValue.getValue)
      ) {
        conf += (keyValue.getKey -> keyValue.getValue)
        sparkConfKeys += keyValue.getKey
      }
    }
    sparkConfKeys.foreach(properties.remove(_))
    conf
  }

  private def isNotBlankPath(path: String): Boolean = {
    StringUtils.isNotBlank(path) && !"/".equals(path.trim) &&
    !"hdfs:///".equals(path.trim) && !"file:///".equals(path.trim)
  }

  private def getValueAndRemove[T](
      properties: java.util.Map[String, String],
      commonVars: CommonVars[T]
  ): T = {
    val value = commonVars.getValue(properties)
    properties.remove(commonVars.key)
    value
  }

  private def getValueAndRemove(
      properties: java.util.Map[String, String],
      key: String,
      defaultValue: String
  ): String = {
    if (properties.containsKey(key)) {
      properties.remove(key)
    } else {
      defaultValue
    }
  }

}
