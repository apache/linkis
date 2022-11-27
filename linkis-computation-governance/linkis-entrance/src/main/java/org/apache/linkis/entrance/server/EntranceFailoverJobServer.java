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

import org.apache.commons.compress.utils.Lists;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.constant.ServiceNameConsts;
import org.apache.linkis.entrance.scheduler.EntranceSchedulerContext;
import org.apache.linkis.entrance.utils.JobHistoryHelper;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.instance.label.client.InstanceLabelClient;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.constant.LabelConstant;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.route.RouteLabel;
import org.apache.linkis.publicservice.common.lock.entity.CommonLock;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.queue.SchedulerEventState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component(ServiceNameConsts.ENTRANCE_FAILOVER_SERVER)
public class EntranceFailoverJobServer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEntranceServer.class);

    @Autowired
    private EntranceServer entranceServer;

    @Autowired
    private CommonLockService commonLockService;


    private static String ENTRANCE_FAILOVER_LOCK = "ENTRANCE_FAILOVER_LOCK";

    @PostConstruct
    public void init() {
        failoverTask();
    }

  public void failoverTask() {
    if (EntranceConfiguration.ENTRANCE_FAILOVER_ENABLED()) {
      Utils.defaultScheduler()
          .scheduleWithFixedDelay(
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
                  locked = commonLockService.lock(commonLock, 10 * 1000L);
                  if (!locked) return;
                  logger.info("success locked {}", ENTRANCE_FAILOVER_LOCK);

                  // serverInstance to map
                  Map<String, Long> serverInstanceMap =
                      getActiveServerInstances().stream()
                          .collect(
                              Collectors.toMap(
                                  ServiceInstance::getInstance,
                                  ServiceInstance::getRegistryTimestamp,
                                  (k1, k2) -> k2));
                  if (serverInstanceMap.isEmpty()) return;

                  // get failover job expired time (获取任务故障转移过期时间，配置为0表示不过期, 过期则不处理)
                  long expiredTimestamp = 0L;
                  if (EntranceConfiguration.ENTRANCE_FAILOVER_DATA_INTERVAL_TIME() > 0) {
                    expiredTimestamp =
                        System.currentTimeMillis()
                            - EntranceConfiguration.ENTRANCE_FAILOVER_DATA_INTERVAL_TIME();
                  }

                  // get uncompleted status
                  List<String> statusList =
                      Arrays.stream(SchedulerEventState.uncompleteStatusArray())
                          .map(Object::toString)
                          .collect(Collectors.toList());

                  List<JobRequest> jobRequests =
                      JobHistoryHelper.queryWaitForFailoverTask(
                          serverInstanceMap,
                          statusList,
                          expiredTimestamp,
                          EntranceConfiguration.ENTRANCE_FAILOVER_DATA_NUM_LIMIT());
                  if (jobRequests.isEmpty()) return;
                  Object[] ids = jobRequests.stream().map(JobRequest::getId).toArray();
                  logger.info("success query failover jobs , job ids: {}", ids);

                  // failover to local server
                  jobRequests.forEach(jobRequest -> entranceServer.failoverExecute(jobRequest));
                  logger.info("success execute failover jobs, job ids: {}", ids);

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
  }

  private List<ServiceInstance> getActiveServerInstances() {
    // get all entrance server from eureka
    ServiceInstance[] serviceInstances =
        Sender.getInstances(Sender.getThisServiceInstance().getApplicationName());
    if (serviceInstances == null || serviceInstances.length <= 0) return Lists.newArrayList();

    // get all offline label server
    RouteLabel routeLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory()
            .createLabel(LabelKeyConstant.ROUTE_KEY, LabelConstant.OFFLINE);
    List<Label<?>> labels = Lists.newArrayList();
    labels.add(routeLabel);
    List<ServiceInstance> labelInstances =
        InstanceLabelClient.getInstance().getInstanceFromLabel(labels);
    if (labelInstances == null) labelInstances = Lists.newArrayList();

    // get active entrance server
    List<ServiceInstance> allInstances = Lists.newArrayList();
    allInstances.addAll(Arrays.asList(serviceInstances));
    allInstances.removeAll(labelInstances);

    return allInstances;
  }
}

}