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

package com.webank.wedatasphere.linkis.protocol.utils

import com.webank.wedatasphere.linkis.common.conf.CommonVars


object ProtocolUtils {

  val SERVICE_SUFFIX = CommonVars("wds.linkis.service.suffix","engineManager,entrance,engine")
  val suffixs = SERVICE_SUFFIX.getValue.split(",")

  /**
    * Pass in moduleName to return the corresponding appName
    * 传入moduleName返回对应的appName
    * @param moduleName
    * @return
    */
  def getAppName(moduleName:String):Option[String] = {
    val moduleNameLower = moduleName.toLowerCase()
    for(suffix <- suffixs ){
      if(moduleNameLower.contains(suffix.toLowerCase())) return Some(moduleNameLower.replace(suffix.toLowerCase(),""))
    }
    None
  }

}
