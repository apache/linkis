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

package org.apache.linkis.rpc.interceptor

import org.apache.linkis.common.ServiceInstance

import scala.runtime.BoxedUnit

trait RPCInterceptorChain {
  def getApplicationName: String
  def handle(rpcInterceptorExchange: RPCInterceptorExchange): Any
}

class BaseRPCInterceptorChain(
    index: Int,
    rpcInterceptors: Array[RPCInterceptor],
    applicationName: String
) extends RPCInterceptorChain {
  def getRPCInterceptors: Array[RPCInterceptor] = rpcInterceptors

  override def handle(rpcInterceptorExchange: RPCInterceptorExchange): Any =
    if (index < rpcInterceptors.length) {
      val rpcInterceptor = rpcInterceptors(index)
      val chain = increment()
      rpcInterceptor.intercept(rpcInterceptorExchange, chain)
    } else BoxedUnit.UNIT

  def increment(): RPCInterceptorChain =
    new BaseRPCInterceptorChain(index + 1, getRPCInterceptors, applicationName)

  override def getApplicationName: String = applicationName
}

class ServiceInstanceRPCInterceptorChain(
    index: Int,
    rpcInterceptors: Array[RPCInterceptor],
    serviceInstance: ServiceInstance
) extends BaseRPCInterceptorChain(index, rpcInterceptors, serviceInstance.getApplicationName) {
  def getServiceInstance: ServiceInstance = serviceInstance

  override def increment(): RPCInterceptorChain =
    new ServiceInstanceRPCInterceptorChain(index + 1, getRPCInterceptors, serviceInstance)

}
