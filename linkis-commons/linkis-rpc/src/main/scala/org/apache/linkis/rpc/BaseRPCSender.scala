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

package org.apache.linkis.rpc

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.exception.WarnException
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.protocol.Protocol
import org.apache.linkis.rpc.conf.RPCConfiguration.{
  BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX,
  BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_MAX,
  BDP_RPC_SENDER_ASYN_QUEUE_CAPACITY
}
import org.apache.linkis.rpc.interceptor._
import org.apache.linkis.rpc.transform.{RPCConsumer, RPCProduct}
import org.apache.linkis.server.Message
import org.apache.linkis.server.conf.ServerConfiguration

import java.util

import scala.concurrent.duration.Duration
import scala.runtime.BoxedUnit

import feign.{Feign, Retryer}
import feign.slf4j.Slf4jLogger

private[rpc] class BaseRPCSender extends Sender with Logging {
  private var name: String = _
  private var rpc: RPCReceiveRemote = _

  protected def getRPCInterceptors: Array[RPCInterceptor] = Array.empty

  protected def createRPCInterceptorChain(): RPCInterceptorChain =
    new BaseRPCInterceptorChain(0, getRPCInterceptors, getApplicationName)

  protected def createRPCInterceptorExchange(
      protocol: Protocol,
      op: => Any
  ): RPCInterceptorExchange =
    new BaseRPCInterceptorExchange(protocol, () => op)

  def this(applicationName: String) {
    this()
    name = applicationName
  }

  private def getRPC: RPCReceiveRemote = {
    if (rpc == null) this synchronized {
      if (rpc == null) rpc = newRPC
    }
    rpc
  }

  private[rpc] def getApplicationName = name

  protected def doBuilder(builder: Feign.Builder): Unit =
    builder.retryer(Retryer.NEVER_RETRY)

  protected def newRPC: RPCReceiveRemote = {
    val builder = Feign.builder.logger(new Slf4jLogger()).logLevel(feign.Logger.Level.FULL)
    doBuilder(builder)
    var url = if (name.startsWith("http://")) name else "http://" + name
    if (url.endsWith("/")) url = url.substring(0, url.length - 1)
    url += ServerConfiguration.BDP_SERVER_RESTFUL_URI.getValue
    builder.target(classOf[RPCReceiveRemote], url)
  }

  private def execute(message: Any)(op: => Any): Any = message match {
    case protocol: Protocol if getRPCInterceptors.nonEmpty =>
      val rpcInterceptorChain = createRPCInterceptorChain()
      rpcInterceptorChain.handle(createRPCInterceptorExchange(protocol, op))
    case _ => op
  }

  override def ask(message: Any): Any = execute(message) {
    val msg = RPCProduct.getRPCProduct.toMessage(message)
    BaseRPCSender.addInstanceInfo(msg.getData)
    val response = getRPC.receiveAndReply(msg)
    RPCConsumer.getRPCConsumer.toObject(response)
  }

  override def ask(message: Any, timeout: Duration): Any = execute(message) {
    val msg = RPCProduct.getRPCProduct.toMessage(message)
    msg.data("duration", timeout.toMillis)
    BaseRPCSender.addInstanceInfo(msg.getData)
    val response = getRPC.receiveAndReplyInMills(msg)
    RPCConsumer.getRPCConsumer.toObject(response)
  }

  private def sendIt(message: Any, op: Message => Message): Unit = execute(message) {
    val msg = RPCProduct.getRPCProduct.toMessage(message)
    BaseRPCSender.addInstanceInfo(msg.getData)
    RPCConsumer.getRPCConsumer.toObject(op(msg)) match {
      case w: WarnException => logger.warn("RPC requests an alarm!(RPC请求出现告警！)", w)
      case _: BoxedUnit =>
    }
  }

  override def send(message: Any): Unit = sendIt(message, getRPC.receive)

  /**
   * Deliver is an asynchronous method that requests the target microservice asynchronously,
   * ensuring that the target microservice is requested once, but does not guarantee that the target
   * microservice will successfully receive the request.
   * deliver是一个异步方法，该方法异步请求目标微服务，确保一定会请求目标微服务一次，但不保证目标微服务一定能成功接收到本次请求。
   * @param message
   *   请求的参数
   */
  override def deliver(message: Any): Unit =
    BaseRPCSender.rpcSenderListenerBus.post(RPCMessageEvent(message, ServiceInstance(name, null)))

  protected def getRPCSenderListenerBus = BaseRPCSender.rpcSenderListenerBus

  override def equals(obj: Any): Boolean = {
    if (obj == null) {
      false
    } else {
      obj match {
        case sender: BaseRPCSender => name == sender.name
        case _ => false
      }
    }
  }

  override def hashCode(): Int = if (name == null) 0 else name.hashCode

  override def toString: String = s"RPCSender($name)"
}

private[rpc] object BaseRPCSender extends Logging {

  private val rpcSenderListenerBus =
    new AsynRPCMessageBus(BDP_RPC_SENDER_ASYN_QUEUE_CAPACITY.getValue, "RPC-Sender-Asyn-Thread")(
      BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_MAX.getValue,
      BDP_RPC_SENDER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX.getValue.toLong
    )

  rpcSenderListenerBus.addListener(new RPCMessageEventListener {

    override def onEvent(event: RPCMessageEvent): Unit =
      Sender.getSender(event.serviceInstance).send(event.message)

    override def onMessageEventError(event: RPCMessageEvent, t: Throwable): Unit =
      logger.warn(
        s"${event.serviceInstance} deliver RPC message failed! Message: " + event.message,
        t
      )

  })

  def addInstanceInfo[T](map: util.Map[String, T]): Unit = {
    map.put("name", DataWorkCloudApplication.getApplicationName.asInstanceOf[T])
    map.put("instance", DataWorkCloudApplication.getInstance.asInstanceOf[T])
  }

  def getInstanceInfo[T](map: util.Map[String, T]): ServiceInstance = {
    val name = map.get("name").toString
    val instance = map.get("instance").toString
    ServiceInstance(name, instance)
  }

}
