/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.enginemanager.configuration

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, Configuration}
import com.webank.wedatasphere.linkis.common.utils.{ClassUtils, Logging}
import com.webank.wedatasphere.linkis.engine.factory.SparkEngineExecutorFactory
import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineCreator

import scala.collection.mutable.ArrayBuffer
/**
  * Created by allenlliu on 2019/4/8.
  */
object SparkConfiguration extends Logging {
  val SPARK_MAX_PARALLELISM_USERS = CommonVars[Int]("wds.linkis.engine.spark.user.parallelism", 100)
  val SPARK_USER_MAX_WAITING_SIZE = CommonVars[Int]("wds.linkis.engine.spark.user.waiting.max", 100)

  val SPARK_SESSION_HOOK = CommonVars[String]("wds.linkis.engine.spark.session.hook", "")
  val SPARK_LANGUAGE_REPL_INIT_TIME = CommonVars[String]("wds.linkis.engine.spark.language-repl.init.time", new String("30s"))

  val SPARK_ALLOW_REQUEST_ALL_YARN_MEMORY = CommonVars[String]("wds.linkis.engine.spark.allow.all-memory.when.queue", new String("60g"))
  val SPARK_ALLOW_REQUEST_ALL_YARN_CORES = CommonVars[Int]("wds.linkis.engine.spark.allow.all-cores.when.queue", 30)
  val SPARK_USER_MAX_ALLOCATE_SESSIONS = CommonVars[Int]("wds.linkis.engine.spark.user.sessions.max", 5)
  val SPARK_USER_MAX_ALLOCATE_YARN_MEMORY = CommonVars[String]("wds.linkis.engine.spark.user.yarn.memory.max", new String("100g"))
  val SPARK_USER_MAX_ALLOCATE_YARN_CORES = CommonVars[Int]("wds.linkis.engine.spark.user.cores.max", 50)
  val SPARK_USER_MAX_ALLOCATE_DRIVER_MEMORY = CommonVars[String]("wds.linkis.engine.spark.user.driver.memory.max", new String("15g"))
  val SPARK_USER_MAX_ALLOCATE_DRIVER_CORES = SPARK_USER_MAX_ALLOCATE_SESSIONS
  val SPARK_USER_MAX_RESOURCE_IN_QUEUE = CommonVars[Float]("wds.linkis.engine.spark.user.queue.resources.max", 0.6f)
  val SPARK_DANGER_QUEUE_USED_CAPACITY = CommonVars[Float]("wds.linkis.engine.spark.danger.queue.used", 0.2f)
  val SPARK_DANGER_QUEUE_USER_ALLOCATE_SESSION = CommonVars[Int]("wds.linkis.engine.spark.danger.user.sessions.max", 2)
  val SPARK_WARN_QUEUE_USED_CAPACITY = CommonVars[Float]("wds.linkis.engine.spark.warning.queue.used", 0.5f)
  val SPARK_WARN_QUEUE_USER_ALLOCATE_SESSION = CommonVars[Int]("wds.linkis.engine.spark.warning.user.sessions.max", 3)

  val PROXY_USER = CommonVars[String]("spark.proxy.user", "${UM}")
  val SPARK_CLIENT_MODE = "client"
  val SPARK_CLUSTER_MODE = "cluster"
  val SPARK_DEPLOY_MODE = CommonVars[String]("spark.submit.deployMode", SPARK_CLIENT_MODE)
  val SPARK_APPLICATION_JARS = CommonVars[String]("spark.application.jars", "", "User-defined jars, separated by English, must be uploaded to HDFS first, and must be full path to HDFS.（用户自定义jar包，多个以英文,隔开，必须先上传到HDFS，且需为HDFS全路径。）")

  val SPARK_EXTRA_JARS = CommonVars[String]("spark.jars", "", "Additional jar package, Driver and Executor take effect（额外的jar包，Driver和Executor生效）")

  val MAPRED_OUTPUT_COMPRESS = CommonVars[String]("mapred.output.compress", "true", "Whether the map output is compressed（map输出结果是否压缩）")
  val MAPRED_OUTPUT_COMPRESSION_CODEC = CommonVars[String]("mapred.output.compression.codec", "org.apache.hadoop.io.compress.GzipCodec", "Map output compression method（map输出结果压缩方式）")
  val SPARK_MASTER = CommonVars[String]("spark.master", "yarn", "Default master（默认master）")
  val SPARK_OUTPUTDIR = CommonVars[String]("spark.outputDir", "/home/georgeqiao", "Default output path（默认输出路径）")

  val DWC_SPARK_USEHIVECONTEXT = CommonVars[Boolean]("wds.linkis.spark.useHiveContext", true)
  val ENGINE_JAR = CommonVars[String]("wds.linkis.enginemanager.core.jar", ClassUtils.jarOfClass(classOf[SparkEngineExecutorFactory]).head)
  val SPARK_DRIVER_CLASSPATH = CommonVars[String]("wds.linkis.spark.driver.conf.mainjar", "")
  val SPARK_DRIVER_EXTRA_JAVA_OPTIONS = CommonVars[String]("spark.driver.extraJavaOptions", "\"-Dwds.linkis.configuration=linkis-engine.properties " + getJavaRemotePort + "\"")
  val DEFAULT_JAVA_OPTS = CommonVars[String]("wds.linkis.engine.javaOpts.default", "-server -XX:+UseG1GC -XX:MaxPermSize=250m -XX:PermSize=128m " +
    "-Xloggc:%s -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -Dwds.linkis.configuration=linkis-engine.properties")
  val SPARK_ML_BUCKET_FIELDS = CommonVars[String]("wds.linkis.engine.spark.ml.bucketFields", "age[0,18,30,60,100]")

  val SPARK_SUBMIT_CMD = CommonVars[String]("wds.linkis.engine.spark.submit.cmd", "spark-submit")
  private var Ports: ArrayBuffer[Int] = _

  def getJavaRemotePort = {
    if (Configuration.IS_TEST_MODE.getValue) {
      val r = new scala.util.Random()
      val port = 1024 + r.nextInt((65536 - 1024) + 1)
      info(s"open debug mode with port $port.")
      s"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$port"
    } else {
      ""
    }
  }

  private def getAvailablePort: Int = synchronized {
    var port = AbstractEngineCreator.getNewPort
    info("Get new port " + port)
    if (Ports == null) {
      info("Get inInitPorts is null ")
      Ports = ArrayBuffer(0, 1)
      info("Current ports is " + Ports.toList.toString())
    }
    while (Ports.contains(port)) {
      if (AbstractEngineCreator != null) {
        port = AbstractEngineCreator.getNewPort
      }
    }
    Ports += port
    port
  }
}
