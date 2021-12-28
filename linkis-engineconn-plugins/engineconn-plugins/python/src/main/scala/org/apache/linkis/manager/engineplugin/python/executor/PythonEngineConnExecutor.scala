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
 
package org.apache.linkis.manager.engineplugin.python.executor

import java.util

import org.apache.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.launch.EngineConnServer
import org.apache.linkis.governance.common.paser.PythonCodeParser
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadInstanceResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}

import scala.collection.mutable.ArrayBuffer


class PythonEngineConnExecutor(id: Int, pythonSession: PythonSession, outputPrintLimit: Int) extends ComputationExecutor(outputPrintLimit) {

  private var engineExecutionContext: EngineExecutionContext = _

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  override def init(): Unit = {

    info(s"Ready to change engine state!")
    setCodeParser(new PythonCodeParser)
    super.init
  }

  private val pythonDefaultVersion: String = EngineConnServer.getEngineCreationContext.getOptions.getOrDefault("python.version", "python")

  override def executeLine(engineExecutionContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val pythonVersion = engineExecutionContext.getProperties.getOrDefault("python.version", pythonDefaultVersion).toString.toLowerCase()
    info(s" EngineExecutionContext user python.version = > ${pythonVersion}")
    System.getProperties.put("python.version", pythonVersion)
    info(s" System getProperties python.version = > ${System.getProperties.getProperty("python.version")}")
    if(engineExecutionContext != this.engineExecutionContext){
      this.engineExecutionContext = engineExecutionContext
      pythonSession.setEngineExecutionContext(engineExecutionContext)
//      lineOutputStream.reset(engineExecutorContext)
      info("Python executor reset new engineExecutorContext!")
    }
    engineExecutionContext.appendStdout(s"$getId >> ${code.trim}")
    pythonSession.execute(code)
//    lineOutputStream.flush()
    SuccessExecuteResponse()
  }

  override def executeCompletely(engineExecutionContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = {
    val newcode = completedLine + code
    debug("newcode is " + newcode)
    executeLine(engineExecutionContext, newcode)
  }

  override def progress(taskID: String): Float = {
    if (null != this.engineExecutionContext) {
      this.engineExecutionContext.getCurrentParagraph / this.engineExecutionContext.getTotalParagraph.asInstanceOf[Float]
    } else {
      0.0f
    }
  }

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

  override def getId(): String = Sender.getThisServiceInstance.getInstance + "_" + id


  override def killTask(taskID: String): Unit = {
    warn(s"Kill task : $taskID")
    super.killTask(taskID)
  }

  override def close(): Unit = {
    pythonSession.close
  }

}
