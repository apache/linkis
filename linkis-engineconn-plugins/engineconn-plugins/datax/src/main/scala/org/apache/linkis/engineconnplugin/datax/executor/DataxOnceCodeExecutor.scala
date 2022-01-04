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

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.conf.EngineConnConf.ENGINE_CONN_LOCAL_PATH_PWD_KEY
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.once.executor.{OnceExecutorExecutionContext, OperableOnceExecutor}
import org.apache.linkis.engineconnplugin.datax.client.LinkisDataxClient
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse
import org.apache.linkis.engineconnplugin.datax.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.datax.context.DataxEngineConnContext
import org.apache.linkis.engineconnplugin.datax.context.DataxEnvConfiguration.ENGINE_DATAX_PLUGIN_HOME
import org.apache.linkis.protocol.engine.JobProgressInfo

import java.io.{File, PrintWriter}
import java.nio.file.Files
import java.util
import java.util.concurrent.{Future, TimeUnit}


class DataxOnceCodeExecutor(override val id: Long,override protected val dataxEngineConnContext: DataxEngineConnContext) extends DataxOnceExecutor with OperableOnceExecutor{


  private var future: Future[_] = _
  private var daemonThread: Future[_] = _
  private var params:util.Map[String, String] = _
  var isFailed = false
  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext, options: Map[String, String]): Unit = {
    val code: String = options(TaskConstant.CODE)
    params = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.get("datax-params").asInstanceOf[util.Map[String, String]]
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        info("Try to execute codes."+code)
        if(runCode(code) !=0){
          isFailed = true
          setResponse(ErrorExecuteResponse("Run code failed!", new JobExecutionException("Exec Datax Code Error")))
          tryFailed()
        }
        info("All codes completed, now stop DataxEngineConn.")
        closeDaemon()
        if(!isFailed) {
          trySucceed()
        }
        this synchronized notify()
      }
    })
  }
  protected def runCode(code: String):Int = {
    info("Execute Datax Process")
    var args = Array("-mode","standalone","-jobid","1000001","-job",generateExecFile(code))
    if(params != null) {
        args = Array("-mode", params.getOrDefault("datax.args.mode", "standalone"),
          "-jobid", params.getOrDefault("datax.args.jobId", "1000001"),
          "-job", generateExecFile(code))
      }
      System.setProperty("datax.home",System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue));
      Files.createSymbolicLink(new File(System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue)+"/plugin").toPath,new File(ENGINE_DATAX_PLUGIN_HOME.getValue).toPath)
      LinkisDataxClient.main(args)
  }

  override def tryFailed(): Boolean = {
    LinkisDataxClient.close()
    super.tryFailed()
  }
  override def close(): Unit = {
    LinkisDataxClient.close();
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

  private def generateExecFile(code: String) :String = {
    val file = new File(System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue)+"/job_"+System.currentTimeMillis())
    val writer = new PrintWriter(file)
    writer.write(code)
    writer.close()
    file.getAbsolutePath
  }

  override def getProgress: Float = LinkisDataxClient.progress()

  override def getProgressInfo: Array[JobProgressInfo] = {
      val infoMap = LinkisDataxClient.getProgressInfo
      val jobInfo =  JobProgressInfo(LinkisDataxClient.getJobId,infoMap.get("totalTasks"),infoMap.get("runningTasks"),infoMap.get("failedTasks"),infoMap.get("succeedTasks"))
      info("Progress Job Info"+jobInfo)
      Array(jobInfo)
    }
  override def getMetrics: util.Map[String, Any] = LinkisDataxClient.getMetrics.asInstanceOf[util.Map[String, Any]]

  override def getDiagnosis: util.Map[String, Any] = LinkisDataxClient.getDiagnosis.asInstanceOf[util.Map[String, Any]]
}
