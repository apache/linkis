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
 
package org.apache.linkis.engineconn.executor.listener

import org.apache.linkis.engineconn.executor.conf.EngineConnExecutorConfiguration

class ExecutorListenerBusContext {

  private val engineConnAsyncListenerBus: EngineConnAsyncListenerBus = new EngineConnAsyncListenerBus(
    EngineConnExecutorConfiguration.ENGINE_SERVER_LISTENER_ASYNC_QUEUE_CAPACITY.getValue,
    "EngineConn-Asyn-Thread",
    EngineConnExecutorConfiguration.ENGINE_SERVER_LISTENER_ASYNC_CONSUMER_THREAD_MAX.getValue,
    EngineConnExecutorConfiguration.ENGINE_SERVER_LISTENER_ASYNC_CONSUMER_THREAD_FREE_TIME_MAX.getValue.toLong
  )

  private val engineConnSyncListenerBus: EngineConnSyncListenerBus = new EngineConnSyncListenerBus

  def getEngineConnAsyncListenerBus: EngineConnAsyncListenerBus = {
    engineConnAsyncListenerBus
  }

  def getEngineConnSyncListenerBus: EngineConnSyncListenerBus = engineConnSyncListenerBus

}

object ExecutorListenerBusContext {

  private val executorListenerBusContext = new ExecutorListenerBusContext

  def getExecutorListenerBusContext() = executorListenerBusContext
}