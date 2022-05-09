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

package org.apache.linkis.engineconnplugin.sqoop.launch


import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.launch.entity.{EngineConnBuildRequest, RicherEngineConnBuildRequest}
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment.{HADOOP_CONF_DIR, HIVE_CONF_DIR, PWD, variable}
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants.{ENGINE_CONN_CONF_DIR_NAME, ENGINE_CONN_LIB_DIR_NAME, addPathToClassPath}
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder

import java.io.File
import java.util
import org.apache.commons.lang.StringUtils
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration.{HADOOP_LIB_CLASSPATH, HBASE_LIB_CLASSPATH, LINKIS_PUBLIC_MODULE_PATH}

import java.nio.file.Paths
import scala.collection.JavaConversions._

class SqoopEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder{
  override protected def getEnvironment(implicit engineConnBuildRequest: EngineConnBuildRequest): util.Map[String, String] = {
    info("Setting up the launch environment for engineconn.")
    val environment = new util.HashMap[String, String]
    if(ifAddHiveConfigPath) {
      addPathToClassPath(environment, variable(HADOOP_CONF_DIR))
      addPathToClassPath(environment, variable(HIVE_CONF_DIR))
    }
    addPathToClassPath(environment,HADOOP_LIB_CLASSPATH.getValue)
    addPathToClassPath(environment,HBASE_LIB_CLASSPATH.getValue)
    //    addPathToClassPath(environment, variable(PWD))
    // first, add engineconn conf dirs.
    addPathToClassPath(environment, Seq(variable(PWD), ENGINE_CONN_CONF_DIR_NAME))
    // second, add engineconn libs.
    addPathToClassPath(environment, Seq(variable(PWD), ENGINE_CONN_LIB_DIR_NAME + "/*"))
    // then, add public modules.
    if (!enablePublicModule) {
      addPathToClassPath(environment, Seq(LINKIS_PUBLIC_MODULE_PATH.getValue + "/*"))
    }
    // finally, add the suitable properties key to classpath
    engineConnBuildRequest.engineConnCreationDesc.properties.foreach { case (key, value) =>
      if (key.startsWith("engineconn.classpath") || key.startsWith("wds.linkis.engineconn.classpath")) {
        addPathToClassPath(environment, value)
      }
    }
    getExtraClassPathFile.foreach { file: String =>
      addPathToClassPath(environment, file)
    }
    engineConnBuildRequest match {
      case richer: RicherEngineConnBuildRequest =>
        def addFiles(files: String): Unit = if (StringUtils.isNotBlank(files)) {
          files.split(",").foreach(file => addPathToClassPath(environment, Seq(variable(PWD), new File(file).getName)))
        }

        val configs: util.Map[String, String] = richer.getStartupConfigs.filter(_._2.isInstanceOf[String]).map { case (k, v: String) => k -> v }
        val jars: String = EnvConfiguration.ENGINE_CONN_JARS.getValue(configs)
        addFiles(jars)
        val files: String = EnvConfiguration.ENGINE_CONN_CLASSPATH_FILES.getValue(configs)
        addFiles(files)
      case _ =>
    }
    environment
  }
  private implicit def buildPath(paths: Seq[String]): String = Paths.get(paths.head, paths.tail: _*).toFile.getPath
}
