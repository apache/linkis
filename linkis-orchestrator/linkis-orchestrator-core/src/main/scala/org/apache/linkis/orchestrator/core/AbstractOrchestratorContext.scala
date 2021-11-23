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
 
package org.apache.linkis.orchestrator.core

import java.util

import org.apache.linkis.orchestrator.OrchestratorContext
import org.apache.linkis.orchestrator.extensions.Extensions

import scala.collection.mutable.ArrayBuffer

/**
  *
  */
abstract class AbstractOrchestratorContext extends OrchestratorContext {

  private var isClosed = false
  private var globalState: GlobalState = _
  private val globalExtensions = new ArrayBuffer[Extensions[_]]()
  private val globalConfigs = new util.HashMap[String, Any]()
  private val orchestratorPlugins = new ArrayBuffer[OrchestratorPlugin]()

  protected def setGlobalState(globalState: GlobalState): Unit = this.globalState = globalState

  protected def addGlobalExtension(globalExtension: Extensions[_]): Unit = globalExtensions += globalExtension

  def addGlobalPlugin(globalPlugin: OrchestratorPlugin): Unit = orchestratorPlugins += globalPlugin

  override def getGlobalState: GlobalState = globalState

  override def getGlobalExtensions: Array[Extensions[_]] = globalExtensions.toArray

  override def getGlobalConfigs: util.Map[String, Any] = globalConfigs

  override def getOrchestratorPlugins: Array[OrchestratorPlugin] = orchestratorPlugins.toArray

  override def isActive: Boolean = !isClosed

  override def close(): Unit = {
    isClosed = true
  }

}
