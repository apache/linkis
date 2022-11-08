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

package org.apache.linkis.rpc.sender.spring

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.rpc.interceptor.AbstractRPCServerLoader

import scala.concurrent.duration.Duration

class SpringRPCServerLoader extends AbstractRPCServerLoader {

  override def refreshAllServers(): Unit = {}

  override val refreshMaxWaitTime: Duration =
    RPCConfiguration.RPC_SERVICE_REFRESH_MAX_WAIT_TIME.getValue.toDuration

  override def getDWCServiceInstance(
      serviceInstance: SpringCloudServiceInstance
  ): ServiceInstance = {
    val applicationName = serviceInstance.getServiceId
    ServiceInstance(applicationName, s"${serviceInstance.getHost}:${serviceInstance.getPort}")
  }

}
