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
import com.webank.wedatasphere.linkis.common.utils.Utils
import org.apache.commons.lang.StringUtils

/**
  * Created by allenlliu on 2019/2/22.
  */
class SparkCommonProcessEngine(override val processBuilder: ProcessEngineBuilder,
                                override val dwcArgumentsParser: DWCArgumentsParser,
                                override val timeout: Long) extends CommonProcessEngine(processBuilder,dwcArgumentsParser,timeout)
{
  def this(processBuilder: ProcessEngineBuilder, dwcArgumentsParser: DWCArgumentsParser)=this(processBuilder, dwcArgumentsParser, -1)
  private var yarnAppId: String = _

  override def dealStartupLog(line: String): Unit = {
    println(getPort + ": " + line)
    if(StringUtils.isEmpty(yarnAppId)) {
      if (line.contains("application_")) {
        val appIdStr = line.split("\\s+").filter(x => x.startsWith("application_")).toList
        if (appIdStr.size > 0) {
          yarnAppId = appIdStr(0)
          info("Get yarn application id is "+ yarnAppId)
        }
      }
    }
  }

  override def shutdown(): Unit ={
   if(StringUtils.isNotEmpty(yarnAppId)){
     info(s"try to kill yarn app with id($yarnAppId).")
     Utils.tryQuietly(
     Utils.exec(Array(JavaProcessEngineBuilder.sudoUserScript.getValue, getUser, s"yarn  application -kill  $yarnAppId"), 3000l)
     )
   }
    super.shutdown()
  }

}
