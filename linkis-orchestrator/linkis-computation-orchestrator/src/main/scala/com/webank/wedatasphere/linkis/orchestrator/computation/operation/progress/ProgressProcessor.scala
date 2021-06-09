/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.operation.progress

import java.io.Closeable

import com.webank.wedatasphere.linkis.orchestrator.Orchestration
import com.webank.wedatasphere.linkis.orchestrator.computation.operation
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo

import scala.collection.mutable.ArrayBuffer

/**
 *
 */
class ProgressProcessor(rootExecTaskId: String,
                        orchestration: Orchestration, progressObtainOperation: AbstractProgressOperation) extends Closeable{

  private val listeners = new ArrayBuffer[ProgressInfoEvent => Unit]()

  /**
   * Listen entrance
   * @param notify
   */
  def doOnObtain(notify: ProgressInfoEvent => Unit) : Unit = {
    listeners += notify
  }

  def onProgress(progress: Float, progressInfo: Array[JobProgressInfo]): Unit = {
     val progressInfoEvent = operation.progress.ProgressInfoEvent(orchestration, progress, progressInfo)
     listeners.foreach(_(progressInfoEvent))
  }

  override def close(): Unit = {
    this.progressObtainOperation
  }
}
