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
 
package org.apache.linkis.datasourcemanager.core.receivers

import java.util

@Component
class DsmReceiver extends Receiver with Logging{

  @Autowired
  private var dataSourceInfoService: DataSourceInfoService = _

  @Autowired
  private var dataSourceRelateService: DataSourceRelateService = _

  override def receive(message: Any, sender: Sender): Unit = ???

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case DsInfoQueryRequest(id, system) =>
      if ( id.toLong > 0 &&  Some(system).isDefined ) {
        Utils.tryCatch {
          val dataSource: DataSource = dataSourceInfoService.getDataSourceInfo(id.toLong, system)
          if ( null != dataSource ) {
            RestfulApiHelper.decryptPasswordKey(dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId),
              dataSource.getConnectParams)
            DsInfoResponse(status = true, dataSource.getDataSourceType.getName,
              dataSource.getConnectParams, dataSource.getCreateUser)
          }else DsInfoResponse(status = true, "", new util.HashMap[String, Object](), "")
        }{
          case e: Exception => logger.error(s"Fail to get data source information, id:$id system:$system", e)
            DsInfoResponse(status = false, "", new util.HashMap[String, Object](), "")
          case t: Throwable => logger.error(s"Fail to get data source information, id:$id system:$system", t)
            DsInfoResponse(status = false, "", new util.HashMap[String, Object](), "")
        }
      } else {
        DsInfoResponse(status = true, "", new util.HashMap[String, Object](), "")
      }
    case _ => new Object()
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = message match {
    case DsInfoQueryRequest(id, system) =>
      if ( id.toLong > 0 &&  Some(system).isDefined ) {
        Utils.tryCatch {
          val dataSource: DataSource = dataSourceInfoService.getDataSourceInfo(id.toLong, system)
          if ( null != dataSource ) {
            RestfulApiHelper.decryptPasswordKey(dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId),
              dataSource.getConnectParams)
            DsInfoResponse(status = true, dataSource.getDataSourceType.getName,
              dataSource.getConnectParams, dataSource.getCreateUser)
          }else DsInfoResponse(status = true, "", new util.HashMap[String, Object](), "")
        }{
          case e: Exception => logger.error(s"Fail to get data source information, id:$id system:$system", e)
            DsInfoResponse(status = false, "", new util.HashMap[String, Object](), "")
          case t: Throwable => logger.error(s"Fail to get data source information, id:$id system:$system", t)
            DsInfoResponse(status = false, "", new util.HashMap[String, Object](), "")
        }
      } else {
        DsInfoResponse(status = true, "", new util.HashMap[String, Object](), "")
      }
    case _ => new Object()
  }
}
