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

package org.apache.linkis.monitor.scheduled;

import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.core.pac.DataFetcher;
import org.apache.linkis.monitor.core.scanner.AnomalyScanner;
import org.apache.linkis.monitor.core.scanner.DefaultScanner;
import org.apache.linkis.monitor.factory.MapperFactory;
import org.apache.linkis.monitor.jobhistory.analyze.JobHistoryAnalyzeAlertSender;
import org.apache.linkis.monitor.jobhistory.analyze.JobHistoryAnalyzeRule;
import org.apache.linkis.monitor.jobhistory.errorcode.JobHistoryErrCodeRule;
import org.apache.linkis.monitor.jobhistory.errorcode.JobHistoryErrorCodeAlertSender;
import org.apache.linkis.monitor.jobhistory.index.JobIndexRule;
import org.apache.linkis.monitor.jobhistory.index.JobIndexSender;
import org.apache.linkis.monitor.jobhistory.jobtime.*;
import org.apache.linkis.monitor.jobhistory.jobtime.JobTimeExceedAlertSender;
import org.apache.linkis.monitor.jobhistory.jobtime.JobTimeExceedRule;
import org.apache.linkis.monitor.jobhistory.jobtime.StarrocksTimeExceedAlterSender;
import org.apache.linkis.monitor.jobhistory.jobtime.StarrocksTimeExceedRule;
import org.apache.linkis.monitor.jobhistory.labels.JobHistoryLabelsAlertSender;
import org.apache.linkis.monitor.jobhistory.labels.JobHistoryLabelsRule;
import org.apache.linkis.monitor.jobhistory.runtime.CommonJobRunTimeRule;
import org.apache.linkis.monitor.jobhistory.runtime.CommonRunTimeAlertSender;
import org.apache.linkis.monitor.jobhistory.runtime.JobHistoryRunTimeAlertSender;
import org.apache.linkis.monitor.jobhistory.runtime.JobHistoryRunTimeRule;
import org.apache.linkis.monitor.until.CacheUtils;
import org.apache.linkis.monitor.until.JobMonitorUtils;
import org.apache.linkis.monitor.utils.alert.AlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.MonitorAlertUtils;
import org.apache.linkis.monitor.utils.log.LogUtils;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

import org.slf4j.Logger;

/**
 * * jobHistory monitor 1.errorCode: Monitor the error code 2.userLabel: tenant label
 * monitoring,scan the execution data within the first 20 minutes, and judge the labels field of the
 * data
 *
 * <p>3.jobResultRunTime: Scan the execution data within the first 20 minutes, and judge the
 * completed tasks. If the parm field in the jobhistory contains (task.notification.conditions) and
 * the result of executing the task is (Succeed, Failed, Canceled, Timeout, ALL) any one of them, an
 * alarm is triggered and the result of the job is that it has ended. All three are indispensable
 *
 * <p>4.jobResultRunTimeForDSS: Scan the execution data within the first 20 minutes, scan the tasks
 * that have been marked for notification, if the task has ended, a notification will be initiated
 *
 * <p>5.jobHistoryUnfinishedScan: monitor the status of the execution task, scan the data outside 12
 * hours and within 24 hours
 */
