/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.rpc.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}

/**
  * Created by enjoyyin on 2019/1/14.
  */
object RPCConfiguration {

  val BDP_RPC_BROADCAST_THREAD_SIZE = CommonVars("wds.linkis.rpc.broadcast.thread.num", new Integer(10))

  val BDP_RPC_EUREKA_SERVICE_REFRESH_INTERVAL = CommonVars("wds.linkis.rpc.eureka.client.refresh.interval", new TimeType("1s"))
  val BDP_RPC_EUREKA_SERVICE_REFRESH_MAX_WAIT_TIME = CommonVars("wds.linkis.rpc.eureka.client.refresh.wait.time.max", new TimeType("5s"))
  val BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX = CommonVars("wds.linkis.rpc.receiver.asyn.consumer.thread.max", 10)
  val BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX = CommonVars("wds.linkis.rpc.receiver.asyn.consumer.freeTime.max", new TimeType("2m"))
  val BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY = CommonVars("wds.linkis.rpc.receiver.asyn.queue.size.max", 1000)

  val BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_MAX = CommonVars("wds.linkis.rpc.sender.asyn.consumer.thread.max", 5)
  val BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX = CommonVars("wds.linkis.rpc.sender.asyn.consumer.freeTime.max", new TimeType("2m"))
  val BDP_RPC_SENDER_ASYN_QUEUE_CAPACITY = CommonVars("wds.linkis.rpc.sender.asyn.queue.size.max", 300)

  val ENABLE_PUBLIC_SERVICE = CommonVars("wds.linkis.gateway.conf.enable.publicservice", true)
  val PUBLIC_SERVICE_APPLICATION_NAME = CommonVars("wds.linkis.gateway.conf.publicservice.name", "publicservice")
  val PUBLIC_SERVICE_LIST = CommonVars("wds.linkis.gateway.conf.publicservice.list", "query,jobhistory,application,configuration,filesystem,udf,variable").getValue.split(",")

  val BDP_RPC_INSTANCE_ALIAS_SERVICE_REFRESH_INTERVAL = CommonVars("wds.linkis.rpc.instancealias.refresh.interval", new TimeType("3s"))

  val CONTEXT_SERVICE_APPLICATION_NAME = CommonVars("wds.linkis.gateway.conf.contextservice.name", "contextservice")
}
