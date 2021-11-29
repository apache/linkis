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
 
package org.apache.linkis.entranceclient.conf

import org.apache.linkis.common.conf.CommonVars

object ClientConfiguration {

  val CLIENT_ENGINE_MANAGER_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.client.enginemanager.application.name.default", "IOEngineManager")
  val CLIENT_ENGINE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.client.engine.application.name.default", "IOEngine")

  val CLIENT_DEFAULT_NAME: String = CommonVars("wds.linkis.client.name.default", "storageClient").getValue
  val CLIENT_DEFAULT_PARALLELISM_USERS = CommonVars("wds.linkis.client.parallelism.users.max", new Integer(100))

  val BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY_FOR_CLIENT = CommonVars("wds.linkis.client.rpc.receiver.asyn.queue.size.max", new Integer(3000))
  val BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX_FOR_CLIENT = CommonVars("wds.linkis.client.rpc.receiver.asyn.consumer.thread.max", new Integer(120))
  val CONCURRENT_ENGINE_MAX_PARALLELISM_FOR_CLIENT = CommonVars("wds.linkis.client.engine.maxParallelismJobs", new Integer(25))
}
