/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.metadatamanager.common.receiver

import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.metadatamanager.common.protocol._
import com.webank.wedatasphere.linkis.metadatamanager.common.service.MetadataService
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper

import scala.concurrent.duration.Duration

/**
 * Created by jackyxxie on 2020/2/10.
 */
class BaseMetaReceiver extends Receiver with Logging{
  protected var metadataService: MetadataService = _

  override def receive(message: Any, sender: Sender): Unit = ???

  override def receiveAndReply(message: Any, sender: Sender): Any = invoke(metadataService, message)

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = invoke(metadataService, message)


  def invoke(service: MetadataService, message: Any): Any = Utils.tryCatch{
    val data = message match {
      case MetaGetDatabases(params, operator) => service.getDatabases(operator, params)
      case MetaGetTableProps(params, database, table, operator) => service.getTableProps(operator, params, database, table)
      case MetaGetTables(params, database, operator) => service.getTables(operator, params, database)
      case MetaGetPartitions(params, database, table, operator) => service.getPartitions(operator, params, database, table)
      case MetaGetColumns(params, database, table, operator) => service.getColumns(operator, params, database, table)
      case MetadataConnect(operator, params, version) =>
        service.getConnection(operator, params)
        //MetadataConnection is not scala class
        null
      case _ => new Object()
    }
    MetadataResponse(status = true, BDPJettyServerHelper.gson.toJson(data))
  }{
    case e:WarnException => val errorMsg = e.getMessage
      info(s"Fail to invoke meta service: [$message],[$errorMsg]")
      MetadataResponse(status = false, errorMsg)
    case t:Throwable =>
      val errorMsg = t.getMessage
      if (message.isInstanceOf[MetadataConnect])
        info(s"Fail to invoke meta service: [$message], [$errorMsg]")
      else error(s"Fail to invoke meta service", t)
      MetadataResponse(status = false, t.getMessage)
  }
}
