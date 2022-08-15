/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.orchestrator.computation.catalyst.optimizer

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.code.plans.logical.{CacheTask, CodeLogicalUnitTask}
import org.apache.linkis.orchestrator.extensions.catalyst.OptimizerTransform
import org.apache.linkis.orchestrator.plans.logical.{LogicalContext, Task}

/**
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