@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class JobHistoryMonitor {

  private static final Logger logger = LogUtils.stdOutLogger();
  private static final long backtrackNum = 1000000L;

  @Scheduled(cron = "${linkis.monitor.jobHistory.finished.cron}")
  public void jobHistoryFinishedScan() {
    logger.info("Start scan jobHistoryFinishedScan");
    long intervalMs = 20 * 60 * 1000L;
    long maxIntervalMs = Constants.ERRORCODE_MAX_INTERVALS_SECONDS() * 1000;
    long endTime = System.currentTimeMillis();
    long startTime = endTime - intervalMs;
    long realIntervals = Math.min(endTime - startTime, maxIntervalMs);
    AnomalyScanner scanner = new DefaultScanner();
    AnomalyScanner scannerIndex = new DefaultScanner();
    boolean shouldStart = true;
    long id;
    if (null == CacheUtils.cacheBuilder.getIfPresent("jobHistoryId")) {
      long maxId = MapperFactory.getJobHistoryMapper().selectMaxId();
      long beginId = 0L;
      if (maxId > backtrackNum) {
        beginId = maxId - backtrackNum;
      }
      id = MapperFactory.getJobHistoryMapper().selectIdByHalfDay(beginId);
      logger.info("JobHistoryId is null, scan from the minimum ID:" + id);
      CacheUtils.cacheBuilder.put("jobHistoryId", id);
    } else {
      id = CacheUtils.cacheBuilder.getIfPresent("jobHistoryId");
      logger.info("Get JobHistoryId from cache ID:" + id);
    }
    List<DataFetcher> fetchers =
        JobMonitorUtils.generateFetchersfortime(startTime, endTime, id, "finished_job");
    if (fetchers.isEmpty()) {
      logger.warn("generated 0 dataFetchers, plz check input");
      return;
    }
    // errorCode
    try {
      Map<String, AlertDesc> errorCodeAlerts =
          MonitorAlertUtils.getAlerts(Constants.SCAN_PREFIX_ERRORCODE(), null);
      if (errorCodeAlerts == null || errorCodeAlerts.size() == 0) {
        logger.info(
            "JobHistoryErrCodeRule Alert error,Please check the linkis-et-monitor-ims.properties file");
      } else {
        logger.info("JobHistoryErrCodeRule Alert load {} success", errorCodeAlerts.size());
        JobMonitorUtils.addIntervalToImsAlerts(errorCodeAlerts, realIntervals);
        JobHistoryErrCodeRule jobHistoryErrCodeRule =
            new JobHistoryErrCodeRule(
                errorCodeAlerts.keySet(), new JobHistoryErrorCodeAlertSender(errorCodeAlerts));
        scanner.addScanRule(jobHistoryErrCodeRule);
      }
    } catch (Exception e) {
      logger.warn("JobHistoryErrCodeRule Scan Error msg: " + e.getMessage());
    }
    // userLabel
    try {
      Map<String, AlertDesc> userLabelAlerts =
          MonitorAlertUtils.getAlerts(Constants.USER_LABEL_MONITOR(), null);
      if (userLabelAlerts == null || userLabelAlerts.size() == 0) {
        logger.info(
            "JobHistoryLabelsRule Alert error,Please check the linkis-et-monitor-ims.properties file");
      } else {
        logger.info("JobHistoryLabelsRule Alert load {} success", userLabelAlerts.size());
        JobHistoryLabelsRule jobHistoryLabelsRule =
            new JobHistoryLabelsRule(new JobHistoryLabelsAlertSender());
        scanner.addScanRule(jobHistoryLabelsRule);
      }
    } catch (Exception e) {
      logger.warn("JobHistoryLabelsRule Scan Error msg: " + e.getMessage());
    }
    // jobResultRunTime
    try {
      Map<String, AlertDesc> jobResultAlerts =
          MonitorAlertUtils.getAlerts((Constants.SCAN_PREFIX_ERRORCODE()), null);
      if (jobResultAlerts == null || jobResultAlerts.size() == 0) {
        logger.info(
            "JobHistoryRunTimeRule Alert error,Please check the linkis-et-monitor-ims.properties file");
      } else {
        logger.info("JobHistoryRunTimeRule Alert load {} success", jobResultAlerts.size());
        JobHistoryRunTimeRule jobHistoryRunTimeRule =
            new JobHistoryRunTimeRule(new JobHistoryRunTimeAlertSender());
        scanner.addScanRule(jobHistoryRunTimeRule);
      }
    } catch (Exception e) {
      logger.warn("JobHistoryRunTimeRule Scan Error msg: " + e.getMessage());
    }
    // jobResultRunTimeForDSS
    try {
      Map<String, AlertDesc> dssJobResultAlerts =
          MonitorAlertUtils.getAlerts((Constants.SCAN_PREFIX_ERRORCODE()), null);
      if (dssJobResultAlerts == null || dssJobResultAlerts.size() == 0) {
        logger.info(
            "CommonJobRunTimeRule Alert error,Please check the linkis-et-monitor-ims.properties file");
      } else {
        logger.info("CommonJobRunTimeRule Alert load {} success", dssJobResultAlerts.size());
        CommonJobRunTimeRule commonJobRunTimeRule =
            new CommonJobRunTimeRule(new CommonRunTimeAlertSender());
        scanner.addScanRule(commonJobRunTimeRule);
      }
    } catch (Exception e) {
      logger.warn("CommonJobRunTimeRule Scan Error msg: " + e.getMessage());
    }
    // 新增失败任务分析扫描
    try {
      JobHistoryAnalyzeRule jobHistoryAnalyzeRule =
          new JobHistoryAnalyzeRule(new JobHistoryAnalyzeAlertSender());
      scanner.addScanRule(jobHistoryAnalyzeRule);
    } catch (Exception e) {
      logger.warn("JobHistoryAnalyzeRule Scan Error msg: " + e.getMessage());
    }
    // 执行任务扫描
    JobMonitorUtils.run(scanner, fetchers, true);

    // 任务指标上报
    JobIndexRule jobIndexRule = new JobIndexRule(new JobIndexSender());
    scannerIndex.addScanRule(jobIndexRule);
    List<DataFetcher> createFetcher =
        JobMonitorUtils.generateFetchersfortime(startTime, endTime, id, "");
    JobMonitorUtils.run(scannerIndex, createFetcher, true);
  }

  @Scheduled(cron = "${linkis.monitor.jobHistory.timeout.cron}")
  public void jobHistoryUnfinishedScan() {
    long id =
        Optional.ofNullable(CacheUtils.cacheBuilder.getIfPresent("jobhistoryScan"))
            .orElse(MonitorConfig.JOB_HISTORY_TIME_EXCEED.getValue());
    long intervalMs = Constants.TIMEOUT_INTERVALS_SECONDS() * 1000;
    long maxIntervalMs = Constants.ERRORCODE_MAX_INTERVALS_SECONDS() * 1000;
    long endTime = System.currentTimeMillis();
    long startTime = endTime - intervalMs;
    long realIntervals = Math.min(endTime - startTime, maxIntervalMs);
    AnomalyScanner scanner = new DefaultScanner();
    boolean shouldStart = false;
    List<DataFetcher> fetchers =
        JobMonitorUtils.generateFetchers(startTime, endTime, maxIntervalMs, id, "unfinished_job");
    if (fetchers.isEmpty()) {
      logger.warn("generated 0 dataFetchers, plz check input");
      return;
    }
    Map<String, AlertDesc> jobTimeAlerts =
        MonitorAlertUtils.getAlerts((Constants.SCAN_PREFIX_UNFINISHED_JOBTIME_EXCEED_SEC()), null);
    if (jobTimeAlerts == null || jobTimeAlerts.size() == 0) {
      logger.info("[INFO] Loaded 0 alerts jobtime alert-rule from alert properties file.");
    } else {
      logger.info(
          "[INFO] Loaded {} alerts jobtime alert-rules from alert properties file.",
          jobTimeAlerts.size());
      shouldStart = true;
      JobMonitorUtils.addIntervalToImsAlerts(jobTimeAlerts, realIntervals);
      JobTimeExceedRule jobTimeExceedRule =
          new JobTimeExceedRule(
              jobTimeAlerts.keySet(), new JobTimeExceedAlertSender(jobTimeAlerts));
      scanner.addScanRule(jobTimeExceedRule);
    }
    StarrocksTimeExceedRule starrocksTimeExceedRule =
        new StarrocksTimeExceedRule(new StarrocksTimeExceedAlterSender());
    scanner.addScanRule(starrocksTimeExceedRule);
    JobMonitorUtils.run(scanner, fetchers, shouldStart);
  }

  /** * 每10分钟扫描一次,扫描两个小时之内的任务，告警要求：管理台配置告警相关参数 */
  @Scheduled(cron = "${linkis.monitor.jdbc.timeout.alert.cron:0 0/10 0 * * ?}")
  public void jdbcUnfinishedAlertScan() {
    long id =
        Optional.ofNullable(CacheUtils.cacheBuilder.getIfPresent("jdbcUnfinishedAlertScan"))
            .orElse(MonitorConfig.JOB_HISTORY_TIME_EXCEED.getValue());
    long intervalMs = 7200 * 1000L;
    long maxIntervalMs = Constants.ERRORCODE_MAX_INTERVALS_SECONDS() * 1000;
    long endTime = System.currentTimeMillis();
    long startTime = endTime - intervalMs;
    AnomalyScanner scanner = new DefaultScanner();
    List<DataFetcher> fetchers =
        JobMonitorUtils.generateFetchers(startTime, endTime, maxIntervalMs, id, "");
    if (fetchers.isEmpty()) {
      logger.warn("jdbcUnfinishedScan generated 0 dataFetchers, plz check input");
      return;
    }
    StarrocksTimeExceedRule starrocksTimeExceedRule =
        new StarrocksTimeExceedRule(new StarrocksTimeExceedAlertSender());
    scanner.addScanRule(starrocksTimeExceedRule);
    JobMonitorUtils.run(scanner, fetchers, true);
  }

  /** * 每10分钟扫描一次,扫描两个小时之内的任务，满足要求触发kill kill要求：数据源配置kill参数 */
  @Scheduled(cron = "${linkis.monitor.jdbc.timeout.kill.cron:0 0/10 0 * * ?}")
  public void jdbcUnfinishedKillScan() {
    long id =
        Optional.ofNullable(CacheUtils.cacheBuilder.getIfPresent("jdbcUnfinishedKillScan"))
            .orElse(MonitorConfig.JOB_HISTORY_TIME_EXCEED.getValue());
    long intervalMs = 7200 * 1000L;
    long maxIntervalMs = Constants.ERRORCODE_MAX_INTERVALS_SECONDS() * 1000;
    long endTime = System.currentTimeMillis();
    long startTime = endTime - intervalMs;
    AnomalyScanner scanner = new DefaultScanner();
    List<DataFetcher> fetchers =
        JobMonitorUtils.generateFetchers(startTime, endTime, maxIntervalMs, id, "");
    if (fetchers.isEmpty()) {
      logger.warn("jdbcUnfinishedScan generated 0 dataFetchers, plz check input");
      return;
    }
    StarrocksTimeKillRule starrocksTimeKillRule =
        new StarrocksTimeKillRule(new StarrocksTimeKillAlertSender());
    scanner.addScanRule(starrocksTimeKillRule);
    JobMonitorUtils.run(scanner, fetchers, true);
  }
}
