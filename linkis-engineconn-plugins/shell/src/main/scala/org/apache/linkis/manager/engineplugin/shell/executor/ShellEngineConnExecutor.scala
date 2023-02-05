/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.shell.executor

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.governance.common.utils.GovernanceUtils
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadInstanceResource,
  NodeResource
}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.engineplugin.shell.common.ShellEngineConnPluginConst
import org.apache.linkis.manager.engineplugin.shell.exception.ShellCodeErrorException
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io.{BufferedReader, File, FileReader, InputStreamReader, IOException}
import java.util
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class ShellEngineConnExecutor(id: Int) extends ComputationExecutor with Logging {

  private var engineExecutionContext: EngineExecutionContext = _

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  private var process: Process = _

  private var extractor: YarnAppIdExtractor = _

  override def init(): Unit = {
    logger.info(s"Ready to change engine state!")
    super.init
  }

  override def executeCompletely(
      engineExecutionContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = {
    val newcode = completedLine + code
    logger.debug("newcode is " + newcode)
    executeLine(engineExecutionContext, newcode)
  }

  override def executeLine(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {

    if (null != engineExecutionContext) {
      this.engineExecutionContext = engineExecutionContext
      logger.info("Shell executor reset new engineExecutionContext!")
    }

    var bufferedReader: BufferedReader = null
    var errorsReader: BufferedReader = null

    val completed = new AtomicBoolean(false)
    var errReaderThread: ReaderThread = null
    var inputReaderThread: ReaderThread = null

    try {
      engineExecutionContext.appendStdout(s"$getId >> ${code.trim}")

      val argsArr =
        if (
            engineExecutionContext.getTotalParagraph == 1 &&
            engineExecutionContext.getProperties != null &&
            engineExecutionContext.getProperties.containsKey(
              ShellEngineConnPluginConst.RUNTIME_ARGS_KEY
            )
        ) {
          Utils.tryCatch {
            val argsList = engineExecutionContext.getProperties
              .get(ShellEngineConnPluginConst.RUNTIME_ARGS_KEY)
              .asInstanceOf[util.ArrayList[String]]
            logger.info(
              "Will execute shell task with user-specified arguments: \'" + argsList
                .toArray(new Array[String](argsList.size()))
                .mkString("\' \'") + "\'"
            )
            argsList.toArray(new Array[String](argsList.size()))
          } { t =>
            logger.warn(
              "Cannot read user-input shell arguments. Will execute shell task without them.",
              t
            )
            null
          }
        } else {
          null
        }

      val workingDirectory =
        if (
            engineExecutionContext.getTotalParagraph == 1 &&
            engineExecutionContext.getProperties != null &&
            engineExecutionContext.getProperties.containsKey(
              ShellEngineConnPluginConst.SHELL_RUNTIME_WORKING_DIRECTORY
            )
        ) {
          Utils.tryCatch {
            val wdStr = engineExecutionContext.getProperties
              .get(ShellEngineConnPluginConst.SHELL_RUNTIME_WORKING_DIRECTORY)
              .asInstanceOf[String]
            if (isExecutePathExist(wdStr)) {
              logger.info(
                "Will execute shell task under user-specified working-directory: \'" + wdStr
              )
              wdStr
            } else {
              logger.warn(
                "User-specified working-directory: \'" + wdStr + "\' does not exist or user does not have access permission. " +
                  "Will execute shell task under default working-directory. Please contact the administrator!"
              )
              null
            }
          } { t =>
            logger.warn(
              "Cannot read user-input working-directory. Will execute shell task under default working-directory.",
              t
            )
            null
          }
        } else {
          null
        }

      val generatedCode = if (argsArr == null || argsArr.isEmpty) {
        generateRunCode(code)
      } else {
        generateRunCodeWithArgs(code, argsArr)
      }

      val processBuilder: ProcessBuilder = new ProcessBuilder(generatedCode: _*)
      if (StringUtils.isNotBlank(workingDirectory)) {
        processBuilder.directory(new File(workingDirectory))
      }

      processBuilder.redirectErrorStream(false)
      extractor = new YarnAppIdExtractor
      process = processBuilder.start()

      bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream))
      errorsReader = new BufferedReader(new InputStreamReader(process.getErrorStream))
      val counter: CountDownLatch = new CountDownLatch(2)
      inputReaderThread =
        new ReaderThread(engineExecutionContext, bufferedReader, extractor, true, counter)
      errReaderThread =
        new ReaderThread(engineExecutionContext, errorsReader, extractor, false, counter)

      inputReaderThread.start()
      errReaderThread.start()

      val exitCode = process.waitFor()
      counter.await()

      completed.set(true)

      if (exitCode != 0) {
        ErrorExecuteResponse("run shell failed", ShellCodeErrorException())
      } else SuccessExecuteResponse()
    } catch {
      case e: Exception =>
        logger.error("Execute shell code failed, reason:", e)
        ErrorExecuteResponse("run shell failed", e)
    } finally {
      if (null != errorsReader) {
        inputReaderThread.onDestroy()
      }
      if (null != inputReaderThread) {
        errReaderThread.onDestroy()
      }
      IOUtils.closeQuietly(bufferedReader)
      IOUtils.closeQuietly(errorsReader)
    }
  }

  private def isExecutePathExist(executePath: String): Boolean = {
    val etlHomeDir: File = new File(executePath)
    (etlHomeDir.exists() && etlHomeDir.isDirectory())
  }

  private def generateRunCode(code: String): Array[String] = {
    Array("sh", "-c", code)
  }

  private def generateRunCodeWithArgs(code: String, args: Array[String]): Array[String] = {
    Array("sh", "-c", "echo \"dummy " + args.mkString(" ") + "\" | xargs sh -c \'" + code + "\'")
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
      this.engineExecutionContext.getCurrentParagraph / this.engineExecutionContext.getTotalParagraph
        .asInstanceOf[Float]
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
    val resource = new CommonNodeResource
    resource.setUsedResource(
      NodeResourceUtils
        .applyAsLoadInstanceResource(EngineConnObject.getEngineCreationContext.getOptions)
    )
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
    /*
      Kill sub-processes
     */
    val pid = getPid(process)
    GovernanceUtils.killProcess(String.valueOf(pid), s"kill task $taskID process", false)
    /*
      Kill yarn-applications
     */
    val yarnAppIds = extractor.getExtractedYarnAppIds()
    GovernanceUtils.killYarnJobApp(yarnAppIds)
    logger.info(s"Finished kill yarn app ids in the engine of (${getId()}).}")
    super.killTask(taskID)

  }

  private def getPid(process: Process): Int = {
    Utils.tryCatch {
      val clazz = Class.forName("java.lang.UNIXProcess");
      val field = clazz.getDeclaredField("pid");
      field.setAccessible(true);
      field.get(process).asInstanceOf[Int]
    } { t =>
      logger.warn("Failed to acquire pid for shell process")
      -1
    }
  }

  override def close(): Unit = {
    try {
      process.destroy()
    } catch {
      case e: Exception =>
        logger.error(s"kill process ${process.toString} failed ", e)
      case t: Throwable =>
        logger.error(s"kill process ${process.toString} failed ", t)
    }
    super.close()
  }

}
