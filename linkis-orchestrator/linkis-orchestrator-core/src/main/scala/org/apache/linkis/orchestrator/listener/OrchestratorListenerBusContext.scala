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

package org.apache.linkis.orchestrator.listener

import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration

/**
 */

trait OrchestratorListenerBusContext {

  def getOrchestratorAsyncListenerBus: OrchestratorAsyncListenerBus

  def getOrchestratorSyncListenerBus: OrchestratorSyncListenerBus

}

class OrchestratorListenerBusContextImpl extends OrchestratorListenerBusContext {

  private val orchestratorAsyncListenerBus: OrchestratorAsyncListenerBus =
    new OrchestratorAsyncListenerBus(
      OrchestratorConfiguration.ORCHESTRATOR_LISTENER_ASYNC_QUEUE_CAPACITY.getValue,
      "Orchestrator-Listener-Asyn-Thread",
      OrchestratorConfiguration.ORCHESTRATOR_LISTENER_ASYNC_CONSUMER_THREAD_MAX.getValue,
      OrchestratorConfiguration.ORCHESTRATOR_LISTENER_ASYNC_CONSUMER_THREAD_FREE_TIME_MAX.getValue.toLong
    )

  private val orchestratorSyncListenerBus: OrchestratorSyncListenerBus =
    new OrchestratorSyncListenerBus

  init()

  private def init(): Unit = {
    this.orchestratorAsyncListenerBus.start()
  }

  override def getOrchestratorAsyncListenerBus: OrchestratorAsyncListenerBus = {
    orchestratorAsyncListenerBus
  }

  override def getOrchestratorSyncListenerBus: OrchestratorSyncListenerBus =
    orchestratorSyncListenerBus

}

object OrchestratorListenerBusContext {

  private val listenerBusContext = new OrchestratorListenerBusContextImpl

  def getListenerBusContext(): OrchestratorListenerBusContextImpl = listenerBusContext

  def createBusContext: OrchestratorListenerBusContext = new OrchestratorListenerBusContextImpl

}
