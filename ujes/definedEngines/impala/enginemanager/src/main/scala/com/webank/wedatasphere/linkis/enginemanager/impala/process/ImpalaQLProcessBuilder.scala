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

package com.webank.wedatasphere.linkis.enginemanager.impala.process

import java.nio.file.Paths

import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.enginemanager.{AbstractEngineCreator, EngineResource}
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration.{DEFAULT_JAVA_OPTS, ENGINE_CLIENT_MEMORY, JAVA_HOME, engineGCLogPath}
import com.webank.wedatasphere.linkis.enginemanager.impala.conf.ImpalaEngineConfiguration
import com.webank.wedatasphere.linkis.enginemanager.impl.UserEngineResource
import com.webank.wedatasphere.linkis.enginemanager.process.JavaProcessEngineBuilder
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import scala.collection.mutable.ArrayBuffer

/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 * 
 */
class ImpalaQLProcessBuilder extends JavaProcessEngineBuilder{

  private val LOG = LoggerFactory.getLogger(getClass)

  override protected def getExtractJavaOpts: String = {
     val javaOpts = new ArrayBuffer[String]()
    javaOpts += ImpalaEngineConfiguration.IMPALA_CLIENT_OPTS.getValue(request.properties)
    javaOpts.mkString(" ")
    ""
  }

  override protected def getAlias(request: RequestEngine): String = {
     ImpalaEngineConfiguration.IMPALA_ENGINE_SPRING_APPLICATION_NAME.getValue
   }
 
  override protected def getExtractClasspath: Array[String] = {
    if (StringUtils.isNotBlank(ImpalaEngineConfiguration.IMPALA_CLIENT_EXTRACLASSPATH.getValue)){
      ImpalaEngineConfiguration.IMPALA_CLIENT_EXTRACLASSPATH.getValue.split(",")
    }else Array.empty
  }

  /**
    * check classpath of java cmd process
    * @param jarOrFiles Array[String]
    */
  override protected def classpathCheck(jarOrFiles: Array[String]): Unit = {
    for(jarOrFile <- jarOrFiles){
      checkJarOrFile(jarOrFile)
    }
  }
  //todo Check the jar of the classpath(对classpath的jar进行检查)
  private def checkJarOrFile(jarOrFile:String):Unit = {

  }


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
    if (request.properties.containsKey(ImpalaEngineConfiguration.IMPALA_CLIENT_MEMORY.key)){
      val settingClientMemory = request.properties.get(ImpalaEngineConfiguration.IMPALA_CLIENT_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")){
        request.properties.put(ImpalaEngineConfiguration.IMPALA_CLIENT_MEMORY.key, settingClientMemory + "g")
      }
    }
    val clientMemory = ImpalaEngineConfiguration.IMPALA_CLIENT_MEMORY.getValue(request.properties).toString
    if (clientMemory.toLowerCase().endsWith("g")){
      commandLine += ("-Xmx" + clientMemory.toLowerCase())
      commandLine += ("-Xms" + clientMemory.toLowerCase())
    }else{
      commandLine += ("-Xmx" + clientMemory + "g")
      commandLine += ("-Xms" + clientMemory + "g")
    }
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
    var classpath = getClasspath(request.properties, getExtractClasspath)
    classpath = classpath ++ request.properties.get("jars").split(",")
    classpathCheck(classpath)
    commandLine += "-Djava.library.path=/appcom/Install/hadoop/lib/native"
    commandLine += "-cp"
    commandLine += classpath.mkString(":")
    commandLine += "com.webank.wedatasphere.linkis.engine.DataWorkCloudEngineApplication"
  }

 
  override protected val addApacheConfigPath: Boolean = true
}
