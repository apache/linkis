/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.rpc

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf._
import com.webank.wedatasphere.linkis.rpc.exception.DWCURIException
import com.webank.wedatasphere.linkis.rpc.transform.{RPCConsumer, RPCProduct}
import com.webank.wedatasphere.linkis.server.{Message, catchIt}
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.MediaType
import javax.ws.rs.{Consumes, POST, Path, Produces}
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Import, Primary}
import org.springframework.stereotype.Component
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}

import scala.concurrent.duration.Duration
import scala.runtime.BoxedUnit


@Component
@Path("/rpc")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
@Primary
@Import(Array(classOf[MessageRPCConsumer]))
class MessageRPCReceiveRestful extends RPCReceiveRestful {

  @Autowired(required = false)
  private var receiverChoosers: Array[ReceiverChooser] = Array.empty
  @Autowired(required = false)
  private var receiverSenderBuilders: Array[ReceiverSenderBuilder] = Array.empty
  @Autowired
  private var messageRPCConsumer: MessageRPCConsumer = _

  private def getFirst[K, T](buildArray: Array[K], buildObj: K => Option[T]): Option[T] = {
    var obj: Option[T] = None
    for (builder <- buildArray if obj.isEmpty) obj = buildObj(builder)
    obj
  }

  //广播功能去掉，messageScheduler可以提供这种功能，目前只有entrance有此类方法，后续调整

  private implicit def getReceiver(event: RPCMessageEvent): Option[Receiver] = getFirst[ReceiverChooser, Receiver](receiverChoosers, _.chooseReceiver(event))

  private implicit def getSender(event: RPCMessageEvent): Sender = getFirst[ReceiverSenderBuilder, Sender](receiverSenderBuilders, _.build(event)).get

  private implicit def getMessageRPCConsumer(rpcConsumer: RPCConsumer): MessageRPCConsumer = messageRPCConsumer

  override def registerReceiverChooser(receiverChooser: ReceiverChooser): Unit = {
    info("register a new ReceiverChooser " + receiverChooser)
    receiverChoosers = receiverChooser +: receiverChoosers
  }

  @PostConstruct
  def init(): Unit = {
    if (!receiverChoosers.exists(_.isInstanceOf[CommonReceiverChooser]))
      receiverChoosers = receiverChoosers :+ new CommonReceiverChooser
    info("init all receiverChoosers in spring beans, list => " + receiverChoosers.toList)
    if (!receiverSenderBuilders.exists(_.isInstanceOf[CommonReceiverSenderBuilder]))
      receiverSenderBuilders = receiverSenderBuilders :+ new CommonReceiverSenderBuilder
    receiverSenderBuilders = receiverSenderBuilders.sortBy(_.order)
    info("init all receiverSenderBuilders in spring beans, list => " + receiverSenderBuilders.toList)
  }

  private implicit def toMessage(obj: Any): Message = obj match {
    case Unit | () =>
      RPCProduct.getRPCProduct.ok()
    case _: BoxedUnit => RPCProduct.getRPCProduct.ok()
    case _ =>
      RPCProduct.getRPCProduct.toMessage(obj)
  }

  private implicit def getReq: HttpServletRequest = if (null != RequestContextHolder.getRequestAttributes ) {
    RequestContextHolder.getRequestAttributes.asInstanceOf[ServletRequestAttributes].getRequest
  }else {
    null
  }

  @Path("receive")
  @POST
  override def receive(message: Message): Message = invokeReceiver(message, _.receive(_, _))

  private def invokeReceiver(message: Message, opEvent: (Receiver, Any, Sender) => Message)(implicit req: HttpServletRequest): Message = catchIt {
    message.getData.put(REQUEST_KEY, req)
    val obj = RPCConsumer.getRPCConsumer.overrideToObject(message)
    val serviceInstance = BaseRPCSender.getInstanceInfo(message.getData)
    val event = RPCMessageEvent(obj, serviceInstance)
    event.map(opEvent(_, obj, event)).getOrElse(RPCProduct.getRPCProduct.notFound())
  }

  @Path("receiveAndReply")
  @POST
  override def receiveAndReply(message: Message): Message = invokeReceiver(message, _.receiveAndReply(_, _))

  @Path("replyInMills")
  @POST
  override def receiveAndReplyInMills(message: Message): Message = catchIt {
    val duration = message.getData.get("duration")
    if (duration == null || StringUtils.isEmpty(duration.toString)) throw new DWCURIException(10002, "The timeout period is not set!(超时时间未设置！)")
    val timeout = Duration(duration.toString.toLong, TimeUnit.MILLISECONDS)
    invokeReceiver(message, _.receiveAndReply(_, timeout, _))
  }

}
