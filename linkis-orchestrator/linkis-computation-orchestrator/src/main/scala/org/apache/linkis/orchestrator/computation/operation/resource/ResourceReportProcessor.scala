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

package org.apache.linkis.orchestrator.computation.operation.resource

import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.orchestrator.Orchestration

import java.io.Closeable
import java.util

import scala.collection.mutable.ArrayBuffer

class ResourceReportProcessor(
    rootExecTaskId: String,
    orchestration: Orchestration,
    resourceReportOperation: ResourceReportOperation
) extends Closeable {

  private val listener = new ArrayBuffer[ResourceReportEvent => Unit]()

  def registerResourceReportNotify(notify: ResourceReportEvent => Unit): Unit = {
    listener += notify
  }

  def resourceReport(resourceMap: util.HashMap[String, ResourceWithStatus]): Unit = {
    val resourceReport = ResourceReportEvent(orchestration, resourceMap)
    listener.foreach(notify => notify.apply(resourceReport))
  }

  override def close(): Unit = {
    resourceReportOperation.removeResourceReportProcessor(rootExecTaskId)
    listener.clear()
  }

}
