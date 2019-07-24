/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.enginemanager.impl

import com.webank.wedatasphere.linkis.enginemanager._
import com.webank.wedatasphere.linkis.enginemanager.event.EngineManagerEventListenerBus
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2018/10/11.
  */
@Component
class EngineManagerContextImpl extends EngineManagerContext {
  @Autowired
  private var engineCreator: EngineCreator = _
  @Autowired
  private var engineResourceFactory: EngineResourceFactory = _
  @Autowired
  private var hooks: Array[EngineHook] = _
  @Autowired
  private var rmClient: ResourceManagerClient = _
  private var engineFactory: EngineFactoryImpl = _
  private var resourceRequester: ResourceRequesterImpl = _

  @PostConstruct
  def init(): Unit ={
    engineFactory = new EngineFactoryImpl(rmClient)
    engineFactory.init()
    resourceRequester = new ResourceRequesterImpl(rmClient)
  }

  override def getOrCreateEngineFactory: EngineFactory = engineFactory

  override def getOrCreateResourceRequester: ResourceRequester = resourceRequester

  override def getOrCreateEngineCreator: EngineCreator = engineCreator

  override def getOrCreateEngineResourceFactory: EngineResourceFactory = engineResourceFactory

  override def getOrCreateEngineHook: Array[EngineHook] = hooks

  override def getOrCreateEventListenerBus: EngineManagerEventListenerBus = null //TODO wait for completing
}