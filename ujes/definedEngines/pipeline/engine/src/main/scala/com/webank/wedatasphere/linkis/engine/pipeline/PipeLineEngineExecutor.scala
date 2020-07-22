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

package com.webank.wedatasphere.linkis.engine.pipeline

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext}
import com.webank.wedatasphere.linkis.engine.pipeline.exception.PipeLineErrorException
import com.webank.wedatasphere.linkis.engine.pipeline.executor.PipeLineExecutorFactory
import com.webank.wedatasphere.linkis.protocol.config.{RequestQueryAppConfig, ResponseQueryConfig}
import com.webank.wedatasphere.linkis.protocol.engine.{EngineState, JobProgressInfo}
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, Resource}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteResponse, SingleTaskInfoSupport}
import com.webank.wedatasphere.linkis.server._

/**
 * Created by johnnwang on 2018/11/13.
 */
class PipeLineEngineExecutor(options: JMap[String, String]) extends EngineExecutor(outputPrintLimit = 10, false) with SingleTaskInfoSupport with Logging {
  override def getName: String = "pipeLineEngine"

  val sender = Sender.getSender("cloud-publicservice");
  private var index = 0
  private var progressInfo: JobProgressInfo = _

  override def getActualUsedResources: Resource = new LoadInstanceResource(Runtime.getRuntime.totalMemory() - Runtime.getRuntime.freeMemory(), 1, 1)

  override def init(): Unit = {
    info("init pipeLineEngine...")
    transition(EngineState.Idle)
    super.init()
    info("init pipeLineEngine end...")
  }


  override protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = {
    /*    if(engineExecutorContext.getJobId.isDefined) {
          progressInfo += new JobProgressInfo(engineExecutorContext.getJobId.get, 1, 1, 0, 0)
        }*/
    //fifo
    index += 1
    var failedTasks = 0
    var succeedTasks = 1
    val newOptions = sender.ask(RequestQueryAppConfig(options.get("user"), options.get("creator"), "pipeline")).asInstanceOf[ResponseQueryConfig].getKeyAndValue
    newOptions.foreach({ case (k, v) => info(s"key is $k, value is $v") })
    PipeLineExecutorFactory.listPipeLineExecutor.foreach(f => f.init(newOptions))
    val regex = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s?".r
    try {
      code match {
        case regex(sourcePath, destPath) => {
          if (destPath.contains(".")) {
            PipeLineExecutorFactory.listPipeLineExecutor.find(f => "cp".equals(f.Kind)).get.execute(sourcePath, destPath, engineExecutorContext)
          } else {
            PipeLineExecutorFactory.listPipeLineExecutor.find(f => newOptions.get("pipeline.output.mold").equalsIgnoreCase(f.Kind)).map(_.execute(sourcePath, destPath, engineExecutorContext)).get
          }
        }
        case _ => throw new PipeLineErrorException(70007, "")
      }
    } catch {
      case e: Exception => failedTasks = 1; succeedTasks = 0; throw e
    }
    finally {
      info("begin to remove osCache:" + engineExecutorContext.getJobId.get)
      OutputStreamCache.osCache.remove(engineExecutorContext.getJobId.get)
      progressInfo = JobProgressInfo(getName + "_" + index, 1, 0, failedTasks, succeedTasks)
    }
  }

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = null


  override def close(): Unit = {

  }

  override def progress(): Float = if (progressInfo == null) 0f else 1f

  override def getProgressInfo: Array[JobProgressInfo] = null

  override def log(): String = {
    "PipeLine engine is running"
  }
}
