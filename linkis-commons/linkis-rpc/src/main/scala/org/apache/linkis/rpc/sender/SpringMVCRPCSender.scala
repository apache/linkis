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

package org.apache.linkis.rpc.sender

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.rpc.{BaseRPCSender, RPCMessageEvent, RPCSpringBeanCache}
import org.apache.linkis.rpc.interceptor.{RPCInterceptor, ServiceInstanceRPCInterceptorChain}

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment

private[rpc] class SpringMVCRPCSender private[rpc] (
    private[rpc] val serviceInstance: ServiceInstance
) extends BaseRPCSender(serviceInstance.getApplicationName) {

  override protected def getRPCInterceptors: Array[RPCInterceptor] =
    RPCSpringBeanCache.getRPCInterceptors

  override protected def createRPCInterceptorChain() =
    new ServiceInstanceRPCInterceptorChain(0, getRPCInterceptors, serviceInstance)

  @Autowired
  private var env: Environment = _

  /**
   * Deliver is an asynchronous method that requests the target microservice asynchronously,
   * ensuring that the target microservice is requested once, but does not guarantee that the target
   * microservice will successfully receive the request.
   * deliver是一个异步方法，该方法异步请求目标微服务，确保一定会请求目标微服务一次，但不保证目标微服务一定能成功接收到本次请求。
   * @param message
   *   Requested parameters(请求的参数)
   */
  override def deliver(message: Any): Unit =
    getRPCSenderListenerBus.post(RPCMessageEvent(message, serviceInstance))

  override def equals(obj: Any): Boolean = if (obj == null) {
    false
  } else {
    obj match {
      case sender: SpringMVCRPCSender => sender.serviceInstance == serviceInstance
      case _ => false
    }
  }

  override def hashCode(): Int = serviceInstance.hashCode()

  override val toString: String =
    if (StringUtils.isBlank(serviceInstance.getInstance)) {
      s"RPCSender(${serviceInstance.getApplicationName})"
    } else s"RPCSender($getApplicationName, ${serviceInstance.getInstance})"

  override def getSenderInstance(): String = {
    if (null != serviceInstance) {
      serviceInstance.getInstance
    } else {
      null
    }
  }

}
