/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.ecm.server.service

import java.util

import com.webank.wedatasphere.linkis.ecm.core.engineconn.EngineConn
import com.webank.wedatasphere.linkis.ecm.core.launch.EngineConnLaunchRunner
import com.webank.wedatasphere.linkis.manager.common.entity.resource.Resource


/**
 * The enginelistservice interface manages the interface started by the engine
 * The most important submit method is to put the thread that starts the engine into the thread pool to start
 * EngineListService接口管理引擎启动的接口
 * 最重要的submit方法是将启动引擎的线程放入到线程池中进行启动
 */
trait EngineConnListService {

  def init(): Unit

  def getEngineConn(engineConnId: String): Option[EngineConn]

  def getEngineConns: util.List[EngineConn]

  def addEngineConn(engineConn: EngineConn): Unit

  def killEngineConn(engineConnId: String): Unit

  def getUsedResources: Resource

  def submit(runner: EngineConnLaunchRunner): Option[EngineConn]

}
