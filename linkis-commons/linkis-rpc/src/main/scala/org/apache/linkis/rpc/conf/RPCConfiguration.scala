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
 
package org.apache.linkis.rpc.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}


object RPCConfiguration {

  val BDP_RPC_BROADCAST_THREAD_SIZE = CommonVars("wds.linkis.rpc.broadcast.thread.num", new Integer(25))

  val BDP_RPC_EUREKA_SERVICE_REFRESH_INTERVAL = CommonVars("wds.linkis.rpc.eureka.client.refresh.interval", new TimeType("1s"))
  val BDP_RPC_EUREKA_SERVICE_REFRESH_MAX_WAIT_TIME = CommonVars("wds.linkis.rpc.eureka.client.refresh.wait.time.max", new TimeType("1m"))
  val BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX = CommonVars("wds.linkis.rpc.receiver.asyn.consumer.thread.max", 400)
  val BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX = CommonVars("wds.linkis.rpc.receiver.asyn.consumer.freeTime.max", new TimeType("2m"))
  val BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY = CommonVars("wds.linkis.rpc.receiver.asyn.queue.size.max", 5000)

  val BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_MAX = CommonVars("wds.linkis.rpc.sender.asyn.consumer.thread.max", 100)
  val BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX = CommonVars("wds.linkis.rpc.sender.asyn.consumer.freeTime.max", new TimeType("2m"))
  val BDP_RPC_SENDER_ASYN_QUEUE_CAPACITY = CommonVars("wds.linkis.rpc.sender.asyn.queue.size.max", 2000)


  val ENABLE_PUBLIC_SERVICE = CommonVars("wds.linkis.gateway.conf.enable.publicservice", true)
  val PUBLIC_SERVICE_APPLICATION_NAME = CommonVars("wds.linkis.gateway.conf.publicservice.name", "linkis-ps-publicservice")
  val PUBLIC_SERVICE_LIST = CommonVars("wds.linkis.gateway.conf.publicservice.list", "query,jobhistory,application,configuration,filesystem,udf,variable,microservice,errorcode,bml,datasource").getValue.split(",")

  val BDP_RPC_INSTANCE_ALIAS_SERVICE_REFRESH_INTERVAL = CommonVars("wds.linkis.rpc.instancealias.refresh.interval", new TimeType("3s"))

  val CONTEXT_SERVICE_APPLICATION_NAME = CommonVars("wds.linkis.gateway.conf.contextservice.name", "linkis-ps-cs")

  val ENABLE_LOCAL_MESSAGE = CommonVars("wds.linkis.rpc.conf.enable.local.message", false)
  val LOCAL_APP_LIST = CommonVars("wds.linkis.rpc.conf.local.app.list", "").getValue.split(",")

}
