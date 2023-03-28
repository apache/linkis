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

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.constant.ServiceNameConsts;
import org.apache.linkis.entrance.scheduler.EntranceSchedulerContext;
import org.apache.linkis.entrance.utils.JobHistoryHelper;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.publicservice.common.lock.entity.CommonLock;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.queue.SchedulerEventState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import scala.Enumeration;
import scala.collection.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(ServiceNameConsts.ENTRANCE_FAILOVER_SERVER)
public class EntranceFailoverJobServer {

  private static final Logger logger = LoggerFactory.getLogger(EntranceFailoverJobServer.class);

  @Autowired private EntranceServer entranceServer;

  @Autowired private CommonLockService commonLockService;

  private static String ENTRANCE_FAILOVER_LOCK = "ENTRANCE_FAILOVER_LOCK";

  private ScheduledExecutorService scheduledExecutor;

  private Future future;

  @PostConstruct
  public void init() {
    if (EntranceConfiguration.ENTRANCE_FAILOVER_ENABLED()) {
      this.scheduledExecutor =
          Executors.newSingleThreadScheduledExecutor(
              Utils.threadFactory("Linkis-Failover-Scheduler-Thread-", true));
      failoverTask();
    }
  }

  @EventListener
  private void shutdownFailover(ContextClosedEvent event) {
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
    if (scheduledExecutor != null) {
      scheduledExecutor.shutdown();
      logger.info("Entrance Failover Server exit!");
    }
  }

  public void failoverTask() {
    future =
        scheduledExecutor.scheduleWithFixedDelay(
            () -> {
              EntranceSchedulerContext schedulerContext =
                  (EntranceSchedulerContext)
                      entranceServer
                          .getEntranceContext()
                          .getOrCreateScheduler()
                          .getSchedulerContext();

              // entrance do not failover job when it is offline
              if (schedulerContext.getOfflineFlag()) return;

              CommonLock commonLock = new CommonLock();
              commonLock.setLockObject(ENTRANCE_FAILOVER_LOCK);
              Boolean locked = false;
              try {
                locked = commonLockService.lock(commonLock, 30 * 1000L);
                if (!locked) return;
                logger.info("success locked {}", ENTRANCE_FAILOVER_LOCK);

                // get all entrance server from eureka
                ServiceInstance[] serviceInstances =
                    Sender.getInstances(Sender.getThisServiceInstance().getApplicationName());
                if (serviceInstances == null || serviceInstances.length <= 0) return;

                // serverInstance to map
                Map<String, Long> serverInstanceMap =
                    Arrays.stream(serviceInstances)
                        .collect(
                            Collectors.toMap(
                                ServiceInstance::getInstance,
                                ServiceInstance::getRegistryTimestamp,
                                (k1, k2) -> k2));

                // It is very important to avoid repeated execute job
                // when failover self job, if self instance is empty, the job can be repeated
                // execute
                if (!serverInstanceMap.containsKey(Sender.getThisInstance())) {
                  logger.warn(
                      "server has just started and has not get self info, it does not failover");
                  return;
                }

                // get failover job expired time (获取任务故障转移过期时间，配置为0表示不过期, 过期则不处理)
                long expiredTimestamp = 0L;
                if (EntranceConfiguration.ENTRANCE_FAILOVER_DATA_INTERVAL_TIME() > 0) {
                  expiredTimestamp =
                      System.currentTimeMillis()
                          - EntranceConfiguration.ENTRANCE_FAILOVER_DATA_INTERVAL_TIME();
                }

                List<JobRequest> jobRequests =
                    JobHistoryHelper.queryWaitForFailoverTask(
                        serverInstanceMap,
                        getUnCompleteStatus(),
                        expiredTimestamp,
                        EntranceConfiguration.ENTRANCE_FAILOVER_DATA_NUM_LIMIT());
                if (jobRequests.isEmpty()) return;
                List<Long> ids =
                    jobRequests.stream().map(JobRequest::getId).collect(Collectors.toList());
                logger.info("success query failover jobs , job size: {}, ids: {}", ids.size(), ids);

                // failover to local server
                for (JobRequest jobRequest : jobRequests) {
                  entranceServer.failoverExecute(jobRequest);
                }
                logger.info("finished execute failover jobs, job ids: {}", ids);

              } catch (Exception e) {
                logger.error("failover failed", e);
              } finally {
                if (locked) commonLockService.unlock(commonLock);
              }
            },
            EntranceConfiguration.ENTRANCE_FAILOVER_SCAN_INIT_TIME(),
            EntranceConfiguration.ENTRANCE_FAILOVER_SCAN_INTERVAL(),
            TimeUnit.MILLISECONDS);
  }

  private List<String> getUnCompleteStatus() {
    List<String> status = new ArrayList<>();
    Enumeration.ValueSet values = SchedulerEventState.values();
    Iterator<Enumeration.Value> iterator = values.iterator();
    while (iterator.hasNext()) {
      Enumeration.Value next = iterator.next();
      if (!SchedulerEventState.isCompleted(next)) status.add(next.toString());
    }
    return status;
  }
}
