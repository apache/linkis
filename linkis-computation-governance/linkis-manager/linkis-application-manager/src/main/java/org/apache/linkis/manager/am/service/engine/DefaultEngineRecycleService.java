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

package org.apache.linkis.manager.am.service.engine;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.am.recycle.RecyclingRuleExecutor;
import org.apache.linkis.manager.common.entity.recycle.RecyclingRule;
import org.apache.linkis.manager.common.protocol.engine.EngineRecyclingRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineRecycleService extends AbstractEngineService
    implements EngineRecycleService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineRecycleService.class);

  @Autowired private List<RecyclingRuleExecutor> ruleExecutorList;

  @Autowired private EngineStopService engineStopService;

  @Receiver
  @Override
  public ServiceInstance[] recycleEngine(EngineRecyclingRequest engineRecyclingRequest) {
    if (null == ruleExecutorList) {
      logger.error("has not recycling rule");
      return null;
    }
    logger.info("start to recycle engine by " + engineRecyclingRequest.getUser());
    // 1. 规则解析
    List<RecyclingRule> ruleList = engineRecyclingRequest.getRecyclingRuleList();
    // 2. 返回一系列待回收Engine，
    Set<ServiceInstance> recyclingNodeSet =
        ruleList.stream()
            .flatMap(
                rule -> {
                  RecyclingRuleExecutor ruleExecutorOption =
                      ruleExecutorList.stream()
                          .filter(recyclingRuleExecutor -> recyclingRuleExecutor.ifAccept(rule))
                          .findFirst()
                          .orElse(null);
                  if (ruleExecutorOption != null) {
                    return Arrays.stream(ruleExecutorOption.executeRule(rule));
                  } else {
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    if (Objects.isNull(recyclingNodeSet)) {
      return null;
    }
    logger.info("The list of engines recycled this time is as follows:" + recyclingNodeSet);

    // 3. 调用EMService stopEngine
    recyclingNodeSet.forEach(
        serviceInstance -> {
          EngineStopRequest stopEngineRequest =
              new EngineStopRequest(serviceInstance, engineRecyclingRequest.getUser());
          engineStopService.asyncStopEngine(stopEngineRequest);
        });
    logger.info("Finished to recycle engine ,num " + recyclingNodeSet.size());
    return recyclingNodeSet.toArray(new ServiceInstance[0]);
  }
}
