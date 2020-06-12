package com.webank.wedatasphere.linkis.gateway.ujes.route.contextservice

import java.util
import java.util.{ArrayList, List}

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.cs.common.entity.source.{ContextIDParser, HAContextID}
import com.webank.wedatasphere.linkis.cs.common.utils.CSHighAvailableUtils
import com.webank.wedatasphere.linkis.rpc.instancealias.InstanceAliasManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * created by cooperyang on 2020/2/19
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
