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

package com.webank.wedatasphere.linkis.engineconnplugin.sqoop.executor
import com.webank.wedatasphere.linkis.common.utils.{OverloadUtils, Utils}
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.context.SqoopEnvConfiguration.{LINKIS_QUEUE_NAME, LINKIS_SQOOP_TASK_MANAGER_CPU_CORES, LINKIS_SQOOP_TASK_MANAGER_MEMORY}
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.exception.JobExecutionException
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.context.SqoopEngineConnContext
import com.webank.wedatasphere.linkis.governance.common.paser.{CodeParserFactory, CodeType}
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, DriverAndYarnResource, LoadInstanceResource, NodeResource, YarnResource}
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.scheduler.executer.ErrorExecuteResponse
import org.apache.commons.lang.StringUtils

import java.util.concurrent.{Future, TimeUnit}


class SqoopOnceCodeExecutor(override val id: Long,
                            override protected val sqoopEngineConnContext: SqoopEngineConnContext) extends SqoopOnceExecutor{



  private var current_code:String = _
  private var future: Future[_] = _
  private var daemonThread: Future[_] = _
  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext, options: Map[String, String]): Unit = {
    val codes: String = options(TaskConstant.CODE)
    var isFailed = false;
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        info("Try to execute codes.")
        val codeArray = CodeParserFactory.getCodeParser(CodeType.SQL).parse(codes).filter(StringUtils.isNotBlank)
        for ( code <- codeArray ) {
          if(runCode(code)!=0){
            error("The Code:"+code+" execute fail")
            isFailed = true
            setResponse(ErrorExecuteResponse("Run code failed!",new JobExecutionException("Exec Sqoop Code Error")))
            tryFailed()
          }
        }
          info("All codes completed, now stop SqoopEngineConn.")
          closeDaemon()
        if(!isFailed) {
          trySucceed()
        }
          this synchronized notify()
      }
    })
  }
  protected def runCode(code: String): Int = {
    current_code = code
    Sqoop.main(code.split(" "))
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
    if(current_code != null){
      val strings = current_code.toLowerCase.replaceAll("\\s+", " ").split(" ")
      var index = -1;
      for (i <- 0 until strings.length-1) {
        if("--num-mappers".equals(strings(i))){
          index = i
        }
      }
      try {
        if(index != -1) {
          strings(index).toInt
        }else{
          1
        }
      }catch {
        case ex:Exception => {
          warn("Sqoop Get Parallel Exception"+ex.getMessage)
          1
        }
      }
    }else{
      0
    }

  }
  protected def closeDaemon(): Unit = {
    if(daemonThread != null) daemonThread.cancel(true)
  }
}
