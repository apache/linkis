/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.jobhistory.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.linkis.jobhistory.cache.QueryCacheManager;
import org.apache.linkis.jobhistory.util.QueryConfig;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class DefaultQueryCacheManager implements QueryCacheManager, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(DefaultQueryCacheManager.class);

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    private Map<String, Cache<String, UserTaskResultCache>> engineUserCaches = Maps.newConcurrentMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        SimpleScheduleBuilder cleanBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes((Integer) QueryConfig.CACHE_CLEANING_INTERVAL_MINUTE().getValue())
                .repeatForever();
        JobDetail cleanJobDetail = JobBuilder.newJob(ScheduledCleanJob.class).withIdentity("ScheduledCleanJob").storeDurably().build();
        cleanJobDetail.getJobDataMap().put(QueryCacheManager.class.getName(), this);
        Trigger cleanTrigger = TriggerBuilder.newTrigger().withIdentity("ScheduledCleanJob").withSchedule(cleanBuilder).build();
        scheduler.scheduleJob(cleanJobDetail, cleanTrigger);
        logger.info("Submitted cache cleaning job.");

        if ((Boolean) QueryConfig.CACHE_DAILY_EXPIRE_ENABLED().getValue()) {
            CronScheduleBuilder refreshBuilder = CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
            JobDetail refreshJobDetail = JobBuilder.newJob(ScheduledRefreshJob.class).withIdentity("ScheduledRefreshJob").storeDurably().build();
            refreshJobDetail.getJobDataMap().put(QueryCacheManager.class.getName(), this);
            Trigger refreshTrigger = TriggerBuilder.newTrigger().withIdentity("ScheduledRefreshJob").withSchedule(refreshBuilder).build();
            scheduler.scheduleJob(refreshJobDetail, refreshTrigger);
            logger.info("Submitted cache 00:00 refresh job.");
        }

        if (!scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    @Override
    public UserTaskResultCache getCache(String user, String engineType) {
        Cache<String, UserTaskResultCache> userCaches = engineUserCaches.getOrDefault(engineType, null);
        if (userCaches == null) {
            userCaches = createUserCaches();
            Cache<String, UserTaskResultCache> oldCaches = engineUserCaches.putIfAbsent(engineType, userCaches);
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

}
