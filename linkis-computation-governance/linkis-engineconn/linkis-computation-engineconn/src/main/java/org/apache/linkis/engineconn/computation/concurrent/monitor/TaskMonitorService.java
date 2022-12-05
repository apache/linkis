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

package org.apache.linkis.engineconn.computation.concurrent.monitor;

import org.apache.linkis.engineconn.computation.executor.execute.ConcurrentComputationExecutor;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineconn.core.executor.ExecutorManager$;
import org.apache.linkis.engineconn.executor.entity.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskMonitorService implements MonitorService {

  private static Logger LOG = LoggerFactory.getLogger(HardwareMonitorService.class);

  private static ConcurrentComputationExecutor concurrentExecutor = null;

  @Override
  public boolean isAvailable() {

    if (!EngineConnObject.isReady()) {
      return true;
    }

    try {
      if (null == concurrentExecutor) {
        Executor executor = ExecutorManager$.MODULE$.getInstance().getReportExecutor();
        if (executor instanceof ConcurrentComputationExecutor) {
          concurrentExecutor = (ConcurrentComputationExecutor) executor;
        }
      }
      if (null == concurrentExecutor) {
        LOG.warn("shell executor can not is null");
        return true;
      }
      if (concurrentExecutor.getRunningTask() > concurrentExecutor.getConcurrentLimit()) {
        LOG.info(
            "running task({}) > concurrent limit ({}) , now to mark ec to busy ",
            concurrentExecutor.getRunningTask(),
            concurrentExecutor.getConcurrentLimit());
        return false;
      }
    } catch (Exception e) {
      LOG.warn("Task Monitor failed", e);
    }
    return true;
  }
}
