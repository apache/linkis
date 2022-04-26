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

package org.apache.linkis.engineconnplugin.sqoop.executor

import org.apache.linkis.common.utils.{OverloadUtils, Utils}
import org.apache.linkis.engineconn.common.conf.EngineConnConf.ENGINE_CONN_LOCAL_PATH_PWD_KEY
import org.apache.linkis.engineconn.once.executor.{OnceExecutorExecutionContext, OperableOnceExecutor}
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopEnvConfiguration.{LINKIS_QUEUE_NAME, LINKIS_SQOOP_TASK_MANAGER_CPU_CORES, LINKIS_SQOOP_TASK_MANAGER_MEMORY}
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, DriverAndYarnResource, LoadInstanceResource, NodeResource, YarnResource}
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse
import org.apache.linkis.engineconnplugin.sqoop.client.LinkisSqoopClient
import org.apache.linkis.engineconnplugin.sqoop.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopEngineConnContext
import org.apache.linkis.protocol.engine.JobProgressInfo

import java.util
import java.util.concurrent.{Future, TimeUnit}


class SqoopOnceCodeExecutor(override val id: Long,
                            override protected val sqoopEngineConnContext: SqoopEngineConnContext) extends SqoopOnceExecutor with OperableOnceExecutor{


  private var params:util.Map[String, String] = _;
  private var future: Future[_] = _
  private var daemonThread: Future[_] = _
  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext, options: Map[String, String]): Unit = {
    var isFailed = false;
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        params = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.get("sqoop-params").asInstanceOf[util.Map[String, String]]
        info("Try to execute params."+params)
          if(runSqoop(params) != 0) {
            isFailed = true
            setResponse(ErrorExecuteResponse("Run code failed!", new JobExecutionException("Exec Sqoop Code Error")))
            tryFailed()
          }
          info("All codes completed, now stop SqoopEngineConn.")
          closeDaemon()
          this synchronized notify()
      }
    })
  }
  protected def runSqoop(params: util.Map[String, String]): Int = {
    LinkisSqoopClient.run(params,System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue))
  }

  override protected def waitToRunning(): Unit ={
    if(!isCompleted) daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        if(!(future.isDone || future.isCancelled)){
          info("The Sqoop Process In Running")
        }
      }
    }, 10000,15000 , TimeUnit.MILLISECONDS)
  }
  override def getCurrentNodeResource(): NodeResource = {
    val resource = new DriverAndYarnResource(
      new LoadInstanceResource(OverloadUtils.getProcessMaxMemory,
        1,
        1),
      new YarnResource(LINKIS_SQOOP_TASK_MANAGER_MEMORY.getValue*getNumTasks, LINKIS_SQOOP_TASK_MANAGER_CPU_CORES.getValue*getNumTasks, 0, LINKIS_QUEUE_NAME.getValue)
    )
    val engineResource = new CommonNodeResource
    engineResource.setUsedResource(resource)
    engineResource
  }
  def getNumTasks: Int ={
    if(params != null) {
      params.getOrDefault("sqoop.args.num.mappers","1").toInt
    }else{
      0
    }
  }
  protected def closeDaemon(): Unit = {
    if(daemonThread != null) daemonThread.cancel(true)
  }

  override def getProgress: Float = LinkisSqoopClient.progress()

  override def getProgressInfo: Array[JobProgressInfo] = {
    val infoMap = LinkisSqoopClient.getProgressInfo
    val jobInfo =  JobProgressInfo(LinkisSqoopClient.getApplicationId,infoMap.get("totalTasks"),infoMap.get("runningTasks"),infoMap.get("failedTasks"),infoMap.get("succeedTasks"))
    info("Progress Job Info"+jobInfo)
    Array(jobInfo)
  }


  override def getMetrics: util.Map[String, Any] = LinkisSqoopClient.getMetrics.asInstanceOf[util.Map[String, Any]]

  override def getDiagnosis: util.Map[String, Any] = LinkisSqoopClient.getDiagnosis.asInstanceOf[util.Map[String,Any]]
}
