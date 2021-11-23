/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconnplugin.flink.factory

import java.io.File
import java.time.Duration
import java.util
import java.util.Collections

import com.google.common.collect.Lists
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconnplugin.flink.client.config.Environment
import org.apache.linkis.engineconnplugin.flink.client.config.entries.ExecutionEntry
import org.apache.linkis.engineconnplugin.flink.client.context.ExecutionContext
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import org.apache.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration._
import org.apache.linkis.engineconnplugin.flink.context.{EnvironmentContext, FlinkEngineConnContext}
import org.apache.linkis.engineconnplugin.flink.exception.FlinkInitFailedException
import org.apache.linkis.engineconnplugin.flink.util.ClassUtil
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.creation.{ExecutorFactory, MultiExecutorEngineConnFactory}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineType.EngineType
import org.apache.linkis.manager.label.entity.engine._
import org.apache.commons.lang.StringUtils
import org.apache.flink.configuration._
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings
import org.apache.flink.streaming.api.CheckpointingMode
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
    // set user classpaths
    val classpaths = FLINK_APPLICATION_CLASSPATH.getValue(options)
    if (StringUtils.isNotBlank(classpaths)) {
      info(s"Add $classpaths to flink application classpath.")
      flinkConfig.set(PipelineOptions.CLASSPATHS, util.Arrays.asList(classpaths.split(","): _*))
    }
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
      val flinkMainClassJarPath = if (new File(flinkMainClassJar).exists()) flinkMainClassJar
        else getClass.getClassLoader.getResource(flinkMainClassJar).getPath
      info(s"Ready to use $flinkMainClassJarPath as main class jar to submit application to Yarn.")
      flinkConfig.set(PipelineOptions.JARS, Collections.singletonList(flinkMainClassJarPath))
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
    val executionContext = ExecutionContext.builder(environmentContext.getDefaultEnv, environment, environmentContext.getDependencies,
      environmentContext.getFlinkConfig).build()
    if(FLINK_CHECK_POINT_ENABLE.getValue(options)) {
      val checkpointInterval = FLINK_CHECK_POINT_INTERVAL.getValue(options)
      val checkpointMode = FLINK_CHECK_POINT_MODE.getValue(options)
      val checkpointTimeout = FLINK_CHECK_POINT_TIMEOUT.getValue(options)
      val checkpointMinPause = FLINK_CHECK_POINT_MIN_PAUSE.getValue(options)
      info(s"checkpoint is enabled, checkpointInterval is $checkpointInterval, checkpointMode is $checkpointMode, checkpointTimeout is $checkpointTimeout.")
      executionContext.getTableEnvironment  // This line is need to initialize the StreamExecutionEnvironment.
      executionContext.getStreamExecutionEnvironment.enableCheckpointing(checkpointInterval)
      val checkpointConfig = executionContext.getStreamExecutionEnvironment.getCheckpointConfig
      checkpointMode match {
        case "EXACTLY_ONCE" =>
          checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
        case "AT_LEAST_ONCE" =>
          checkpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
        case _ => throw new FlinkInitFailedException(s"Unknown checkpoint mode $checkpointMode.")
      }
      checkpointConfig.setCheckpointTimeout(checkpointTimeout)
      checkpointConfig.setMinPauseBetweenCheckpoints(checkpointMinPause)
    }
    executionContext
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
