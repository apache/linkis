package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.optimizer

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.code.plans.logical.{CacheTask, CodeLogicalUnitTask}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.OptimizerTransform
import com.webank.wedatasphere.linkis.orchestrator.plans.logical.{LogicalContext, Task}

/**
 *
 *
 * @description
 */
class CacheTaskOptimizer extends OptimizerTransform with Logging {

  // TODO We should use CacheLabel to be judgment condition. If CacheLabel is not provided, we won't transform a CodeLogicalUnitTask to a CacheTask.
  override def apply(in: Task, context: LogicalContext): Task = in transform {
    case realTask: CodeLogicalUnitTask =>
      val cacheTask = new CacheTask(Array.empty, Array.empty)
      cacheTask.setRealTask(realTask)
      cacheTask.setTaskDesc(realTask.getTaskDesc)
      cacheTask
    case task: Task => task
  }

  override def getName: String = "CacheTaskOptimizer"

}
