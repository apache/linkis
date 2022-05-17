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
 
package org.apache.linkis.orchestrator.computation.operation.progress

import org.apache.linkis.orchestrator.OrchestratorSession
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.linkis.orchestrator.computation.utils.TaskTreeUtil
import org.apache.linkis.orchestrator.listener.task.TaskProgressEvent

import java.util.concurrent.ConcurrentHashMap
import java.util.function.{DoubleBinaryOperator, ToDoubleFunction}
import scala.collection.JavaConverters.asScalaBufferConverter

/**
  * Default implement of progress operation
  */
class DefaultProgressOperation(orchestratorSession: OrchestratorSession) extends AbstractProgressOperation(orchestratorSession) {

  private var isInitialized = false

  def init(): Unit = {
    if (! isInitialized) synchronized{
      if (! isInitialized) {
        orchestratorSession.getOrchestratorSessionState.getOrchestratorAsyncListenerBus.addListener(this)
        isInitialized = true
      }
    }
  }

  override def getName: String = {
    if (! isInitialized) {
      init()
    }
    DefaultProgressOperation.PROGRESS_NAME
  }

  /**
    * To deal with the progress event from engineConn
    * @param taskProgressEvent progress event
    */
  override def onProgressOn(taskProgressEvent: TaskProgressEvent): Unit = {
    val execTask = taskProgressEvent.execTask
    Option(execTask).foreach(task => {
      val physicalContext = task.getPhysicalContext
      val progressInfo = physicalContext.getOption(ProgressConstraints.PROGRESS_MAP_NAME)
      val event = if(null != progressInfo.get){
        val progressMap = progressInfo.get.asInstanceOf[ConcurrentHashMap[String, Float]]
        //Update the progress value
        progressMap.put(execTask.getId, taskProgressEvent.progress)
        //Iterate the progress map and calculate the global progress value
        progressMap synchronized {
          //Make sure that the iterate function is serial
          val progressValueSum = progressMap.reduceValuesToDouble(1L,new ToDoubleFunction[Float] {
            override def applyAsDouble(t: Float): Double = t.asInstanceOf[Double]
          }, 0.0d, new DoubleBinaryOperator {
            override def applyAsDouble(left: Double, right: Double): Double = left + right
          })
          //Update the global progress value in event
          TaskProgressEvent(execTask, progressValueSum.asInstanceOf[Float] / progressMap.size.asInstanceOf[Float], taskProgressEvent.progressInfo)
        }
      } else {
        val progressMap = physicalContext.getOrElsePut(ProgressConstraints.PROGRESS_MAP_NAME, new ConcurrentHashMap[String, Float])
          .asInstanceOf[ConcurrentHashMap[String,Float]]
        //Init the value of progress as 0
        val codeExecTasks = TaskTreeUtil.getAllTaskRecursive(physicalContext.getRootTask, classOf[CodeLogicalUnitExecTask])
        if (null != codeExecTasks) {
          codeExecTasks.asScala.foreach(task => progressMap.put(task.getId, 0.0f))
        }
        progressMap.put(execTask.getId, taskProgressEvent.progress)
        taskProgressEvent
      }

      Option(execTaskToProgressProcessor.get(execTask.getPhysicalContext.getRootTask.getId)).foreach( progress => {
        progress.onProgress(event.progress, event.progressInfo)
      })
    })

  }
}

object DefaultProgressOperation{
  val PROGRESS_NAME = "progress-default"
}
