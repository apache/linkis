package com.webank.wedatasphere.linkis.usercontrol.receiver

import com.webank.wedatasphere.linkis.protocol.usercontrol.{RequestLogin, RequestRegister}
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import com.webank.wedatasphere.linkis.usercontrol.service.UserControlService

import scala.concurrent.duration.Duration

/**
  * Created by alexyang
  */
class UserControlReceiver extends Receiver {

  private var userControlService: UserControlService = _

  def this(userControlService: UserControlService) = {
    this()
    this.userControlService = userControlService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case t: RequestLogin => userControlService.login(t)
    case t: RequestRegister => userControlService.register(t)
  }

  override def receiveAndReply(message:  Any, duration:  Duration, sender:  Sender): Any = {}
}
