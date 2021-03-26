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

package com.webank.wedatasphere.linkis.engine.executors

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.PythonSession
import com.webank.wedatasphere.linkis.engine.exception.EngineException
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext}
import com.webank.wedatasphere.linkis.engine.rs.RsOutputStream
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, Resource}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer._
import org.apache.commons.io.IOUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by allenlliu on 2019/4/8.
  */
class PythonEngineExecutor(outputPrintLimit: Int) extends EngineExecutor(outputPrintLimit, false) with SingleTaskOperateSupport with SingleTaskInfoSupport with Logging {
  override def getName: String = Sender.getThisServiceInstance.getInstance
  private val lineOutputStream = new RsOutputStream
  private[executors] var engineExecutorContext: EngineExecutorContext = _
  override def getActualUsedResources: Resource = {
    new LoadInstanceResource(Runtime.getRuntime.totalMemory() - Runtime.getRuntime.freeMemory(), 2, 1)
  }

 private val pySession = new PythonSession

  override protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = {
    if(engineExecutorContext != this.engineExecutorContext){
      this.engineExecutorContext = engineExecutorContext
      pySession.setEngineExecutorContext(engineExecutorContext)
      //lineOutputStream.reset(engineExecutorContext)
      info("Python executor reset new engineExecutorContext!")
    }
    engineExecutorContext.appendStdout(s"$getName >> ${code.trim}")
    pySession.execute(code)
    //lineOutputStream.flush()
   SuccessExecuteResponse()
  }

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = {
    val newcode = completedLine + code
    info("newcode is " + newcode)
    executeLine(engineExecutorContext, newcode)
  }

  override def kill(): Boolean = true

  override def pause(): Boolean = true

  override def resume(): Boolean = true

  override def progress(): Float = {
    if (this.engineExecutorContext != null){
      this.engineExecutorContext.getCurrentParagraph / this.engineExecutorContext.getTotalParagraph.asInstanceOf[Float]
    }else 0.0f
  }

  override def getProgressInfo: Array[JobProgressInfo] = {
    val jobProgressInfos = new ArrayBuffer[JobProgressInfo]()
    jobProgressInfos.toArray
    Array.empty
  }

  override def log(): String = ""

  override def close(): Unit = {
    IOUtils.closeQuietly(lineOutputStream)
    var isKill:Boolean = false
    try {
      pySession.close
      isKill = true;
    } catch {
      case e: Throwable =>
        throw new EngineException(60004, "Engine shutdown exception（引擎关闭异常）")
    }
  }
}
