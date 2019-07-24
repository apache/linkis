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

package com.webank.wedatasphere.linkis.engine.execute.scheduler

import com.webank.wedatasphere.linkis.engine.annotation.EngineExecutorManagerBeanAnnotation
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration.ENGINE_SUPPORT_PARALLELISM
import com.webank.wedatasphere.linkis.engine.execute._
import org.springframework.beans.factory.annotation.Autowired

/**
  * Created by enjoyyin on 2018/10/16.
  */
@EngineExecutorManagerBeanAnnotation
class EngineExecutorManagerImpl extends EngineExecutorManager {
  @Autowired
  private var codeParser: CodeParser = _
  @Autowired
  private var engineHooks: Array[EngineHook] = _
  @Autowired
  private var engineExecutorFactory: EngineExecutorFactory = _

  override protected def getOrCreateCodeParser(): CodeParser = codeParser

  override protected def getOrCreateEngineExecutorFactory(): EngineExecutorFactory = engineExecutorFactory

  override protected def getEngineHooks: Array[EngineHook] = engineHooks

  override protected val isSupportParallelism: Boolean = ENGINE_SUPPORT_PARALLELISM.getValue
}
