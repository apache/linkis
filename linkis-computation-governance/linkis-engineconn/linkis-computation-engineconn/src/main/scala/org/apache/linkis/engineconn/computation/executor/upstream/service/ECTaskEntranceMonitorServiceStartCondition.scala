package org.apache.linkis.engineconn.computation.executor.upstream.service;


import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.`type`.AnnotatedTypeMetadata;

class ECTaskEntranceMonitorServiceStartCondition extends Condition {
  override def matches(conditionContext: ConditionContext, annotatedTypeMetadata: AnnotatedTypeMetadata): Boolean = ComputationExecutorConf.UPSTREAM_MONITOR_ECTASK_SHOULD_START
}
