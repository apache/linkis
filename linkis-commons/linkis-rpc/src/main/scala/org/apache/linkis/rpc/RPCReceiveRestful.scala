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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.protocol.BroadcastProtocol
import org.apache.linkis.rpc.conf.RPCConfiguration.{
  BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX,
  BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX,
  BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY
}
import org.apache.linkis.rpc.errorcode.LinkisRpcErrorCodeSummary.TIMEOUT_PERIOD
import org.apache.linkis.rpc.exception.DWCURIException
import org.apache.linkis.rpc.transform.{RPCConsumer, RPCProduct}
import org.apache.linkis.server.{catchIt, Message}

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{
  RequestBody,
  RequestMapping,
  RequestMethod,
  RestController
}

import javax.annotation.PostConstruct

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.runtime.BoxedUnit

@RestController
private[rpc] class RPCReceiveRestful extends RPCReceiveRemote with Logging {

  @Autowired(required = false)
  private var receiverChoosers: Array[ReceiverChooser] = Array.empty

  @Autowired(required = false)
  private var receiverSenderBuilders: Array[ReceiverSenderBuilder] = Array.empty

  @Autowired(required = false)
  private var broadcastListeners: Array[BroadcastListener] = Array.empty

  private var rpcReceiverListenerBus: AsynRPCMessageBus = _

  private def getFirst[K, T](buildArray: Array[K], buildObj: K => Option[T]): Option[T] = {
    var obj: Option[T] = None
    for (builder <- buildArray if obj.isEmpty) obj = buildObj(builder)
    obj
  }

  private implicit def getReceiver(event: RPCMessageEvent): Option[Receiver] =
    getFirst[ReceiverChooser, Receiver](receiverChoosers, _.chooseReceiver(event))

  private implicit def getSender(event: RPCMessageEvent): Sender =
    getFirst[ReceiverSenderBuilder, Sender](receiverSenderBuilders, _.build(event)).get

  def registerReceiverChooser(receiverChooser: ReceiverChooser): Unit = {
    logger.info("register a new ReceiverChooser " + receiverChooser)
    receiverChoosers = receiverChooser +: receiverChoosers
  }

  def registerBroadcastListener(broadcastListener: BroadcastListener): Unit = {
    broadcastListeners = broadcastListener +: broadcastListeners
    addBroadcastListener(broadcastListener)
  }

  @PostConstruct
  def initListenerBus(): Unit = {
    if (!receiverChoosers.exists(_.isInstanceOf[CommonReceiverChooser])) {
      receiverChoosers = receiverChoosers :+ new CommonReceiverChooser
    }
    if (!receiverChoosers.exists(_.isInstanceOf[MessageReceiverChooser])) {
      receiverChoosers = receiverChoosers :+ new MessageReceiverChooser
    }
    logger.info("init all receiverChoosers in spring beans, list => " + receiverChoosers.toList)
    if (!receiverSenderBuilders.exists(_.isInstanceOf[CommonReceiverSenderBuilder])) {
      receiverSenderBuilders = receiverSenderBuilders :+ new CommonReceiverSenderBuilder
    }
    receiverSenderBuilders = receiverSenderBuilders.sortBy(_.order)
    logger.info(
      "init all receiverSenderBuilders in spring beans, list => " + receiverSenderBuilders.toList
    )
    val queueSize = BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY.acquireNew
    val threadSize = BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX.acquireNew
    rpcReceiverListenerBus = new AsynRPCMessageBus(queueSize, "RPC-Receiver-Asyn-Thread")(
      threadSize,
      BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX.getValue.toLong
    )
    logger.info(
      s"init RPCReceiverListenerBus with queueSize $queueSize and consumeThreadSize $threadSize."
    )
    rpcReceiverListenerBus.addListener(new RPCMessageEventListener {
      override def onEvent(event: RPCMessageEvent): Unit = event.message match {
        case _: BroadcastProtocol =>
        case _ =>
          event.fold(logger.warn(s"cannot find a receiver to deal $event."))(
            _.receive(event.message, event)
          )
      }
      override def onMessageEventError(event: RPCMessageEvent, t: Throwable): Unit =
        logger.warn(s"deal RPC message failed! Message: " + event.message, t)
    })
    broadcastListeners.foreach(addBroadcastListener)
    rpcReceiverListenerBus.start()
  }

  private def addBroadcastListener(broadcastListener: BroadcastListener): Unit =
    if (rpcReceiverListenerBus != null) {
      logger.info("add a new RPCBroadcastListener => " + broadcastListener.getClass)
      rpcReceiverListenerBus.addListener(new RPCMessageEventListener {
        val listenerName = broadcastListener.getClass.getSimpleName
        override def onEvent(event: RPCMessageEvent): Unit = event.message match {
          case broadcastProtocol: BroadcastProtocol =>
            broadcastListener.onBroadcastEvent(broadcastProtocol, event)
          case _ =>
        }
        override def onMessageEventError(event: RPCMessageEvent, t: Throwable): Unit =
          logger.warn(
            s"$listenerName consume broadcast message failed! Message: " + event.message,
            t
          )
      })
    }

  private def toMessage(obj: Any): Message = obj match {
    case Unit | () | null =>
      RPCProduct.getRPCProduct.ok()
    case _: BoxedUnit => RPCProduct.getRPCProduct.ok()
    case _ =>
      RPCProduct.getRPCProduct.toMessage(obj)
  }

  @RequestMapping(path = Array("/rpc/receive"), method = Array(RequestMethod.POST))
  override def receive(@RequestBody message: Message): Message = catchIt {
    val obj = RPCConsumer.getRPCConsumer.toObject(message)
    val event = RPCMessageEvent(obj, BaseRPCSender.getInstanceInfo(message.getData))
    rpcReceiverListenerBus.post(event)
    toMessage(Unit)
  }

  private def receiveAndReplyWithMessage(
      message: Message,
      opEvent: (Receiver, Any, Sender) => Any
  ): Message = catchIt {
    val obj = RPCConsumer.getRPCConsumer.toObject(message)
    val serviceInstance = BaseRPCSender.getInstanceInfo(message.getData)
    val event = RPCMessageEvent(obj, serviceInstance)
    event
      .map(receiver => {
        logger.debug("show the receiver {}", receiver.getClass)
        toMessage(opEvent(receiver, obj, event))
      })
      .getOrElse(RPCProduct.getRPCProduct.notFound())
  }

  @RequestMapping(path = Array("/rpc/receiveAndReply"), method = Array(RequestMethod.POST))
  override def receiveAndReply(@RequestBody message: Message): Message =
    receiveAndReplyWithMessage(message, _.receiveAndReply(_, _))

  @RequestMapping(path = Array("/rpc/replyInMills"), method = Array(RequestMethod.POST))
  override def receiveAndReplyInMills(@RequestBody message: Message): Message = catchIt {
    val duration = message.getData.get("duration")
    if (duration == null || StringUtils.isEmpty(duration.toString)) {
      throw new DWCURIException(TIMEOUT_PERIOD.getErrorCode, TIMEOUT_PERIOD.getErrorDesc)
    }
    val timeout = Duration(duration.toString.toLong, TimeUnit.MILLISECONDS)
    receiveAndReplyWithMessage(message, _.receiveAndReply(_, timeout, _))
  }

}
