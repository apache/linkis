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

package org.apache.linkis.engineconnplugin.sqoop.launch

import org.apache.linkis.engineconnplugin.sqoop.context.SqoopEnvConfiguration._
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment.{variable, _}
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants._

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.nio.file.Paths
import java.util
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

class SqoopEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override protected def getEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.Map[String, String] = {
    val environment = super.getEnvironment
    // Basic classpath
    addPathToClassPath(environment, variable(HADOOP_CONF_DIR))
    addExistPathToClassPath(environment, Seq(SQOOP_CONF_DIR.getValue))
    if (StringUtils.isNotBlank(SQOOP_HOME.getValue)) {
      addPathToClassPath(environment, Seq(SQOOP_HOME.getValue, "/*"))
      addPathToClassPath(environment, Seq(SQOOP_HOME.getValue, "/lib/*"))
    }
    // HBase classpath
    if (
        StringUtils.isNotBlank(SQOOP_HBASE_HOME.getValue) && Paths
          .get(SQOOP_HBASE_HOME.getValue)
          .toFile
          .exists()
    ) {
      resolveCommandToClassPath(environment, SQOOP_HBASE_HOME.getValue + "/bin/hbase classpath")
    }
    // HCat classpath
    if (
        StringUtils.isNotBlank(SQOOP_HCAT_HOME.getValue) && Paths
          .get(SQOOP_HCAT_HOME.getValue)
          .toFile
          .exists()
    ) {
      resolveCommandToClassPath(environment, SQOOP_HCAT_HOME.getValue + "/bin/hcat -classpath")
    }
    addExistPathToClassPath(environment, Seq(SQOOP_ZOOCFGDIR.getValue))
    environment
  }

  override protected def getNecessaryEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] = {
    // To submit a mapReduce job, we should load the configuration from hadoop config dir
    Array(HADOOP_CONF_DIR.toString, SQOOP_HOME.key)
  }

  private def addExistPathToClassPath(env: util.Map[String, String], path: String): Unit = {
    if (StringUtils.isNotBlank(path) && Paths.get(path).toFile.exists()) {
      addPathToClassPath(env, path)
    }
  }

  private def resolveCommandToClassPath(env: util.Map[String, String], command: String): Unit = {
    logger.trace(s"Invoke command [${command}] to get class path sequence")
    val builder = new ProcessBuilder(Array("/bin/bash", "-c", command): _*)
    // Set the environment
    builder.environment.putAll(sys.env.asJava)
    builder.redirectErrorStream(false)
    val process = builder.start()
    if (
        process.waitFor(5, TimeUnit.SECONDS) &&
        process.waitFor() == 0
    ) {
      val jarPathSerial = IOUtils.toString(process.getInputStream).trim()
      // TODO we should decide separator in different environment
      val separatorChar = ":"
      val jarPathList = StringUtils
        .split(jarPathSerial, separatorChar)
        .filterNot(jarPath => {
          val splitIndex = jarPath.lastIndexOf("/")
          val jarName = if (splitIndex >= 0) jarPath.substring(splitIndex + 1) else jarPath
          jarName.matches("^jasper-compiler-[\\s\\S]+?\\.jar$") || jarName.matches(
            "^jsp-[\\s\\S]+?\\.jar$"
          ) || jarName.matches("^disruptor-[\\s\\S]+?\\.jar")
        })
        .toList
      addPathToClassPath(env, StringUtils.join(jarPathList.asJava, separatorChar))
    }
    // Release the process
    process.destroy();
  }

  private implicit def buildPath(paths: Seq[String]): String =
    Paths.get(paths.head, paths.tail: _*).toFile.getPath

}
