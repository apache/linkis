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

package org.apache.linkis.orchestrator.computation.operation.progress

import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.orchestrator.Orchestration
import org.apache.linkis.orchestrator.computation.operation
import org.apache.linkis.protocol.engine.JobProgressInfo

import java.io.Closeable
import java.util

import scala.collection.mutable.ArrayBuffer

class ProgressProcessor(
    rootExecTaskId: String,
    orchestration: Orchestration,
    progressObtainOperation: AbstractProgressOperation
) extends Closeable {

  private val listeners = new ArrayBuffer[ProgressInfoEvent => Unit]()

  /**
   * Listen entrance
   * @param notify
   */
  def doOnObtain(notify: ProgressInfoEvent => Unit): Unit = {
    listeners += notify
  }

  def onProgress(
      progress: Float,
      progressInfo: Array[JobProgressInfo],
      resourceMap: util.HashMap[String, ResourceWithStatus],
      infoMap: util.HashMap[String, Object]
  ): Unit = {
    val progressInfoWithResourceEvent =
      ProgressInfoEvent(orchestration, progress, progressInfo, resourceMap, infoMap)
    listeners.foreach(_(progressInfoWithResourceEvent))
  }

  override def close(): Unit = {
    progressObtainOperation.removeLogProcessor(rootExecTaskId)
    listeners.clear()
  }

}
