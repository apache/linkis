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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.ecm.core.listener.{ECMEvent, ECMEventListener}
import org.apache.linkis.ecm.server.listener.EngineConnStopEvent
import org.apache.linkis.ecm.server.service.EngineConnKillService
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

class ECMListenerService extends ECMEventListener with Logging {

  private var engineConnKillService: EngineConnKillService = _

  override def onEvent(event: ECMEvent): Unit = event match {
    case EngineConnStopEvent(engineConn, engineStopRequest) =>
      if (NodeStatus.Failed == engineConn.getStatus) {
        logger.info("deal stopEvent to kill ec {}", engineStopRequest)
        engineConnKillService.dealEngineConnStop(engineStopRequest)
      } else {
        if (engineConnKillService.isInstanceOf[DefaultEngineConnKillService]) {
          logger.info("deal stopEvent to kill yarn app {}", engineStopRequest)
          engineConnKillService
            .asInstanceOf[DefaultEngineConnKillService]
            .killYarnAppIdOfOneEc(engineStopRequest)
        }
      }
    case _ =>
  }

  def getEngineConnKillService(): EngineConnKillService = {
    engineConnKillService
  }

  def setEngineConnKillService(engineConnKillService: EngineConnKillService): Unit = {
    this.engineConnKillService = engineConnKillService
  }

}
