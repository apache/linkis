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

package org.apache.linkis.engineplugin.spark.config

import org.apache.linkis.common.conf.{CommonVars, TimeType}
import org.apache.linkis.common.utils.{ClassUtils, Logging}
import org.apache.linkis.engineplugin.spark.factory.SparkEngineConnFactory

import java.io.File

/**
 */
object SparkConfiguration extends Logging {

  val SPARK_HOME_ENV = "SPARK_HOME"
  val SPARK_CONF_DIR_ENV = "SPARK_CONF_DIR"

  val PROCESS_MAX_THREADS = CommonVars[Int]("wds.linkis.process.threadpool.max", 100)

  val SPARK_SESSION_HOOK = CommonVars[String]("wds.linkis.engine.spark.session.hook", "")

  val SPARK_LOOP_INIT_TIME =
    CommonVars[TimeType]("wds.linkis.engine.spark.spark-loop.init.time", new TimeType("120s"))

  val SPARK_LANGUAGE_REPL_INIT_TIME =
    CommonVars[TimeType]("wds.linkis.engine.spark.language-repl.init.time", new TimeType("30s"))

  val SPARK_REPL_CLASSDIR = CommonVars[String]("spark.repl.classdir", "", "默认master")

  val PROXY_USER = CommonVars[String]("spark.proxy.user", "")

  val SPARK_DEPLOY_MODE = CommonVars[String]("spark.submit.deployMode", "client")

  val SPARK_APP_NAME = CommonVars[String]("spark.app.name", "Linkis-EngineConn-Spark")
  val SPARK_APP_RESOURCE = CommonVars[String]("spark.app.resource", "")
  val SPARK_APP_CONF = CommonVars[String]("spark.extconf", "")

  val SPARK_K8S_CONFIG_FILE = CommonVars[String]("linkis.spark.k8s.config.file", "~/.kube/config")
  val SPARK_K8S_SERVICE_ACCOUNT = CommonVars[String]("linkis.spark.k8s.serviceAccount", "")
  val SPARK_K8S_MASTER_URL = CommonVars[String]("linkis.spark.k8s.master.url", "")
  val SPARK_K8S_USERNAME = CommonVars[String]("linkis.spark.k8s.username", "")
  val SPARK_K8S_PASSWORD = CommonVars[String]("linkis.spark.k8s.password", "")
  val SPARK_K8S_IMAGE = CommonVars[String]("linkis.spark.k8s.image", "apache/spark:v3.2.1")
  val SPARK_K8S_IMAGE_PULL_POLICY = CommonVars[String]("linkis.spark.k8s.imagePullPolicy", "Always")
  val SPARK_K8S_LANGUAGE_TYPE = CommonVars[String]("linkis.spark.k8s.languageType", "Scala")
  val SPARK_K8S_RESTART_POLICY = CommonVars[String]("linkis.spark.k8s.restartPolicy", "Never")
  val SPARK_K8S_SPARK_VERSION = CommonVars[String]("linkis.spark.k8s.sparkVersion", "3.2.1")
  val SPARK_K8S_NAMESPACE = CommonVars[String]("linkis.spark.k8s.namespace", "default")

  val SPARK_K8S_EXECUTOR_REQUEST_CORES =
    CommonVars[String]("linkis.spark.k8s.executor.request.cores", "1")

  val SPARK_K8S_DRIVER_REQUEST_CORES =
    CommonVars[String]("linkis.spark.k8s.driver.request.cores", "1")

  val SPARK_KUBERNETES_FILE_UPLOAD_PATH =
    CommonVars[String]("spark.kubernetes.file.upload.path", "local:///opt/spark/tmp")

  val SPARK_PYTHON_VERSION = CommonVars[String]("spark.python.version", "python")

  val SPARK_PYTHON_TEST_MODE_ENABLE =
    CommonVars[Boolean]("linkis.spark.python.test.mode.enable", false)

  val SPARK_PYTHON_TEST_MODE_MIX__PYSHELL_PATH = CommonVars[String](
    "linkis.spark.python.mix.pyshell.path",
    "/appcom/Install/linkis/mix_pyspark.py"
  )

  val SPARK_EXTRA_JARS = CommonVars[String](
    "spark.jars",
    "",
    "Additional jar package, Driver and Executor take effect（额外的jar包，Driver和Executor生效）"
  )

  val SPARK_SUBMIT_PATH = CommonVars[String]("wds.linkis.spark.sparksubmit.path", "spark-submit")

  val MAPRED_OUTPUT_COMPRESS = CommonVars[Boolean](
    "mapred.output.compress",
    false,
    "Whether the map output is compressed（map输出结果是否压缩）"
  )

