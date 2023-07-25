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

package org.apache.linkis.engineconn.acessible.executor.service

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.hook.OperationHook
import org.apache.linkis.manager.common.operator.OperatorFactory
import org.apache.linkis.manager.common.protocol.engine.{
  EngineOperateRequest,
  EngineOperateResponse
}
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.stereotype.Service

import java.util

import scala.collection.JavaConverters.mapAsScalaMapConverter

@Service
class DefaultOperateService extends OperateService with Logging {

  @Receiver
  override def executeOperation(
      engineOperateRequest: EngineOperateRequest
  ): EngineOperateResponse = {
    var response: EngineOperateResponse = null
    val parameters = {
      val map = new util.HashMap[String, Object]()
      engineOperateRequest.getParameters.asScala.foreach(entry => map.put(entry._1, entry._2))
      map
    }
    val operator = Utils.tryCatch(OperatorFactory.apply().getOperatorRequest(parameters)) { t =>
      logger.error(s"Get operator failed, parameters is ${engineOperateRequest.getParameters}.", t)
      response = new EngineOperateResponse(
        new util.HashMap[String, Object](),
        true,
        ExceptionUtils.getRootCauseMessage(t)
      )
      doPostHook(engineOperateRequest, response)
      return response
    }
    logger.info(
      s"Try to execute operator ${operator.getClass.getSimpleName} with parameters ${engineOperateRequest.getParameters}."
    )
    val result = Utils.tryCatch(operator(parameters)) { t =>
      logger.error(s"Execute ${operator.getClass.getSimpleName} failed.", t)
      response = new EngineOperateResponse(
        new util.HashMap[String, Object](),
        true,
        ExceptionUtils.getRootCauseMessage(t)
      )
      doPostHook(engineOperateRequest, response)
      return response
    }
    logger.info(s"End to execute operator ${operator.getClass.getSimpleName}.")
    response = new EngineOperateResponse(result)
    doPostHook(engineOperateRequest, response)
    response
  }

  private def doPreHook(
      engineOperateRequest: EngineOperateRequest,
      engineOperateResponse: EngineOperateResponse
  ): Unit = {
    Utils.tryAndWarn {
      OperationHook
        .getOperationHooks()
        .foreach(hook => hook.doPreOperation(engineOperateRequest, engineOperateResponse))
    }
  }

  private def doPostHook(
      engineOperateRequest: EngineOperateRequest,
      engineOperateResponse: EngineOperateResponse
  ): Unit = {
    Utils.tryAndWarn {
      OperationHook
        .getOperationHooks()
        .foreach(hook => hook.doPostOperation(engineOperateRequest, engineOperateResponse))
    }
  }

}
