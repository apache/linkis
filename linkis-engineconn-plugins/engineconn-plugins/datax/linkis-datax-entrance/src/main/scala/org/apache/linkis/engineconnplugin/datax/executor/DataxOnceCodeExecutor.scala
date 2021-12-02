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

package org.apache.linkis.engineconnplugin.datax.executor

import com.alibaba.datax.DataxEngine
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.conf.EngineConnConf.ENGINE_CONN_LOCAL_PATH_PWD_KEY
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import org.apache.linkis.engineconnplugin.datax.context.DataxEnvConfiguration.ENGINE_DATAX_HOME
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, SuccessExecuteResponse}
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.linkis.engineconnplugin.datax.client.LinkisDataxClient
import org.apache.linkis.engineconnplugin.datax.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.datax.context.DataxEngineConnContext

import java.io.{BufferedReader, File}
import java.util.concurrent.{Future, TimeUnit}
import scala.util.Random


class DataxOnceCodeExecutor(override val id: Long,override protected val dataxEngineConnContext: DataxEngineConnContext) extends DataxOnceExecutor{


  private var future: Future[_] = _
  private var daemonThread: Future[_] = _
  private var  client:LinkisDataxClient = _
  var isFailed = false
  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext, options: Map[String, String]): Unit = {
    val code: String = options(TaskConstant.CODE)

    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        info("Try to execute codes."+code)
        runCode(code)
        info("All codes completed, now stop DataxEngineConn.")
        closeDaemon()
        if(!isFailed) {
          trySucceed()
        }
        this synchronized notify()
      }
    })
  }
  protected def runCode(code: String) = {
    info("Execute Datax Process")
    try {
      val client = new LinkisDataxClient()
      client.run(code,System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue));
    } catch {
      case e: Exception => {
        error("The Code:"+code+" execute fail")
        error(e.getMessage)
        isFailed = true
        setResponse(ErrorExecuteResponse("Run code failed!",new JobExecutionException("Exec Datax Code Error")))
        tryFailed()
        throw e
      }
    }

  }

  override def tryFailed(): Boolean = {
    client.destory()
    super.tryFailed()
  }
  override def close(): Unit = {
    client.destory()
    super.close()
  }

  override protected def waitToRunning(): Unit ={
    if(!isCompleted) daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        if(!(future.isDone || future.isCancelled)){
          info("The Datax Process In Running")
        }
      }
    }, 10000,15000 , TimeUnit.MILLISECONDS)
  }
  override def getCurrentNodeResource(): NodeResource = {
    val properties = EngineConnObject.getEngineCreationContext.getOptions
    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory = properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")) {
        properties.put(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key, settingClientMemory + "g")
      }
    }
    val actualUsedResource = new LoadInstanceResource(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.getValue(properties).toLong,
      EngineConnPluginConf.JAVA_ENGINE_REQUEST_CORES.getValue(properties), EngineConnPluginConf.JAVA_ENGINE_REQUEST_INSTANCE.getValue)
    val resource = new CommonNodeResource
    resource.setUsedResource(actualUsedResource)
    resource
  }

  protected def closeDaemon(): Unit = {
    if(daemonThread != null) daemonThread.cancel(true)
  }

}
