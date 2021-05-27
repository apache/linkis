package com.webank.wedatasphere.linkis.manager.am.recycle

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.recycle.RecyclingRule

/**
  * @author peacewong
  * @date 2020/7/10 15:21
  */
trait RecyclingRuleExecutor {

  def ifAccept(recyclingRule: RecyclingRule): Boolean

  def executeRule(recyclingRule: RecyclingRule): Array[ServiceInstance]

}
