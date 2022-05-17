/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.rpc

import java.util


trait ReceiverChooser {

  def getReceivers: util.Map[String, Receiver] = RPCSpringBeanCache.getReceivers

  def chooseReceiver(event: RPCMessageEvent): Option[Receiver]

}

class CommonReceiverChooser extends ReceiverChooser {
  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = if(getReceivers.size() == 1) Some(getReceivers.values.iterator.next)
  else {
    var receiver = getReceivers.get(event.serviceInstance.getApplicationName)
    if(receiver == null) receiver = getReceivers.get("receiver")
    Option(receiver)
  }
}