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

package org.apache.linkis.rpc.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

import org.reflections.Reflections
import org.reflections.scanners.{MethodAnnotationsScanner, SubTypesScanner, TypeAnnotationsScanner}

object RPCConfiguration {

  val BDP_RPC_BROADCAST_THREAD_SIZE: CommonVars[Integer] =
    CommonVars("wds.linkis.rpc.broadcast.thread.num", 25)

  val RPC_SERVICE_REFRESH_MAX_WAIT_TIME: CommonVars[TimeType] =
    CommonVars(
      "linkis.rpc.client.refresh.wait.time.max",
      CommonVars("wds.linkis.rpc.eureka.client.refresh.wait.time.max", new TimeType("30s")).getValue
    )

  val BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX: CommonVars[Int] =
    CommonVars("wds.linkis.rpc.receiver.asyn.consumer.thread.max", 400)

  val BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX: CommonVars[TimeType] =
    CommonVars("wds.linkis.rpc.receiver.asyn.consumer.freeTime.max", new TimeType("2m"))

  val BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY: CommonVars[Int] =
    CommonVars("wds.linkis.rpc.receiver.asyn.queue.size.max", 5000)

  val BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_MAX: CommonVars[Int] =
    CommonVars("wds.linkis.rpc.sender.asyn.consumer.thread.max", 100)

  val BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX: CommonVars[TimeType] =
    CommonVars("wds.linkis.rpc.sender.asyn.consumer.freeTime.max", new TimeType("2m"))

  val BDP_RPC_SENDER_ASYN_QUEUE_CAPACITY: CommonVars[Int] =
    CommonVars("wds.linkis.rpc.sender.asyn.queue.size.max", 2000)

  val PUBLIC_SERVICE_APP_PREFIX: String =
    CommonVars("wds.linkis.gateway.conf.publicservice.name", "linkis-ps-").getValue

  val ENABLE_PUBLIC_SERVICE: CommonVars[Boolean] =
    CommonVars("wds.linkis.gateway.conf.enable.publicservice", true)

  val PUBLIC_SERVICE_APPLICATION_NAME: CommonVars[String] =
    CommonVars("wds.linkis.gateway.conf.publicservice.name", "linkis-ps-publicservice")

  val PUBLIC_SERVICE_LIST: Array[String] = CommonVars(
    "wds.linkis.gateway.conf.publicservice.list",
    "cs,contextservice,data-source-manager,metadataQuery,metadatamanager,query,jobhistory,application,configuration,filesystem,udf,variable,microservice,errorcode,bml,datasource,basedata-manager"
  ).getValue.split(",")

  val METADATAQUERY_SERVICE_APPLICATION_NAME: CommonVars[String] =
    CommonVars("wds.linkis.gateway.conf.publicservice.name", "linkis-ps-metadataquery")

  val METADATAQUERY_SERVICE_LIST: Array[String] = CommonVars(
    "wds.linkis.gateway.conf.metadataquery.list",
    "metadatamanager,metadataquery"
  ).getValue.split(",")

  val LINKIS_MANAGER_SERVICE_NAME: CommonVars[String] =
    CommonVars("wds.linkis.gateway.conf.linkismanager.name", "linkis-cg-linkismanager")

  val LINKIS_MANAGER_SERVICE_LIST: Array[String] =
    CommonVars("wds.linkis.gateway.conf.linkismanager.list", "linkisManager,engineplugin").getValue
      .split(",")

  val BDP_RPC_INSTANCE_ALIAS_SERVICE_REFRESH_INTERVAL: CommonVars[TimeType] =
    CommonVars("wds.linkis.rpc.instancealias.refresh.interval", new TimeType("3s"))

  val CONTEXT_SERVICE_APPLICATION_NAME: CommonVars[String] =
    CommonVars("wds.linkis.gateway.conf.contextservice.name", "linkis-ps-cs")

  val ENABLE_LOCAL_MESSAGE: CommonVars[Boolean] =
    CommonVars("wds.linkis.rpc.conf.enable.local.message", false)

  val LOCAL_APP_LIST: Array[String] =
    CommonVars("wds.linkis.rpc.conf.local.app.list", "").getValue.split(",")

  val SERVICE_SCAN_PACKAGE: String =
    CommonVars("wds.linkis.ms.service.scan.package", "org.apache.linkis").getValue

  val ENABLE_SPRING_PARAMS: Boolean =
    CommonVars("wds.linkis.rpc.spring.params.enable", false).getValue

  val REFLECTIONS = new Reflections(
    SERVICE_SCAN_PACKAGE,
    new MethodAnnotationsScanner(),
    new TypeAnnotationsScanner(),
    new SubTypesScanner()
  )

  val BDP_RPC_CACHE_CONF_EXPIRE_TIME: CommonVars[Long] =
    CommonVars("wds.linkis.rpc.cache.expire.time", 120000L)

  val CONTEXT_SERVICE_REQUEST_PREFIX = "contextservice"

  val CONTEXT_SERVICE_NAME: String =
    if (
        ENABLE_PUBLIC_SERVICE.getValue && PUBLIC_SERVICE_LIST
          .exists(_.equalsIgnoreCase(CONTEXT_SERVICE_REQUEST_PREFIX))
    ) {
      PUBLIC_SERVICE_APPLICATION_NAME.getValue
    } else {
      CONTEXT_SERVICE_APPLICATION_NAME.getValue
    }

}
