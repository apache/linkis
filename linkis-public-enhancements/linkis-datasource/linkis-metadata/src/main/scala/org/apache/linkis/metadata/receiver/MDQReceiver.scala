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

package org.apache.linkis.metadata.receiver

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.metadata.ddl.DDLHelper
import org.apache.linkis.metadata.domain.mdq.bo.MdqTableBO
import org.apache.linkis.metadata.service.MdqService
import org.apache.linkis.metadata.utils.MdqUtils
import org.apache.linkis.protocol.mdq.{
  DDLCompleteResponse,
  DDLExecuteResponse,
  DDLRequest,
  DDLResponse
}
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.fasterxml.jackson.databind.ObjectMapper

@Component
class MDQReceiver extends Logging {

  @Autowired
  private var mdqService: MdqService = _

  private val mapper = new ObjectMapper

  @Receiver
  def dealDDLRequest(ddlRequest: DDLRequest): DDLResponse = {
    logger.info("received a request from sparkEngine")
    val ddlCode = DDLHelper.createDDL(ddlRequest.params)
    DDLResponse(ddlCode)
  }

  @Receiver
  def dealDDLExecuteResponse(ddlExecuteResponse: DDLExecuteResponse): DDLCompleteResponse = {

    if (!ddlExecuteResponse.status) {
      logger.warn(s"${MdqUtils.ruleString(ddlExecuteResponse.code)} execute failed")
      DDLCompleteResponse(false)
    } else {
      Utils.tryCatch {
        val jsonNode = mapper.readTree(ddlExecuteResponse.code)
        val mdqTableBO = mapper.treeToValue(jsonNode, classOf[MdqTableBO])
        val tableName = mdqTableBO.getTableBaseInfo.getBase.getName
        val dbName = mdqTableBO.getTableBaseInfo.getBase.getDatabase
        logger.info(s"begin to persist table $dbName $tableName")
        mdqService.persistTable(mdqTableBO, ddlExecuteResponse.user)
        logger.info(s"end to persist table $dbName $tableName")
        DDLCompleteResponse(true)
      } {
        case e: Exception =>
          logger.error(s"fail to persist table for user ${ddlExecuteResponse.user}", e)
          DDLCompleteResponse(false)
        case t: Throwable =>
          logger.error(s"fail to persist table for user ${ddlExecuteResponse.user}", t)
          DDLCompleteResponse(false)
      }
    }
  }

}
