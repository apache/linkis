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

package org.apache.linkis.datasourcemanager.core.receivers

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.datasourcemanager.common.domain.DataSource
import org.apache.linkis.datasourcemanager.common.protocol.{DsInfoQueryRequest, DsInfoResponse}
import org.apache.linkis.datasourcemanager.core.restful.RestfulApiHelper
import org.apache.linkis.datasourcemanager.core.service.{
  DataSourceInfoService,
  DataSourceRelateService
}
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util

import org.slf4j.LoggerFactory

@Component
class DsmReceiver {

  private val logger = LoggerFactory.getLogger(classOf[DsmReceiver])

  @Autowired
  private var dataSourceInfoService: DataSourceInfoService = _

  @Autowired
  private var dataSourceRelateService: DataSourceRelateService = _

  @Receiver
  def dealDsInfoQueryRequest(dsInfoQueryRequest: DsInfoQueryRequest): Any = {
    if (dsInfoQueryRequest.isValid) {
      Utils.tryCatch {
        var dataSource: DataSource = null
        if (Option(dsInfoQueryRequest.name).isDefined) {
          logger.info("Try to get dataSource by dataSourceName:" + dsInfoQueryRequest.name)
          dataSource = dataSourceInfoService.getDataSourceInfoForConnect(dsInfoQueryRequest.name)
        } else if (dsInfoQueryRequest.id.toLong > 0) {
          logger.info("Try to get dataSource by dataSourceId:" + dsInfoQueryRequest.id)
          dataSource =
            dataSourceInfoService.getDataSourceInfoForConnect(dsInfoQueryRequest.id.toLong)
        }
        if (null != dataSource) {
          RestfulApiHelper.decryptPasswordKey(
            dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId),
            dataSource.getConnectParams
          )
          DsInfoResponse(
            status = true,
            dataSource.getDataSourceType.getName,
            dataSource.getConnectParams,
            dataSource.getCreateUser
          )
        } else {
          logger.warn("Can not get any dataSource")
          DsInfoResponse(status = true, "", new util.HashMap[String, Object](), "")
        }
      } {
        case e: Exception =>
          logger.error(
            s"Fail to get data source information, id:${dsInfoQueryRequest.id} system:${dsInfoQueryRequest.system}",
            e
          )
          DsInfoResponse(status = false, "", new util.HashMap[String, Object](), "")
        case t: Throwable =>
          logger.error(
            s"Fail to get data source information, id:{dsInfoQueryRequest.id} system:${dsInfoQueryRequest.system}",
            t
          )
          DsInfoResponse(status = false, "", new util.HashMap[String, Object](), "")
      }
    } else {
      DsInfoResponse(status = true, "", new util.HashMap[String, Object](), "")
    }
  }

}
