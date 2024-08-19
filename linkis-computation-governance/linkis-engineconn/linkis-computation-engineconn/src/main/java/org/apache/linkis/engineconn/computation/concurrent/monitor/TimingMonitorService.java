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

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration;
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor;
import org.apache.linkis.engineconn.core.EngineConnObject;
import org.apache.linkis.engineconn.core.executor.ExecutorManager$;
import org.apache.linkis.engineconn.executor.entity.Executor;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TimingMonitorService implements InitializingBean, Runnable {

  private static Logger LOG = LoggerFactory.getLogger(TimingMonitorService.class);

  private static CommonVars<TimeType> MONITOR_INTERVAL =
      CommonVars.apply("linkis.engineconn.concurrent.monitor.interval", new TimeType("30s"));

  @Autowired private List<MonitorService> monitorServiceList;

  private boolean isAvailable = true;

  private AccessibleExecutor concurrentExecutor = null;

  private static final Object EXECUTOR_STATUS_LOCKER = new Object();

  @Override
  public void afterPropertiesSet() throws Exception {
    if ((Boolean) (AccessibleExecutorConfiguration.ENGINECONN_SUPPORT_PARALLELISM().getValue())) {
      Utils.defaultScheduler()
          .scheduleAtFixedRate(
              this, 3 * 60 * 1000, MONITOR_INTERVAL.getValue().toLong(), TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public void run() {

    if (!EngineConnObject.isReady()) {
      return;
    }

    try {
      if (null == concurrentExecutor) {
        Executor executor = ExecutorManager$.MODULE$.getInstance().getReportExecutor();
        if (executor instanceof AccessibleExecutor) {
          concurrentExecutor = (AccessibleExecutor) executor;
        }
      }
      if (null == concurrentExecutor) {
        LOG.warn("Executor can not is null");
        return;
      }
      isAvailable = true;
      monitorServiceList.forEach(
          monitorService -> {
            if (!monitorService.isAvailable()) {
              isAvailable = false;
            }
          });
      if (isAvailable) {
        if (concurrentExecutor.isBusy())
          synchronized (EXECUTOR_STATUS_LOCKER) {
            LOG.info("monitor turn to executor status from busy to unlock");
            concurrentExecutor.transition(NodeStatus.Unlock);
          }
      } else {
        if (concurrentExecutor.isIdle())
          synchronized (EXECUTOR_STATUS_LOCKER) {
            LOG.info("monitor turn to executor status from unlock to busy");
            concurrentExecutor.transition(NodeStatus.Busy);
          }
      }
    } catch (Exception e) {
      LOG.warn("Failed to executor monitor ", e);
    }
  }
}
