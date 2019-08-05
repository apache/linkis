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

package com.webank.wedatasphere.linkis.enginemanager.process
import java.io.File
import java.nio.file.Paths

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, Configuration}
import com.webank.wedatasphere.linkis.common.utils.ClassUtils
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration._
import com.webank.wedatasphere.linkis.enginemanager.exception.EngineManagerErrorException
import com.webank.wedatasphere.linkis.enginemanager.impl.UserEngineResource
import com.webank.wedatasphere.linkis.enginemanager.{AbstractEngineCreator, EngineResource}
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by johnnwang on 2018/10/11.
  */
abstract class JavaProcessEngineBuilder extends ProcessEngineBuilder {

  protected var port: Int = _
  protected var request: RequestEngine = _
  protected var userEngineResource: UserEngineResource = _

  protected val commandLine:ArrayBuffer[String] = ArrayBuffer[String]()

  protected def getExtractJavaOpts: String
  protected def getAlias(request: RequestEngine): String
  protected def getExtractClasspath: Array[String]
  protected def classpathCheck(jarOrFiles: Array[String]): Unit
  protected val addApacheConfigPath: Boolean

  override def getEngineResource: EngineResource = userEngineResource

  override def getRequestEngine: RequestEngine = request

  override def build(engineRequest: EngineResource, request: RequestEngine): Unit = {
    this.request = request
    userEngineResource = engineRequest.asInstanceOf[UserEngineResource]
    val javaHome = JAVA_HOME.getValue(request.properties)
    if(StringUtils.isEmpty(javaHome)) {
      warn("We cannot find the java home, use java to run storage repl web server.")
      commandLine += "java"
    } else {
      commandLine += Paths.get(javaHome, "bin/java").toAbsolutePath.toFile.getAbsolutePath
    }
    val clientMemory = ENGINE_CLIENT_MEMORY.getValue(request.properties).toString
    commandLine += ("-Xmx" + clientMemory)
    commandLine += ("-Xms" + clientMemory)
    val javaOPTS = getExtractJavaOpts
    val alias = getAlias(request)
    if(StringUtils.isNotEmpty(DEFAULT_JAVA_OPTS.getValue))
      DEFAULT_JAVA_OPTS.getValue.format(engineGCLogPath(port, userEngineResource.getUser, alias)).split("\\s+").foreach(commandLine += _)
    if(StringUtils.isNotEmpty(javaOPTS)) javaOPTS.split("\\s+").foreach(commandLine += _)
    //engineLogJavaOpts(port, alias).trim.split(" ").foreach(commandLine += _)
    if(Configuration.IS_TEST_MODE.getValue) {
      val port = AbstractEngineCreator.getNewPort
      info(s"$toString open debug mode with port $port.")
      commandLine += s"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$port"
    }
    val classpath = getClasspath(request.properties, getExtractClasspath)
    classpathCheck(classpath)
    commandLine += "-Djava.library.path=" + HADOOP_LIB_NATIVE.getValue
    commandLine += "-cp"
    commandLine += classpath.mkString(":")
    commandLine += "com.webank.wedatasphere.linkis.engine.DataWorkCloudEngineApplication"
  }

  override def start(args: Array[String]): Process = {
    commandLine += args.mkString(" ")
    val command = Seq(JavaProcessEngineBuilder.sudoUserScript.getValue, request.user, commandLine.mkString(" "))
    val pb = new ProcessBuilder(command:_*)
    info("Running " + command.mkString(" "))
    pb.redirectErrorStream(true)
    pb.redirectInput(ProcessBuilder.Redirect.PIPE)
    pb.start()
  }

  override def setPort(port: Int): Unit = this.port = port

  protected def getClasspath(properties: java.util.Map[String, String], extraClasspath: Array[String]): Array[String] = {
    val classpath = ArrayBuffer[String]()
    classpath += JavaProcessEngineBuilder.engineManagerJar.getValue
    classpath += JavaProcessEngineBuilder.engineManagerConfigPath.getValue
    val engineManagerLib = new File(JavaProcessEngineBuilder.engineManagerJar.getValue).getParentFile
    val allLibFileNames = engineManagerLib.listFiles.map(_.getName)
    classpath += (engineManagerLib.getPath + "/*")
    //depends on scala, now find available scala library
    val scalaLibs = allLibFileNames.filter(_.startsWith("scala-"))
    if(scalaLibs.isEmpty) {
      val scalaHome = SCALA_HOME.getValue(properties)
      if(StringUtils.isNotEmpty(scalaHome)) {
        classpath += Paths.get(scalaHome, "lib", "/*").toFile.getAbsolutePath
      } else {
        throw new EngineManagerErrorException(30021, "no scala library or spark library are found, we cannot instance it.")
      }
    }
    extraClasspath.foreach(classpath += _)
    val clientClasspath = ENGINE_CLIENT_EXTRACLASSPATH.getValue(properties)
    if(StringUtils.isNotBlank(clientClasspath)) clientClasspath.split(",").foreach(classpath += _)
    if(addApacheConfigPath) {
      classpath += HADOOP_CONF_DIR.getValue
      classpath += HBASE_CONF_DIR.getValue
      classpath += SPARK_CONF_DIR.getValue
      classpath += HIVE_CONF_DIR.getValue
    }
    classpath.toArray
  }
}
object JavaProcessEngineBuilder {

  val engineManagerJar = CommonVars[String]("wds.linkis.enginemanager.core.jar", ClassUtils.jarOfClass(classOf[JavaProcessEngineBuilder]).head)
  val engineManagerConfigPath = CommonVars[String]("wds.linkis.enginemanager.config.path", {
    val path = new File(engineManagerJar.getValue)
    val parentFile = path.getParentFile.getParentFile
    val realPath = parentFile.list().find(f => f == "config" || f == "conf")
    parentFile.getAbsolutePath + File.separator + realPath.getOrElse("")
  })
  val sudoUserScript = CommonVars[String]("wds.linkis.enginemanager.sudo.script", {
    val url = Thread.currentThread().getContextClassLoader.getResource("rootScript.sh")
    if (url == null) new File(engineManagerConfigPath.getValue, "util/rootScript.sh").getAbsolutePath
    else url.getPath
  })
}