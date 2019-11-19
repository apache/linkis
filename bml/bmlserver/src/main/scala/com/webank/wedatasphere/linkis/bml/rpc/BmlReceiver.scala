package com.webank.wedatasphere.linkis.bml.rpc

import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}

import scala.concurrent.duration.Duration

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
class BmlReceiver extends Receiver{


  override def receive(message: Any, sender: Sender): Unit = ???

  override def receiveAndReply(message: Any, sender: Sender): Any = ???

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = ???


}
