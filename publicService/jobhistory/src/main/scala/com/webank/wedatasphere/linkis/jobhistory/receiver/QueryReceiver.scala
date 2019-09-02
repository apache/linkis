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

package com.webank.wedatasphere.linkis.jobhistory.receiver

import com.webank.wedatasphere.linkis.protocol.query.{RequestInsertTask, RequestQueryTask, RequestUpdateTask}
import com.webank.wedatasphere.linkis.jobhistory.service.QueryService
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration

/**
  * Created by johnnwang on 2018/10/16.
  */
class QueryReceiver extends Receiver{

  private var queryService: QueryService = _

  def this(queryService: QueryService) = {
    this()
    this.queryService = queryService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case t:RequestInsertTask =>queryService.add(message.asInstanceOf[RequestInsertTask])
    case t:RequestUpdateTask =>queryService.change(message.asInstanceOf[RequestUpdateTask])
    case t:RequestQueryTask =>queryService.query(message.asInstanceOf[RequestQueryTask])
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