  val MAPRED_OUTPUT_COMPRESSION_CODEC = CommonVars[String](
    "mapred.output.compression.codec",
    "org.apache.hadoop.io.compress.GzipCodec",
    "Map output compression method（map输出结果压缩方式）"
  )

  val SPARK_MASTER = CommonVars[String]("spark.master", "yarn", "Default yarn（默认yarn）")

  val SPARK_CONSOLE_OUTPUT_NUM = CommonVars[Int]("wds.linkis.spark.output.line.limit", 10)

  val LINKIS_SPARK_USEHIVECONTEXT = CommonVars[Boolean]("wds.linkis.spark.useHiveContext", true)

  val DEFAULT_SPARK_JAR_NAME =
    CommonVars[String]("wds.linkis.ecp.spark.default.jar", "linkis-engineconn-core-1.3.2.jar")

  val ENGINE_JAR = CommonVars[String]("wds.linkis.enginemanager.core.jar", getMainJarName)

  val SPARK_DRIVER_CLASSPATH = CommonVars[String]("spark.driver.extraClassPath", "")

  val SPARK_DRIVER_EXTRA_JAVA_OPTIONS = CommonVars[String](
    "spark.driver.extraJavaOptions",
    "\"-Dwds.linkis.server.conf=linkis-engine.properties " + "\""
  )

  val SPARK_DEFAULT_EXTERNAL_JARS_PATH = CommonVars[String]("spark.external.default.jars", "")

  val SPARK_CONF_DIR = CommonVars[String](
    "spark.config.dir",
    CommonVars[String]("SPARK_CONF_DIR", "/appcom/config/spark-config").getValue
  )

  val SPARK_HOME = CommonVars[String](
    "spark.home",
    CommonVars[String]("SPARK_HOME", "/appcom/Install/spark").getValue
  )

  val SQL_EXTENSION_TIMEOUT = CommonVars("wds.linkis.dws.ujes.spark.extension.timeout", 3000L)
  val SPARK_NF_FRACTION_LENGTH = CommonVars[Int]("wds.linkis.engine.spark.fraction.length", 30)
  val SHOW_DF_MAX_RES = CommonVars("wds.linkis.show.df.max.res", Int.MaxValue)
  val MDQ_APPLICATION_NAME = CommonVars("wds.linkis.mdq.application.name", "linkis-ps-datasource")
  val DOLPHIN_LIMIT_LEN = CommonVars("wds.linkis.dolphin.limit.len", 5000)

  val IS_VIEWFS_ENV = CommonVars("wds.linkis.spark.engine.is.viewfs.env", true)

  val ENGINE_SHUTDOWN_LOGS =
    CommonVars("wds.linkis.spark.engineconn.fatal.log", "error writing class;OutOfMemoryError")

  val PYSPARK_PYTHON3_PATH =
    CommonVars[String]("pyspark.python3.path", "/appcom/Install/anaconda3/bin/python")

  val ENABLE_REPLACE_PACKAGE_NAME =
    CommonVars("wds.linkis.spark.engine.scala.replace_package_header.enable", true)

  val REPLACE_PACKAGE_HEADER = CommonVars(
    "wds.linkis.spark.engine.scala.replace_package.header",
    "com.webank.wedatasphere.linkis"
  )

  val REPLACE_PACKAGE_TO_HEADER = "org.apache.linkis"

  val SPARK_APPLICATION_ARGS = CommonVars("spark.app.args", "")
  val SPARK_APPLICATION_MAIN_CLASS = CommonVars("spark.app.main.class", "")

  val SPARK_ONCE_APP_STATUS_FETCH_INTERVAL =
    CommonVars("linkis.spark.once.app.fetch.status.interval", new TimeType("5s"))

  val SPARK_ONCE_APP_STATUS_FETCH_FAILED_MAX =
    CommonVars("linkis.spark.once.app.fetch.status.failed.num", 3)

  val SPARK_ONCE_YARN_RESTFUL_URL = CommonVars[String]("linkis.spark.once.yarn.restful.url", "")

  val LINKIS_SPARK_ETL_SUPPORT_HUDI = CommonVars[Boolean]("linkis.spark.etl.support.hudi", false)

  val SCALA_PARSE_APPEND_CODE =
    CommonVars("linkis.scala.parse.append.code", "val linkisVar=1").getValue

  private def getMainJarName(): String = {
    val somePath = ClassUtils.jarOfClass(classOf[SparkEngineConnFactory])
    if (somePath.isDefined) {
      val path = new File(somePath.get)
      path.getName
    } else {
      DEFAULT_SPARK_JAR_NAME.getValue
    }
  }

}
