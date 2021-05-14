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

package com.webank.wedatasphere.linkis.gateway.ujes.route.contextservice

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextIDParser
import com.webank.wedatasphere.linkis.cs.common.utils.CSHighAvailableUtils
import com.webank.wedatasphere.linkis.rpc.instancealias.InstanceAliasManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * Description: 如果id为HAID，则解析出对应的instance
 */
@Component
class ContextIdParserImpl extends ContextIDParser with Logging {

  @Autowired
  var instanceAliasManager : InstanceAliasManager = _

  override def parse(contextId: String): util.List[String] = {

    if (CSHighAvailableUtils.checkHAIDBasicFormat(contextId)) {
      val instances = new util.ArrayList[String](2)
      val haContextID = CSHighAvailableUtils.decodeHAID(contextId)
      if (instanceAliasManager.isInstanceAliasValid(haContextID.getInstance)) {
        instances.add(instanceAliasManager.getInstanceByAlias(haContextID.getInstance).getInstance)
      } else {
        error(s"parse HAID instance invalid. haIDKey : " + contextId)
      }
      if (instanceAliasManager.isInstanceAliasValid(haContextID.getBackupInstance)) {
        instances.add(instanceAliasManager.getInstanceByAlias(haContextID.getBackupInstance).getInstance)
      } else {
        error("parse HAID backupInstance invalid. haIDKey : " + contextId)
      }
      instances
    } else {
      new util.ArrayList[String](0)
    }
  }

  private def isNumberic(s:String):Boolean = {
    s.toCharArray foreach {
      c => if (c < 48 || c >57) return false
    }
    true
  }

}
