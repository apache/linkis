/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineplugin.distcp.executor

import com.google.gson.Gson
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.engineplugin.distcp.exception.DistcpCodeErrorException
import com.webank.wedatasphere.linkis.engineplugin.distcp.utils.{Code, FormatCode}
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, LoadInstanceResource, NodeResource}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import java.io.{BufferedReader, InputStreamReader}
import java.util
import scala.collection.mutable.ArrayBuffer

class DistcpEngineConnExecutor (id: Int) extends ComputationExecutor with Logging{
  private var engineExecutionContext: EngineExecutionContext = _

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  private var process: Process = _

  override def init(): Unit = {
    info(s"Ready to change engine state!")
    super.init
  }

  override def executeCompletely(engineExecutionContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = {
    val newcode = completedLine + code
    debug("newcode is " + newcode)
    executeLine(engineExecutionContext, newcode)
  }

  override def executeLine(engineExecutionContext: EngineExecutionContext, code: String): ExecuteResponse = {

    if (engineExecutionContext != this.engineExecutionContext) {
      this.engineExecutionContext = engineExecutionContext
      info("Distcp executor reset new engineExecutionContext!")
    }

    var bufferedReader: BufferedReader = null
    var errorsReader: BufferedReader = null

    try {
      engineExecutionContext.appendStdout(s"$getId >> ${code.trim}")
      val processBuilder: ProcessBuilder = new ProcessBuilder(generateRunCode(code): _*)
      processBuilder.redirectErrorStream(true)
      process = processBuilder.start()
      bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream))
      errorsReader = new BufferedReader(new InputStreamReader(process.getErrorStream))
      var line: String = null
      while ( {
        line = bufferedReader.readLine(); line != null
      }) {
        info(line)
        engineExecutionContext.appendTextResultSet(line)
      }
      val errorLog = Stream.continually(errorsReader.readLine).takeWhile(_ != null).mkString("\n")
      val exitCode = process.waitFor()
      if (StringUtils.isNotEmpty(errorLog) || exitCode != 0) {
        error(s"exitCode is $exitCode")
        error(errorLog)
        engineExecutionContext.appendStdout("failed to execute distcp (distcp执行失败)")
        engineExecutionContext.appendStdout(errorLog)
        ErrorExecuteResponse("run distcp failed", DistcpCodeErrorException())
      } else SuccessExecuteResponse()
    } catch {
      case e: Exception => {
        error("Execute distcp code failed, reason:", e)
        ErrorExecuteResponse("run distcp failed", e)
      }
      case t: Throwable => ErrorExecuteResponse("Internal error executing distcp process(执行distcp进程内部错误)", t)
    } finally {

      IOUtils.closeQuietly(bufferedReader)

      IOUtils.closeQuietly(errorsReader)
    }
  }

  private def generateRunCode(code: String): Array[String] = {
    val objectCode = new Gson().fromJson(code,classOf[Code])
    info("From The Json Code,Get the Object Code:"+objectCode.toString)
    val codeArray = FormatCode(objectCode).codes.toArray
    info("Exec Code Is:"+codeArray.mkString(" "))
    codeArray
  }

  override def getId(): String = Sender.getThisServiceInstance.getInstance + "_" + id

  override def getProgressInfo: Array[JobProgressInfo] = {
    val jobProgressInfo = new ArrayBuffer[JobProgressInfo]()
    if (0.0f == progress()) {
      jobProgressInfo += JobProgressInfo(engineExecutionContext.getJobId.getOrElse(""), 1, 1, 0, 0)
    } else {
      jobProgressInfo += JobProgressInfo(engineExecutionContext.getJobId.getOrElse(""), 1, 0, 0, 1)
    }
    jobProgressInfo.toArray
  }

  override def progress(): Float = {
    if (null != this.engineExecutionContext) {
      this.engineExecutionContext.getCurrentParagraph / this.engineExecutionContext.getTotalParagraph.asInstanceOf[Float]
    } else {
      0.0f
    }
  }

  override def supportCallBackLogs(): Boolean = {
    // todo
    true
  }

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = {
    null
  }

  override def getCurrentNodeResource(): NodeResource = {
    // todo refactor for duplicate
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

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (null != labels) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }


  override def killTask(taskID: String): Unit = {
    super.killTask(taskID)
  }

  override def close(): Unit = {
    try {
      process.destroy()
    } catch {
      case e: Exception =>
        error(s"kill process ${process.toString} failed ", e)
      case t: Throwable =>
        error(s"kill process ${process.toString} failed ", t)
    }
    super.close()
  }
}
