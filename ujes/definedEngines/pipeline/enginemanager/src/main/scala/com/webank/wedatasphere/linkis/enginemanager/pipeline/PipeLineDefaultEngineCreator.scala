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

package com.webank.wedatasphere.linkis.enginemanager.pipeline

import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineCreator
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngineBuilder

/**
  * Created by johnnwang on 2018/11/15.
  */
class PipeLineDefaultEngineCreator extends AbstractEngineCreator{
  override protected def createProcessEngineBuilder(): ProcessEngineBuilder = new PipeLineProcessEngineBuilder
}
