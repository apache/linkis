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

package org.apache.linkis.entrance.server;

import org.apache.linkis.entrance.EntranceContext;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.entrance.constant.ServiceNameConsts;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.log.LogReader;
import org.apache.linkis.rpc.Sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Description: */
@Component(ServiceNameConsts.ENTRANCE_SERVER)
public class DefaultEntranceServer extends EntranceServer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultEntranceServer.class);

  @Autowired private EntranceContext entranceContext;

  private Boolean shutdownFlag = false;

  public DefaultEntranceServer() {}

  public DefaultEntranceServer(EntranceContext entranceContext) {
    this.entranceContext = entranceContext;
  }

  @Override
  @PostConstruct
  public void init() {
    getEntranceWebSocketService();
    addRunningJobEngineStatusMonitor();
  }

  @Override
  public String getName() {
    return Sender.getThisInstance();
  }

  @Override
  public EntranceContext getEntranceContext() {
    return entranceContext;
  }

  @Override
  public LogReader logReader(String execId) {
    return getEntranceContext().getOrCreateLogManager().getLogReader(execId);
  }

  private void addRunningJobEngineStatusMonitor() {}

  @EventListener
  private void shutdownEntrance(ContextClosedEvent event) {
    if (shutdownFlag) {
      logger.warn("event has been handled");
    } else {
      logger.warn("Entrance exit to stop all job");
      EntranceJob[] allUndoneJobs = getAllUndoneTask(null);
      if (null != allUndoneJobs) {
        for (EntranceJob job : allUndoneJobs) {
          job.onFailure(
              "Entrance exits the automatic cleanup task and can be rerun(服务退出自动清理任务，可以重跑)", null);
        }
      }
    }
  }
}
