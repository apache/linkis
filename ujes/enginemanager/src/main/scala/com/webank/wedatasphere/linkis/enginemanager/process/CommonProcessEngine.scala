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

/**
  * Created by johnnwang on 2018/10/11.
  */
class CommonProcessEngine(override val processBuilder: ProcessEngineBuilder,
                          override val dwcArgumentsParser: DWCArgumentsParser,
                          override val timeout: Long) extends ProcessEngine {

  def this(processBuilder: ProcessEngineBuilder, dwcArgumentsParser: DWCArgumentsParser) = this(processBuilder, dwcArgumentsParser, -1)

  private var user: String = _

  def setUser(user: String) = this.user = user
  /**
    * creator(创建者)
    *
    * @return
    */
  override val getCreator: String = dwcArgumentsParser.getDWCConfMap("creator")

  /**
    * Actually launched user(实际启动的用户)
    *
    * @return
    */
  override def getUser: String = user
}
