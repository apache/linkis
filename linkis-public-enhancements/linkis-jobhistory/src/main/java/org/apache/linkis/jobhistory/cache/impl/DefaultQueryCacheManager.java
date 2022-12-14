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

package org.apache.linkis.jobhistory.cache.impl;

import org.apache.linkis.jobhistory.cache.QueryCacheManager;
import org.apache.linkis.jobhistory.conf.JobhistoryConfiguration;
import org.apache.linkis.jobhistory.dao.JobHistoryMapper;
import org.apache.linkis.jobhistory.entity.JobHistory;
import org.apache.linkis.jobhistory.util.QueryConfig;

import org.apache.commons.lang3.time.DateUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultQueryCacheManager implements QueryCacheManager, InitializingBean {

  private static Logger logger = LoggerFactory.getLogger(DefaultQueryCacheManager.class);

  @Autowired private SchedulerFactoryBean schedulerFactoryBean;

  @Autowired private JobHistoryMapper jobHistoryMapper;

  private Map<String, Cache<String, UserTaskResultCache>> engineUserCaches =
      Maps.newConcurrentMap();

  private Long undoneTaskMinId =
      Long.valueOf(String.valueOf(JobhistoryConfiguration.UNDONE_JOB_MINIMUM_ID().getValue()));

  @PostConstruct
  private void init() {
    Thread undoneTask =
        new Thread(
            () -> {
              try {
                refreshUndoneTask();
              } catch (Exception e) {
                logger.info("Failed to init refresh undone task", e);
              }
            });
    undoneTask.setName("refreshUndoneTask");
    undoneTask.start();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();
    SimpleScheduleBuilder cleanBuilder =
        SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInMinutes(
                (Integer) QueryConfig.CACHE_CLEANING_INTERVAL_MINUTE().getValue())
            .repeatForever();
    JobDetail cleanJobDetail =
        JobBuilder.newJob(ScheduledCleanJob.class)
            .withIdentity("ScheduledCleanJob")
            .storeDurably()
            .build();
    cleanJobDetail.getJobDataMap().put(QueryCacheManager.class.getName(), this);
    Trigger cleanTrigger =
        TriggerBuilder.newTrigger()
            .withIdentity("ScheduledCleanJob")
            .withSchedule(cleanBuilder)
            .build();
    scheduler.scheduleJob(cleanJobDetail, cleanTrigger);
    logger.info("Submitted cache cleaning job.");

    if ((Boolean) QueryConfig.CACHE_DAILY_EXPIRE_ENABLED().getValue()) {
      CronScheduleBuilder refreshBuilder = CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
      JobDetail refreshJobDetail =
          JobBuilder.newJob(ScheduledRefreshJob.class)
              .withIdentity("ScheduledRefreshJob")
              .storeDurably()
              .build();
      refreshJobDetail.getJobDataMap().put(QueryCacheManager.class.getName(), this);
      Trigger refreshTrigger =
          TriggerBuilder.newTrigger()
              .withIdentity("ScheduledRefreshJob")
              .withSchedule(refreshBuilder)
              .build();
      scheduler.scheduleJob(refreshJobDetail, refreshTrigger);
      logger.info("Submitted cache 00:00 refresh job.");
    }

    int hour = 0;
    int minute = 15;

    try {
      String refreshTime = JobhistoryConfiguration.UNDONE_JOB_REFRESH_TIME_DAILY().getValue();
      String[] parts = refreshTime.split(":");
      if (parts.length != 2) {
        logger.error(
            "Invalid UNDONE_JOB_REFRESH_TIME_DAILY value: {}. It should be the format of '00:15'. Will use the default value '00:15'",
            refreshTime);
      }
      hour = Integer.parseInt(parts[0]);
      minute = Integer.parseInt(parts[1]);
    } catch (Exception ignored) {
      logger.warn(
          "parse the config 'wds.linkis.jobhistory.undone.job.refreshtime.daily' failed. ",
          ignored);
    }

    CronScheduleBuilder refreshUndoneJobBuilder =
        CronScheduleBuilder.dailyAtHourAndMinute(hour, minute);
    JobDetail refreshUndoneJobDetail =
        JobBuilder.newJob(ScheduledRefreshUndoneJob.class)
            .withIdentity("ScheduledRefreshUndoneJob")
            .storeDurably()
            .build();
    refreshUndoneJobDetail.getJobDataMap().put(QueryCacheManager.class.getName(), this);
    Trigger refreshTrigger =
        TriggerBuilder.newTrigger()
            .withIdentity("ScheduledRefreshUndoneJob")
            .withSchedule(refreshUndoneJobBuilder)
            .build();
    scheduler.scheduleJob(refreshUndoneJobDetail, refreshTrigger);
    logger.info("Submitted cache 00:15 refresh undone job.");

    if (!scheduler.isShutdown()) {
      scheduler.start();
    }
  }

  @Override
  public UserTaskResultCache getCache(String user, String engineType) {
    Cache<String, UserTaskResultCache> userCaches = engineUserCaches.getOrDefault(engineType, null);
    if (userCaches == null) {
      userCaches = createUserCaches();
      Cache<String, UserTaskResultCache> oldCaches =
          engineUserCaches.putIfAbsent(engineType, userCaches);
      if (oldCaches != null) {
        userCaches = oldCaches;
      }
    }
    UserTaskResultCache userCache = userCaches.getIfPresent(user);
    if (userCache == null) {
      userCache = new UserTaskResultCache();
      userCaches.put(user, userCache);
    }
    return userCache;
  }

  @Override
  public void cleanAll() {
    foreach(UserTaskResultCache::clean);
  }

  @Override
  public void refreshAll() {
    foreach(UserTaskResultCache::refresh);
  }

  @Override
  public void refreshUndoneTask() {
    PageHelper.startPage(1, 10);
    List<JobHistory> queryTasks = null;
    try {

      Date eDate = new Date(System.currentTimeMillis());
      Date sDate = DateUtils.addDays(eDate, -1);
      queryTasks =
          jobHistoryMapper.searchWithIdOrderAsc(
              undoneTaskMinId,
              null,
              Arrays.asList("Running", "Inited", "Scheduled"),
              sDate,
              eDate,
              null);
    } finally {
      PageHelper.clearPage();
    }

    PageInfo<JobHistory> pageInfo = new PageInfo<>(queryTasks);
    List<JobHistory> list = pageInfo.getList();
    if (!list.isEmpty()) {
      undoneTaskMinId = list.get(0).getId();
      logger.info("Refreshing undone tasks, minimum id: {}", undoneTaskMinId);
    }
  }

  private void foreach(Consumer<UserTaskResultCache> consumer) {
    for (Cache<String, UserTaskResultCache> cacheContainer : engineUserCaches.values()) {
      for (UserTaskResultCache cache : cacheContainer.asMap().values()) {
        consumer.accept(cache);
      }
    }
  }

  private Cache<String, UserTaskResultCache> createUserCaches() {
    return CacheBuilder.newBuilder().build();
  }

  public Long getUndoneTaskMinId() {
    return undoneTaskMinId;
  }
}
