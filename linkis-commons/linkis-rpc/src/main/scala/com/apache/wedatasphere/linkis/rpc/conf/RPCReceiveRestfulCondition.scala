package com.apache.wedatasphere.linkis.rpc.conf

import com.apache.wedatasphere.linkis.common.conf.CommonVars
import org.springframework.context.annotation.{Condition, ConditionContext}
import org.springframework.core.`type`.AnnotatedTypeMetadata

/**
  * @author casionxia
  * @Date 2021/9/26
  */
class RPCReceiveRestfulCondition extends Condition{
  val condition = CommonVars("wds.linkis.rpc.default.recevie.enable",false).getValue
  override def matches(conditionContext: ConditionContext, annotatedTypeMetadata: AnnotatedTypeMetadata): Boolean = {
    condition
  }
}