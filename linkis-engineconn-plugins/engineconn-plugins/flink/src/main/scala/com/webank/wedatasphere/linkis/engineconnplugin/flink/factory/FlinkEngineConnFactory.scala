/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engineconnplugin.flink.factory

import java.time.Duration
import java.util
import java.util.Collections

import com.google.common.collect.Lists
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.config.Environment
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.config.entries.ExecutionEntry
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.context.ExecutionContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration._
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.{EnvironmentContext, FlinkEngineConnContext}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.FlinkInitFailedException
import com.webank.wedatasphere.linkis.engineconnplugin.flink.util.ClassUtil
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EnvConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.{ExecutorFactory, MultiExecutorEngineConnFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType
import com.webank.wedatasphere.linkis.manager.label.entity.engine._
import org.apache.commons.lang.StringUtils
import org.apache.flink.configuration._
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings
import org.apache.flink.yarn.configuration.{YarnConfigOptions, YarnDeploymentTarget}

import scala.collection.convert.decorateAsScala._


class FlinkEngineConnFactory extends MultiExecutorEngineConnFactory with Logging {

  override protected def createEngineConnSession(engineCreationContext: EngineCreationContext): Any = {
    val options = engineCreationContext.getOptions
    val environmentContext = createEnvironmentContext(engineCreationContext)
    val flinkEngineConnContext = createFlinkEngineConnContext(environmentContext)
    val executionContext = createExecutionContext(options, environmentContext)
    flinkEngineConnContext.setExecutionContext(executionContext)
    flinkEngineConnContext
  }

  protected def createEnvironmentContext(engineCreationContext: EngineCreationContext): EnvironmentContext = {
    val options = engineCreationContext.getOptions
    val defaultEnv = Environment.parse(this.getClass.getClassLoader.getResource("flink-sql-defaults.yaml"))
    val hadoopConfDir = EnvConfiguration.HADOOP_CONF_DIR.getValue(options)
    val flinkHome = FLINK_HOME.getValue(options)
    val flinkConfDir = FLINK_CONF_DIR.getValue(options)
    val flinkLibRemotePath = FLINK_LIB_REMOTE_PATH.getValue(options)
    val flinkDistJarPath = FLINK_DIST_JAR_PATH.getValue(options)
    val providedLibDirsArray = FLINK_LIB_LOCAL_PATH.getValue(options).split(",")
    val shipDirsArray = FLINK_SHIP_DIRECTORIES.getValue(options).split(",")
    val context = new EnvironmentContext(defaultEnv, new Configuration, hadoopConfDir, flinkConfDir, flinkHome,
      flinkDistJarPath, flinkLibRemotePath, providedLibDirsArray, shipDirsArray, null)
    //Step1: environment-level configurations(第一步: 环境级别配置)
    val jobName = options.getOrDefault("flink.app.name", "EngineConn-Flink")
    val yarnQueue = LINKIS_QUEUE_NAME.getValue(options)
    val parallelism = FLINK_APP_DEFAULT_PARALLELISM.getValue(options)
    val jobManagerMemory = LINKIS_FLINK_JOB_MANAGER_MEMORY.getValue(options) + "G"
    val taskManagerMemory = LINKIS_FLINK_TASK_MANAGER_MEMORY.getValue(options) + "G"
    val numberOfTaskSlots = LINKIS_FLINK_TASK_SLOTS.getValue(options)
    info(s"Use yarn queue $yarnQueue, and set parallelism = $parallelism, jobManagerMemory = $jobManagerMemory G, taskManagerMemory = $taskManagerMemory G, numberOfTaskSlots = $numberOfTaskSlots.")
    //Step2: application-level configurations(第二步: 应用级别配置)
    //construct app-config(构建应用配置)
    val flinkConfig = context.getFlinkConfig
    //construct jar-dependencies(构建依赖jar包环境)
    val flinkUserLibRemotePath = FLINK_USER_LIB_REMOTE_PATH.getValue(options).split(",")
    val providedLibDirList = Lists.newArrayList(flinkUserLibRemotePath.filter(StringUtils.isNotBlank): _*)
    val flinkUserRemotePathList = Lists.newArrayList(flinkLibRemotePath.split(",").filter(StringUtils.isNotBlank): _*)
    if (flinkUserRemotePathList != null && flinkUserRemotePathList.size() > 0) providedLibDirList.addAll(flinkUserRemotePathList)
    //if(StringUtils.isNotBlank(flinkLibRemotePath)) providedLibDirList.add(flinkLibRemotePath)
    flinkConfig.set(YarnConfigOptions.PROVIDED_LIB_DIRS, providedLibDirList)
    //construct jar-dependencies(构建依赖jar包环境)
    flinkConfig.set(YarnConfigOptions.SHIP_ARCHIVES, context.getShipDirs)
    //yarn application name(yarn 作业名称)
    flinkConfig.set(YarnConfigOptions.APPLICATION_NAME, jobName)
    //yarn queue
    flinkConfig.set(YarnConfigOptions.APPLICATION_QUEUE, yarnQueue)
    //Configure resource/concurrency (设置：资源/并发度)
    flinkConfig.setInteger(CoreOptions.DEFAULT_PARALLELISM, parallelism)
    flinkConfig.set(JobManagerOptions.TOTAL_PROCESS_MEMORY, MemorySize.parse(jobManagerMemory))
    flinkConfig.set(TaskManagerOptions.TOTAL_PROCESS_MEMORY, MemorySize.parse(taskManagerMemory))
    flinkConfig.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, numberOfTaskSlots)
    if(FLINK_REPORTER_ENABLE.getValue) {
      flinkConfig.set(MetricOptions.REPORTER_CLASS, FLINK_REPORTER_CLASS.getValue)
      flinkConfig.set(MetricOptions.REPORTER_INTERVAL, Duration.ofMillis(FLINK_REPORTER_INTERVAL.getValue.toLong))
    }
    //set savePoint(设置 savePoint)
    val savePointPath = FLINK_SAVE_POINT_PATH.getValue(options)
    if (StringUtils.isNotBlank(savePointPath)) {
      val allowNonRestoredState = FLINK_APP_ALLOW_NON_RESTORED_STATUS.getValue(options).toBoolean
      val savepointRestoreSettings = SavepointRestoreSettings.forPath(savePointPath, allowNonRestoredState)
      SavepointRestoreSettings.toConfiguration(savepointRestoreSettings, flinkConfig)
    }
    //Configure user-entrance jar. Can be remote, but only support 1 jar(设置：用户入口jar：可以远程，只能设置1个jar)
    val flinkMainClassJar = FLINK_APPLICATION_MAIN_CLASS_JAR.getValue(options)
    if(StringUtils.isNotBlank(flinkMainClassJar)) {
      info(s"Ready to use $flinkMainClassJar as main class jar to submit application to Yarn.")
      flinkConfig.set(PipelineOptions.JARS, Collections.singletonList(flinkMainClassJar))
      flinkConfig.set(DeploymentOptions.TARGET, YarnDeploymentTarget.APPLICATION.getName)
      context.setDeploymentTarget(YarnDeploymentTarget.APPLICATION)
      addApplicationLabels(engineCreationContext)
    } else if(isOnceEngineConn(engineCreationContext.getLabels())) {
      flinkConfig.set(DeploymentOptions.TARGET, YarnDeploymentTarget.PER_JOB.getName)
    } else {
      flinkConfig.set(DeploymentOptions.TARGET, YarnDeploymentTarget.SESSION.getName)
    }
    context
  }

  protected def isOnceEngineConn(labels: util.List[Label[_]]): Boolean = {
    val engineConnModeLabel = getEngineConnModeLabel(labels)
    engineConnModeLabel != null && (EngineConnMode.toEngineConnMode(engineConnModeLabel.getEngineConnMode) match {
      case EngineConnMode.Once | EngineConnMode.Once_With_Cluster => true
      case _ => false
    })
  }

  private def addApplicationLabels(engineCreationContext: EngineCreationContext): Unit = {
    val labels = engineCreationContext.getLabels().asScala
    if(!labels.exists(_.isInstanceOf[CodeLanguageLabel])) {
      val codeLanguageLabel = new CodeLanguageLabel
      codeLanguageLabel.setCodeType(RunType.JAR.toString)
      engineCreationContext.getLabels().add(codeLanguageLabel)
    }
    if(!labels.exists(_.isInstanceOf[EngineConnModeLabel])) {
      val engineConnModeLabel = new EngineConnModeLabel
      engineConnModeLabel.setEngineConnMode(EngineConnMode.Once.toString)
      engineCreationContext.getLabels().add(engineConnModeLabel)
    }
  }

  def createExecutionContext(options: util.Map[String, String], environmentContext: EnvironmentContext): ExecutionContext = {
    val environment = environmentContext.getDeploymentTarget match {
      case YarnDeploymentTarget.PER_JOB | YarnDeploymentTarget.SESSION =>
        val planner = FlinkEnvConfiguration.FLINK_SQL_PLANNER.getValue(options)
        if (!ExecutionEntry.AVAILABLE_PLANNERS.contains(planner.toLowerCase))
          throw new FlinkInitFailedException("Planner must be one of these: " + String.join(", ", ExecutionEntry.AVAILABLE_PLANNERS))
        val executionType = FlinkEnvConfiguration.FLINK_SQL_EXECUTION_TYPE.getValue(options)
        if (!ExecutionEntry.AVAILABLE_EXECUTION_TYPES.contains(executionType.toLowerCase))
          throw new FlinkInitFailedException("Execution type must be one of these: " + String.join(", ", ExecutionEntry.AVAILABLE_EXECUTION_TYPES))
        val properties = new util.HashMap[String, String]
        properties.put(Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_PLANNER, planner)
        properties.put(Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_TYPE, executionType)
        if (executionType.equalsIgnoreCase(ExecutionEntry.EXECUTION_TYPE_VALUE_BATCH)) {
          // for batch mode we ensure that results are provided in materialized form
          properties.put(Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_RESULT_MODE, ExecutionEntry.EXECUTION_RESULT_MODE_VALUE_TABLE)
        } else {
          // for streaming mode we ensure that results are provided in changelog form
          properties.put(Environment.EXECUTION_ENTRY + "." + ExecutionEntry.EXECUTION_RESULT_MODE, ExecutionEntry.EXECUTION_RESULT_MODE_VALUE_CHANGELOG)
        }
        Environment.enrich(environmentContext.getDefaultEnv, properties, Collections.emptyMap())
      case YarnDeploymentTarget.APPLICATION => null
      case t =>
        error(s"Not supported YarnDeploymentTarget ${t.getName}.")
        throw new FlinkInitFailedException(s"Not supported YarnDeploymentTarget ${t.getName}.")
    }
    ExecutionContext.builder(environmentContext.getDefaultEnv, environment, environmentContext.getDependencies,
      environmentContext.getFlinkConfig).build()
  }

  protected def createFlinkEngineConnContext(environmentContext: EnvironmentContext): FlinkEngineConnContext =
    new FlinkEngineConnContext(environmentContext)

  override protected def getDefaultExecutorFactoryClass: Class[_ <: ExecutorFactory] = classOf[FlinkCodeExecutorFactory]

  override protected def getEngineConnType: EngineType = EngineType.FLINK

  private val executorFactoryArray =  Array[ExecutorFactory](ClassUtil.getInstance(classOf[FlinkSQLExecutorFactory], new FlinkSQLExecutorFactory),
    ClassUtil.getInstance(classOf[FlinkApplicationExecutorFactory], new FlinkApplicationExecutorFactory),
    ClassUtil.getInstance(classOf[FlinkCodeExecutorFactory], new FlinkCodeExecutorFactory))

  override def getExecutorFactories: Array[ExecutorFactory] = executorFactoryArray
}
