/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engine.execute

import com.webank.wedatasphere.linkis.rpc.Sender


/**
  * Created by enjoyyin on 2018/9/19.
  */
trait SenderContainer {

  private var senders: Array[Sender] = Array.empty

  def setSenders(senders: Array[Sender]): Unit = this.senders = senders
  def addSender(sender: Sender): Unit = if(!senders.contains(sender)) senders = senders :+ sender
  def getSenders: Array[Sender] = senders

}

trait SyncSenderContainer extends SenderContainer {
  override def setSenders(senders: Array[Sender]): Unit = {
    super.setSenders(senders)
    if(senders != null) this synchronized notifyAll()
  }

  override def addSender(sender: Sender): Unit = {
    super.addSender(sender)
    this synchronized notifyAll()
  }

  override def getSenders: Array[Sender] = {
    if(super.getSenders == null || super.getSenders.isEmpty) this synchronized {
      while(super.getSenders == null || super.getSenders.isEmpty)
        wait(50)
    }
    super.getSenders
  }
}