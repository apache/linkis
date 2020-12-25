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

import com.webank.wedatasphere.linkis.enginemanager.{AbstractEngineManager, EngineManagerContext}
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.service.annotation.{EnableResourceManager, RegisterResource}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2018/9/27.
  */
@Component
@EnableResourceManager
class EngineManagerImpl extends AbstractEngineManager {

  @Autowired
  private var engineManagerContext: EngineManagerContext = _
  @Autowired
  private var resources: ModuleInfo = _

  override def getEngineManagerContext: EngineManagerContext = engineManagerContext

  /**
    * Registered resources(注册资源)
    */
  @RegisterResource
  override def registerResources(): ModuleInfo = resources
}
