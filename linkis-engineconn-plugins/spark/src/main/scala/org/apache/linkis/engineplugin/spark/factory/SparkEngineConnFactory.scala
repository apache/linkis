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

package org.apache.linkis.engineplugin.spark.factory

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.launch.EngineConnServer
import org.apache.linkis.engineplugin.spark.client.context.{ExecutionContext, SparkConfig}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration._
import org.apache.linkis.engineplugin.spark.config.SparkResourceConfiguration._
import org.apache.linkis.engineplugin.spark.context.{EnvironmentContext, SparkEngineConnContext}
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary._
import org.apache.linkis.engineplugin.spark.exception.{
  SparkCreateFileException,
  SparkSessionNullException
}
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.creation.{
  ExecutorFactory,
  MultiExecutorEngineConnFactory
}
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment.variable
import org.apache.linkis.manager.label.entity.engine.EngineType
import org.apache.linkis.manager.label.entity.engine.EngineType.EngineType
import org.apache.linkis.server.JMap

import org.apache.commons.lang3.StringUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, SQLContext}
import org.apache.spark.util.SparkUtils

import java.io.File
import java.lang.reflect.Constructor
import java.util

/**
 */
class SparkEngineConnFactory extends MultiExecutorEngineConnFactory with Logging {

  override protected def createEngineConnSession(
      engineCreationContext: EngineCreationContext
  ): Any = {
    if (EngineConnServer.isOnceMode) {
      createSparkOnceEngineConnContext(engineCreationContext)
    } else {
      createSparkEngineSession(engineCreationContext)
    }
  }

  def createSparkOnceEngineConnContext(
      engineCreationContext: EngineCreationContext
  ): SparkEngineConnContext = {
    val environmentContext = createEnvironmentContext(engineCreationContext)
    val sparkEngineConnContext = new SparkEngineConnContext(environmentContext)
    val executionContext =
      createExecutionContext(engineCreationContext.getOptions, environmentContext)
    sparkEngineConnContext.setExecutionContext(executionContext)
    sparkEngineConnContext
  }

  protected def createEnvironmentContext(
      engineCreationContext: EngineCreationContext
  ): EnvironmentContext = {
    val options = engineCreationContext.getOptions
    val hadoopConfDir = EnvConfiguration.HADOOP_CONF_DIR.getValue(options)
    val sparkHome = SPARK_HOME.getValue(options)
    val sparkConfDir = SPARK_CONF_DIR.getValue(options)
    val sparkConfig: SparkConfig = getSparkConfig(options)
    val context = new EnvironmentContext(sparkConfig, hadoopConfDir, sparkConfDir, sparkHome, null)
    context
  }

