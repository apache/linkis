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
 

//package org.apache.linkis.jobhistory.receiver

/*import org.apache.linkis.jobhistory.cache.QueryCacheService
import org.apache.linkis.jobhistory.service.JobHistoryQueryService
import org.apache.linkis.protocol.query.QueryProtocol
import org.apache.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}

import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component*/

/**

@Component
class QueryReceiverChooser extends ReceiverChooser {

  @Autowired
  private var queryService: JobHistoryQueryService = _
  @Autowired
  private var queryCacheService: QueryCacheService = _
  private var receiver: Option[QueryReceiver] = _

  @PostConstruct
  def init(): Unit = receiver = Some(new QueryReceiver(queryService, queryCacheService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: QueryProtocol => receiver
    case _ => None
  }
}
*/
