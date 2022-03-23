package org.apache.linkis.engineconn.computation.executor.upstream.service;


import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ECTaskEntranceMonitorServiceStartCondition implements Condition {
  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    boolean shouldStart = ComputationExecutorConf.UPSTREAM_MONITOR_ECTASK_SHOULD_START();
    return shouldStart;
  }
}
