package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.optimizer

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.OptimizerTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.logical.{LogicalContext, LogicalOrchestration, Task}

/**
 *
 *
 * @description
 */
class PlaybackOptimizer extends OptimizerTransform with Logging  {

  override def apply(in: Task, context: LogicalContext): Task = ???

  override def getName: String = ???

}
