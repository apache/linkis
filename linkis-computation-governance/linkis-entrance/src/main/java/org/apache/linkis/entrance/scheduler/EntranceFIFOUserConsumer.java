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

package org.apache.linkis.entrance.scheduler;

import org.apache.linkis.scheduler.SchedulerContext;
import org.apache.linkis.scheduler.queue.Consumer;
import org.apache.linkis.scheduler.queue.Group;
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOUserConsumer;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntranceFIFOUserConsumer extends FIFOUserConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EntranceFIFOUserConsumer.class);

  public EntranceFIFOUserConsumer(
      SchedulerContext schedulerContext, ExecutorService executeService, Group group) {
    super(schedulerContext, executeService, group);
  }

  @Override
  public boolean runScheduleIntercept() {
    Consumer[] consumers = getSchedulerContext().getOrCreateConsumerManager().listConsumers();
    int creatorRunningJobNum = 0;

    // APP_TEST_hadoop_hive or IDE_hadoop_hive
    String groupNameStr = getGroup().getGroupName();
    String[] groupNames = groupNameStr.split("_");
    int length = groupNames.length;
    if (length < 3) {
      return true;
    }

    // APP_TEST
    int lastIndex = groupNameStr.lastIndexOf("_");
    int secondLastIndex = groupNameStr.lastIndexOf("_", lastIndex - 1);
    String creatorName = groupNameStr.substring(0, secondLastIndex);

    // hive
    String ecType = groupNames[length - 1];

    for (Consumer consumer : consumers) {
      String groupName = consumer.getGroup().getGroupName();
      if (groupName.startsWith(creatorName) && groupName.endsWith(ecType)) {
        creatorRunningJobNum += consumer.getRunningEvents().length;
      }
    }

    int creatorECTypeMaxRunningJobs =
        CreatorECTypeDefaultConf.getCreatorECTypeMaxRunningJobs(creatorName, ecType);

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Creator: {} EC: {} there are currently:{} jobs running and maximum limit: {}",
          creatorName,
          ecType,
          creatorRunningJobNum,
          creatorECTypeMaxRunningJobs);
    }

    if (creatorRunningJobNum > creatorECTypeMaxRunningJobs) {
      logger.error(
          "Creator: {} EC: {} there are currently:{} jobs running that exceed the maximum limit: {}",
          creatorName,
          ecType,
          creatorRunningJobNum,
          creatorECTypeMaxRunningJobs);
      return false;
    } else {
      return true;
    }
  }
}
