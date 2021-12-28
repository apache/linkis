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
 
package org.apache.linkis.manager.engineplugin.shell.executor

import java.io.{BufferedReader, InputStreamReader}
import java.util
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadInstanceResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.engineplugin.shell.exception.ShellCodeErrorException
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.linkis.engineconn.acessible.executor.log.LogHelper

import scala.collection.mutable.ArrayBuffer

class ShellEngineConnExecutor(id: Int) extends ComputationExecutor with Logging {

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
      info("Shell executor reset new engineExecutionContext!")
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
        LogHelper.logCache.cacheLog(line)
      }
      val errorLog = Stream.continually(errorsReader.readLine).takeWhile(_ != null).mkString("\n")
      val exitCode = process.waitFor()
      if (StringUtils.isNotEmpty(errorLog) || exitCode != 0) {
        error(s"exitCode is $exitCode")
        error(errorLog)
        engineExecutionContext.appendStdout("failed to execute shell (shell执行失败)")
        engineExecutionContext.appendStdout(errorLog)
        ErrorExecuteResponse("run shell failed", ShellCodeErrorException())
      } else SuccessExecuteResponse()
    } catch {
      case e: Exception => {
        error("Execute shell code failed, reason:", e)
        ErrorExecuteResponse("run shell failed", e)
      }
      case t: Throwable => ErrorExecuteResponse("Internal error executing shell process(执行shell进程内部错误)", t)
    } finally {

      IOUtils.closeQuietly(bufferedReader)

      IOUtils.closeQuietly(errorsReader)
    }
  }

  private def generateRunCode(code: String): Array[String] = {
    Array("sh", "-c", code)
  }

  override def getId(): String = Sender.getThisServiceInstance.getInstance + "_" + id

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = {
    val jobProgressInfo = new ArrayBuffer[JobProgressInfo]()
    if (null == this.engineExecutionContext) {
      return jobProgressInfo.toArray
    }
    if (0.0f == progress(taskID)) {
      jobProgressInfo += JobProgressInfo(engineExecutionContext.getJobId.getOrElse(""), 1, 1, 0, 0)
    } else {
      jobProgressInfo += JobProgressInfo(engineExecutionContext.getJobId.getOrElse(""), 1, 0, 0, 1)
    }
    jobProgressInfo.toArray
  }

  override def progress(taskID: String): Float = {
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
