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

package org.apache.linkis.bml.common;

import org.apache.linkis.bml.conf.BmlServerConfiguration;
import org.apache.linkis.bml.service.ResourceService;
import org.apache.linkis.bml.service.VersionService;
import org.apache.linkis.common.utils.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ScheduledTask {

  @Autowired private ResourceService resourceService;

  @Autowired private VersionService versionService;

  private Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

  private class CleanExpiredThread implements Runnable {
    @Override
    public void run() {
      //            resourceService.cleanExpiredResources();
      //            versionService.cleanExpiredVersions();
    }
  }

  @PostConstruct
  public void init() {
    logger.info("Schedule Task is init");
    CleanExpiredThread cleanExpiredThread = new CleanExpiredThread();
    Utils.defaultScheduler()
        .scheduleAtFixedRate(
            cleanExpiredThread,
            10,
            ((Number) BmlServerConfiguration.BML_CLEAN_EXPIRED_TIME().getValue()).intValue(),
            TimeUnit.SECONDS);
  }
}
