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

package org.apache.linkis.rpc.interceptor.common

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.protocol.BroadcastProtocol
import org.apache.linkis.rpc.{BaseRPCSender, RPCSpringBeanCache, Sender}
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.rpc.interceptor.{
  RPCInterceptor,
  RPCInterceptorChain,
  RPCInterceptorExchange
}
import org.apache.linkis.rpc.sender.SpringMVCRPCSender

import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.Future

@Component
class BroadcastRPCInterceptor extends RPCInterceptor with Logging {
  override val order: Int = 0

  implicit val executor = BroadcastRPCInterceptor.broadcastThreadPool

  protected def getSenders(broadcast: BroadcastProtocol, applicationName: String): Array[Sender] =
    if (broadcast.instances != null && broadcast.instances.nonEmpty) {
      broadcast.instances.map(instance =>
        Sender.getSender(ServiceInstance(applicationName, instance))
      )
    } else {
      var senders: Option[Array[Sender]] = None
      for (builder <- RPCSpringBeanCache.getBroadcastSenderBuilders if senders.isEmpty)
        senders = builder.build(broadcast, applicationName)
      senders.getOrElse(Sender.getInstances(applicationName).map(Sender.getSender))
    }

  override def intercept(
      interceptorExchange: RPCInterceptorExchange,
      chain: RPCInterceptorChain
  ): Any = interceptorExchange.getProtocol match {
    case broadcast: BroadcastProtocol if !broadcast.skipBroadcast =>
      val completedSize = new AtomicInteger(0)
      val senders = getSenders(broadcast, chain.getApplicationName)
      var failedError: Option[Throwable] = None
      broadcast.skipBroadcast = true
      senders.map(sender =>
        Future {
          Utils.tryCatch(sender.send(broadcast)) { t =>
            failedError = Some(t)
            val serviceInstance = sender match {
              case s: SpringMVCRPCSender => s.serviceInstance
              case b: BaseRPCSender => b.getApplicationName
              case _ => sender
            }
            logger.warn(s"broadcast to $serviceInstance failed!", t)
          }
        }.map { _ =>
          completedSize.incrementAndGet
          completedSize synchronized completedSize.notify
        }
      )
      val sendNums = senders.length
      while (completedSize.get() < sendNums) {
        completedSize synchronized completedSize.wait(2000)
      }
      if (broadcast.throwsIfAnyFailed) failedError.foreach(throw _)
    case _ => chain.handle(interceptorExchange)
  }

}

private object BroadcastRPCInterceptor {

  private val broadcastThreadPool = Utils.newCachedExecutionContext(
    RPCConfiguration.BDP_RPC_BROADCAST_THREAD_SIZE.getValue,
    "Broadcast-ThreadPool-"
  )

}
