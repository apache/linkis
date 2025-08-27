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

package org.apache.linkis.ecm.server.service.impl

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.server.service.OperateService
import org.apache.linkis.manager.common.operator.OperatorFactory
import org.apache.linkis.manager.common.protocol.em.{ECMOperateRequest, ECMOperateResponse}
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.stereotype.Service

import scala.collection.JavaConverters.mapAsScalaMapConverter

@Service
class DefaultOperateService extends OperateService with Logging {

  @Receiver
  override def executeOperation(ecmOperateRequest: ECMOperateRequest): ECMOperateResponse = {
    val parameters = ecmOperateRequest.parameters.asScala.toMap
    val operator = Utils.tryCatch(OperatorFactory().getOperatorRequest(parameters)) { t =>
      logger.error(s"Get operator failed, parameters is ${ecmOperateRequest.parameters}.", t)
      return ECMOperateResponse(Map.empty, true, ExceptionUtils.getRootCauseMessage(t))
    }
    logger.info(
      s"Try to execute operator ${operator.getClass.getSimpleName} with parameters ${ecmOperateRequest.parameters}."
    )
    val result = Utils.tryCatch(operator(parameters)) { t =>
      logger.error(s"Execute ${operator.getClass.getSimpleName} failed.", t)
      return ECMOperateResponse(Map.empty, true, ExceptionUtils.getRootCauseMessage(t))
    }
    ECMOperateResponse(result)
  }

}