  def getSparkConfig(options: util.Map[String, String]): SparkConfig = {
    logger.info("options: " + JsonUtils.jackson.writeValueAsString(options))
    val sparkConfig: SparkConfig = new SparkConfig()
    sparkConfig.setJavaHome(variable(Environment.JAVA_HOME))
    sparkConfig.setSparkHome(SPARK_HOME.getValue(options))
    val master = SPARK_MASTER.getValue(options)
    sparkConfig.setMaster(master)
    if (master.startsWith("k8s")) {
      sparkConfig.setK8sConfigFile(SPARK_K8S_CONFIG_FILE.getValue(options))
      sparkConfig.setK8sServiceAccount(SPARK_K8S_SERVICE_ACCOUNT.getValue(options))
      sparkConfig.setK8sMasterUrl(SPARK_K8S_MASTER_URL.getValue(options))
      sparkConfig.setK8sUsername(SPARK_K8S_USERNAME.getValue(options))
      sparkConfig.setK8sPassword(SPARK_K8S_PASSWORD.getValue(options))
      sparkConfig.setK8sImage(SPARK_K8S_IMAGE.getValue(options))
      sparkConfig.setK8sNamespace(SPARK_K8S_NAMESPACE.getValue(options))
      sparkConfig.setK8sFileUploadPath(SPARK_KUBERNETES_FILE_UPLOAD_PATH.getValue(options))
      sparkConfig.setK8sSparkVersion(SPARK_K8S_SPARK_VERSION.getValue(options))
      sparkConfig.setK8sRestartPolicy(SPARK_K8S_RESTART_POLICY.getValue(options))
      sparkConfig.setK8sLanguageType(SPARK_K8S_LANGUAGE_TYPE.getValue(options))
      sparkConfig.setK8sImagePullPolicy(SPARK_K8S_IMAGE_PULL_POLICY.getValue(options))
    }
    sparkConfig.setDeployMode(SPARK_DEPLOY_MODE.getValue(options))
    sparkConfig.setAppResource(SPARK_APP_RESOURCE.getValue(options))
    sparkConfig.setAppName(SPARK_APP_NAME.getValue(options))
    sparkConfig.setJars(SPARK_EXTRA_JARS.getValue(options)) // todo
    sparkConfig.setDriverMemory(LINKIS_SPARK_DRIVER_MEMORY.getValue(options))
    sparkConfig.setDriverJavaOptions(SPARK_DRIVER_EXTRA_JAVA_OPTIONS.getValue(options))
    sparkConfig.setDriverClassPath(SPARK_DRIVER_CLASSPATH.getValue(options))
    sparkConfig.setExecutorMemory(LINKIS_SPARK_EXECUTOR_MEMORY.getValue(options))
    sparkConfig.setProxyUser(PROXY_USER.getValue(options))
    sparkConfig.setDriverCores(LINKIS_SPARK_DRIVER_CORES.getValue(options))
    sparkConfig.setExecutorCores(LINKIS_SPARK_EXECUTOR_CORES.getValue(options))
    sparkConfig.setNumExecutors(LINKIS_SPARK_EXECUTOR_INSTANCES.getValue(options))
    sparkConfig.setQueue(LINKIS_QUEUE_NAME.getValue(options))

    logger.info(s"spark_info: ${sparkConfig}")
    sparkConfig
  }

  def createExecutionContext(
      options: util.Map[String, String],
      environmentContext: EnvironmentContext
  ): ExecutionContext = {
    val context = new ExecutionContext()
    context.setSparkConfig(environmentContext.getSparkConfig)
    // todo
    context
  }

  def createSparkEngineSession(engineCreationContext: EngineCreationContext): SparkEngineSession = {
    val options = engineCreationContext.getOptions
    val sparkConf: SparkConf = new SparkConf(true)
    val master =
      sparkConf.getOption("spark.master").getOrElse(CommonVars("spark.master", "yarn").getValue)
    logger.info(s"------ Create new SparkContext {$master} -------")
    val pysparkBasePath = SparkConfiguration.SPARK_HOME.getValue
    val pysparkPath = new File(pysparkBasePath, "python" + File.separator + "lib")
    var pythonLibUris = pysparkPath.listFiles().map(_.toURI.toString).filter(_.endsWith(".zip"))
    if (pythonLibUris.length == 2) {
      val sparkConfValue1 = Utils.tryQuietly(CommonVars("spark.yarn.dist.files", "").getValue)
      val sparkConfValue2 = Utils.tryQuietly(sparkConf.get("spark.yarn.dist.files"))
      if (StringUtils.isNotBlank(sparkConfValue2)) {
        pythonLibUris = sparkConfValue2 +: pythonLibUris
      }
      if (StringUtils.isNotBlank(sparkConfValue1)) {
        pythonLibUris = sparkConfValue1 +: pythonLibUris
      }
      sparkConf.set("spark.yarn.dist.files", pythonLibUris.mkString(","))
    }
    // Distributes needed libraries to workers
    // when spark version is greater than or equal to 1.5.0
    if (master.contains("yarn")) sparkConf.set("spark.yarn.isPython", "true")

    val outputDir = createOutputDir(sparkConf)

    logger.info(
      "print current thread name " + Thread.currentThread().getContextClassLoader.toString
    )
    val sparkSession = createSparkSession(outputDir, sparkConf)
    if (sparkSession == null) {
      throw new SparkSessionNullException(CAN_NOT_NULL.getErrorCode, CAN_NOT_NULL.getErrorDesc)
    }

    val sc = sparkSession.sparkContext
    val sqlContext =
      createSQLContext(sc, options.asInstanceOf[util.HashMap[String, String]], sparkSession)
    if (SparkConfiguration.MAPRED_OUTPUT_COMPRESS.getValue(options)) {
      sc.hadoopConfiguration.set(
        "mapred.output.compress",
        SparkConfiguration.MAPRED_OUTPUT_COMPRESS.getValue(options).toString
      )
      sc.hadoopConfiguration.set(
        "mapred.output.compression.codec",
        SparkConfiguration.MAPRED_OUTPUT_COMPRESSION_CODEC.getValue(options)
      )
    }
    SparkEngineSession(sc, sqlContext, sparkSession, outputDir)
  }

