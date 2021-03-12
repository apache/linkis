package com.webank.wedatasphere.linkis.jobhistory.cache.impl;

import com.webank.wedatasphere.linkis.jobhistory.cache.QueryCacheManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class ScheduledRefreshJob extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(ScheduledRefreshJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Started cache refresh job.");
        QueryCacheManager queryCacheManager = (QueryCacheManager) jobExecutionContext.getJobDetail().getJobDataMap().get(QueryCacheManager.class.getName());
        queryCacheManager.refreshAll();
        logger.info("Finished cache refresh job.");
    }
}
