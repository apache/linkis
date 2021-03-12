package com.webank.wedatasphere.linkis.usercontrol.receiver

import com.webank.wedatasphere.linkis.protocol.usercontrol.{RequestLogin, RequestRegister}
import com.webank.wedatasphere.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}
import com.webank.wedatasphere.linkis.usercontrol.service.UserControlService
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by alexyang
  */
@Component
class UserControlReceiverChooser extends ReceiverChooser{

  @Autowired
  private var queryService: UserControlService = _
  private var receiver: Option[UserControlReceiver] = _

  @PostConstruct
  def init(): Unit = receiver = Some(new UserControlReceiver(queryService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: RequestLogin => receiver
    case _: RequestRegister => receiver
    case _ => None
  }

}
