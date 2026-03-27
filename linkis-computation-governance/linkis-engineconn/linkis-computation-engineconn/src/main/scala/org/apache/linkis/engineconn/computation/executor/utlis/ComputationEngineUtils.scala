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

package org.apache.linkis.engineconn.computation.executor.utlis

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.governance.common.exception.engineconn.{
  EngineConnExecutorErrorCode,
  EngineConnExecutorErrorException
}
import org.apache.linkis.protocol.message.RequestProtocol
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.BDPJettyServerHelper

import com.google.gson.{Gson, GsonBuilder}

object ComputationEngineUtils extends Logging {

  def GSON: Gson = BDPJettyServerHelper.gson

  def GSON_PRETTY: Gson = new GsonBuilder().setPrettyPrinting().create()

  private val WORK_DIR_STR = "user.dir"
  def getCurrentWorkDir: String = System.getProperty(WORK_DIR_STR)

  def sendToEntrance(task: EngineConnTask, msg: RequestProtocol): Unit = {
    Utils.tryCatch {
      var sender: Sender = null
      if (null != task && null != task.getCallbackServiceInstance() && null != msg) {
        sender = Sender.getSender(task.getCallbackServiceInstance())
        sender.send(msg)
      } else {
        // todo
        logger.debug("SendtoEntrance error, cannot find entrance instance.")
      }
    } { t =>
      val errorMsg = s"SendToEntrance error. $msg" + t.getCause
      logger.error(errorMsg, t)
      throw new EngineConnExecutorErrorException(
        EngineConnExecutorErrorCode.SEND_TO_ENTRANCE_ERROR,
        errorMsg
      )
    }
  }

}
