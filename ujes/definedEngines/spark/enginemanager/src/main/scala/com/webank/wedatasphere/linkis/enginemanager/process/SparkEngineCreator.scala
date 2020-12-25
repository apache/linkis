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

package com.webank.wedatasphere.linkis.enginemanager.process

import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineCreator
import com.webank.wedatasphere.linkis.enginemanager.impl.UserTimeoutEngineResource
import org.springframework.stereotype.Component

/**
  * Created by allenlliu on 2019/4/8.
  */
@Component("engineCreator")
class SparkEngineCreator extends AbstractEngineCreator with Logging {
  override protected def createProcessEngineBuilder(): SparkSubmitProcessBuilder = {
    val SparkSubmitProcessBuilder = new SparkSubmitProcessBuilder()
    SparkSubmitProcessBuilder
  }

  override def createEngine(processEngineBuilder:ProcessEngineBuilder,parser:DWCArgumentsParser):ProcessEngine={
    processEngineBuilder.getEngineResource match {
      case timeout: UserTimeoutEngineResource =>
        new SparkCommonProcessEngine(processEngineBuilder, parser, timeout.getTimeout)
      case _ =>
        new SparkCommonProcessEngine(processEngineBuilder, parser)
    }
  }
}