  def createSparkSession(
      outputDir: File,
      conf: SparkConf,
      addPythonSupport: Boolean = false
  ): SparkSession = {
    val execUri = System.getenv("SPARK_EXECUTOR_URI")
    val sparkJars = conf.getOption("spark.jars")
    def unionFileLists(leftList: Option[String], rightList: Option[String]): Set[String] = {
      var allFiles = Set[String]()
      leftList.foreach { value => allFiles ++= value.split(",") }
      rightList.foreach { value => allFiles ++= value.split(",") }
      allFiles.filter { _.nonEmpty }
    }
    val master =
      conf.getOption("spark.master").getOrElse(SparkConfiguration.SPARK_MASTER.getValue)
    logger.info(s"------ Create new SparkContext {$master} -------")
    if (StringUtils.isNotEmpty(master)) {
      conf.setMaster(master)
    }

    val jars = if (conf.get("spark.master").contains("yarn")) {
      val yarnJars = conf.getOption("spark.yarn.dist.jars")
      unionFileLists(sparkJars, yarnJars).toSeq
    } else {
      sparkJars.map(_.split(",")).map(_.filter(_.nonEmpty)).toSeq.flatten
    }
    if (outputDir != null) {
      conf.set("spark.repl.class.outputDir", outputDir.getAbsolutePath)
    }

    if (jars.nonEmpty) conf.setJars(jars)
    if (execUri != null) conf.set("spark.executor.uri", execUri)
    if (System.getenv("SPARK_HOME") != null) conf.setSparkHome(System.getenv("SPARK_HOME"))
    conf.set("spark.scheduler.mode", "FAIR")

    if (SparkConfiguration.LINKIS_SPARK_ETL_SUPPORT_HUDI.getValue) {
      conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    }

    val builder = SparkSession.builder.config(conf)
    builder.enableHiveSupport().getOrCreate()
  }

  def createSQLContext(
      sc: SparkContext,
      options: JMap[String, String],
      sparkSession: SparkSession
  ): SQLContext = {
    var sqlc: SQLContext = null
    if (SparkConfiguration.LINKIS_SPARK_USEHIVECONTEXT.getValue(options)) {
      val name = "org.apache.spark.sql.hive.HiveContext"
      var hc: Constructor[_] = null
      Utils.tryCatch {
        hc = getClass.getClassLoader.loadClass(name).getConstructor(classOf[SparkContext])
        sqlc = hc.newInstance(sc).asInstanceOf[SQLContext]
      } { e: Throwable =>
        logger.warn("Can't create HiveContext. Fallback to SQLContext", e)
        sqlc = sparkSession.sqlContext
      }
    } else sqlc = sparkSession.sqlContext
    sqlc
  }

  def createOutputDir(conf: SparkConf): File = {
    val rootDir = conf.get("spark.repl.classdir", System.getProperty("java.io.tmpdir"))
    Utils.tryThrow {
      val output = SparkUtils.createTempDir(root = rootDir, namePrefix = "repl")
      logger.info("outputDir====> " + output)
      output.deleteOnExit()
      conf.set("spark.repl.class.outputDir", output.getAbsolutePath)
      output
    }(t => {
      logger.warn("create spark repl classdir failed", t)
      throw new SparkCreateFileException(
        SPARK_CREATE_EXCEPTION.getErrorCode,
        SPARK_CREATE_EXCEPTION.getErrorDesc,
        t
      )
      null
    })
  }

  override protected def getDefaultExecutorFactoryClass: Class[_ <: ExecutorFactory] =
    classOf[SparkSqlExecutorFactory]

  override protected def getEngineConnType: EngineType = EngineType.SPARK

  private val executorFactoryArray = Array[ExecutorFactory](
    new SparkSqlExecutorFactory,
    new SparkPythonExecutorFactory,
    new SparkScalaExecutorFactory,
    new SparkDataCalcExecutorFactory,
    new SparkOnceExecutorFactory
  )

  override def getExecutorFactories: Array[ExecutorFactory] = {
    executorFactoryArray
  }

}
