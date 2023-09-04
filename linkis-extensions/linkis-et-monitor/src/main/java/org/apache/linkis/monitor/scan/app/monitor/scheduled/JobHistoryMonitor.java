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

package org.apache.linkis.monitor.scan.app.monitor.scheduled;

import org.apache.linkis.monitor.scan.app.factory.MapperFactory;
import org.apache.linkis.monitor.scan.app.jobhistory.JobHistoryDataFetcher;
import org.apache.linkis.monitor.scan.app.jobhistory.errorcode.JobHistoryErrCodeRule;
import org.apache.linkis.monitor.scan.app.jobhistory.errorcode.JobHistoryErrorCodeAlertSender;
import org.apache.linkis.monitor.scan.app.jobhistory.jobtime.JobTimeExceedAlertSender;
import org.apache.linkis.monitor.scan.app.jobhistory.jobtime.JobTimeExceedRule;
import org.apache.linkis.monitor.scan.app.jobhistory.labels.JobHistoryLabelsAlertSender;
import org.apache.linkis.monitor.scan.app.jobhistory.labels.JobHistoryLabelsRule;
import org.apache.linkis.monitor.scan.app.jobhistory.runtime.CommonRunTimeAlertSender;
import org.apache.linkis.monitor.scan.app.jobhistory.runtime.CommonJobRunTimeRule;
import org.apache.linkis.monitor.scan.app.jobhistory.runtime.JobHistoryRunTimeAlertSender;
import org.apache.linkis.monitor.scan.app.jobhistory.runtime.JobHistoryRunTimeRule;
import org.apache.linkis.monitor.scan.app.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.scan.app.monitor.until.CacheUtils;
import org.apache.linkis.monitor.scan.constants.Constants;
import org.apache.linkis.monitor.scan.core.pac.DataFetcher;
import org.apache.linkis.monitor.scan.core.scanner.AnomalyScanner;
import org.apache.linkis.monitor.scan.core.scanner.DefaultScanner;
import org.apache.linkis.monitor.scan.utils.alert.AlertDesc;
import org.apache.linkis.monitor.scan.utils.alert.ims.ImsAlertDesc;
import org.apache.linkis.monitor.scan.utils.alert.ims.JobHistoryScanImsAlertPropFileParserUtils;
import org.apache.linkis.monitor.scan.utils.alert.ims.UserLabelAlertUtils;
import org.apache.linkis.monitor.scan.utils.log.LogUtils;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

import org.slf4j.Logger;

/***
 * jobHistory monitor
 * 1.errorCode: Monitor the error code
 * 2.userLabel: tenant label monitoring, scan the execution data within the first 20 minutes, and judge the labels field of the data
 * 3.jobResultRunTime: Scan the execution data within the first 20 minutes, and judge the completed tasks. If the parm field in the jobhistory contains (task.notification.conditions) and the result of executing the task is (Succeed, Failed, Canceled, Timeout, ALL) any one of them, an alarm is triggered and the result of the job is that it has ended. All three are indispensable
 * 4.jobResultRunTimeForDSS: Scan the execution data within the first 20 minutes, scan the tasks that have been marked for notification, if the task has ended, a notification will be initiated
 * 5.jobHistoryUnfinishedScan: monitor the status of the execution task, scan the data outside 12 hours and within 24 hours
 */
@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class JobHistoryMonitor {

    private static final Logger logger = LogUtils.stdOutLogger();
    private static final long backtrackNum = 1000000L;

    /**
     * Scan tasks completed within 20 minutes
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void jobHistoryFinishedScan() {
        long intervalMs = 20 * 60 * 1000; // 20分钟
        long maxIntervalMs = Constants.MAX_INTERVALS_SECONDS() * 1000;
        long endTime = System.currentTimeMillis();
        long startTime = endTime - intervalMs;
        long realIntervals = endTime - startTime < maxIntervalMs ? endTime - startTime : maxIntervalMs;
        AnomalyScanner scanner = new DefaultScanner();
        boolean shouldStart = false;
        long id;
        if (null == CacheUtils.cacheBuilder.getIfPresent("jobHistoryId")) {
            //20230206：新增获取最大值-100W 作为初始id进行查询，防止慢查询
            long maxId = MapperFactory.getJobHistoryMapper().selectMaxId();
            long beginId = 0L;
            if (maxId > backtrackNum) {
                beginId = maxId - backtrackNum;
            }
            id = MapperFactory.getJobHistoryMapper().selectIdByHalfDay(beginId);
            CacheUtils.cacheBuilder.put("jobHistoryId", id);
        } else {
            id = CacheUtils.cacheBuilder.getIfPresent("jobHistoryId");
        }
        List<DataFetcher> fetchers = generateFetchersfortime(startTime, endTime, id, "updated_time");
        if (fetchers == null) {
            logger.warn("generated 0 dataFetchers, plz check input");
            return;
        }
        // errorCode
        try {
            Map<String, AlertDesc> errorCodeAlerts = JobHistoryScanImsAlertPropFileParserUtils.getAlerts(Constants.SCAN_PREFIX_ERRORCODE());

            if (errorCodeAlerts == null || errorCodeAlerts.size() == 0) {
                logger.info("[INFO] Loaded 0 errorcode alert from alert-rule properties file.");
            } else {
                logger.info(
                        "[INFO] Loaded {} errorcode alert from alert-rules properties file.",
                        errorCodeAlerts.size());
                shouldStart = true;
                addIntervalToImsAlerts(errorCodeAlerts, realIntervals);
                JobHistoryErrCodeRule jobHistoryErrCodeRule =
                        new JobHistoryErrCodeRule(
                                errorCodeAlerts.keySet(), new JobHistoryErrorCodeAlertSender(errorCodeAlerts));
                scanner.addScanRule(jobHistoryErrCodeRule);
            }
        } catch (Exception e) {
            logger.warn("Jobhistory Monitor ErrorCode Faily: "+ e.getMessage());
        }
        // userLabel
        try {
            Map<String, AlertDesc> userLabelAlerts =
                    UserLabelAlertUtils.getAlerts(Constants.USER_LABEL_MONITOR(), "");
            if (userLabelAlerts == null || userLabelAlerts.size() == 0) {
                logger.info("[INFO] Loaded 0 alerts userLabel alert-rule from alert properties file.");
            } else {
                logger.info(
                        "[INFO] Loaded {} alerts userLabel alert-rules from alert properties file.",
                        userLabelAlerts.size());
                shouldStart = true;
                JobHistoryLabelsRule jobHistoryLabelsRule =
                        new JobHistoryLabelsRule(new JobHistoryLabelsAlertSender());
                scanner.addScanRule(jobHistoryLabelsRule);
            }
        } catch (Exception e) {
            logger.warn("Jobhistory Monitor UserLabel Faily: "+ e.getMessage());
        }
        // jobResultRunTime
        try {
            Map<String, AlertDesc> jobResultAlerts =
                    JobHistoryScanImsAlertPropFileParserUtils.getAlerts(Constants.SCAN_PREFIX_ERRORCODE());
            if (jobResultAlerts == null || jobResultAlerts.size() == 0) {
                logger.info("[INFO] Loaded 0 jobResult alert from alert-rule properties file.");
            } else {
                logger.info(
                        "[INFO] Loaded {} alerts jobResult alert-rules from alert properties file.",
                        jobResultAlerts.size());
                shouldStart = true;
                JobHistoryRunTimeRule jobHistoryRunTimeRule =
                        new JobHistoryRunTimeRule(new JobHistoryRunTimeAlertSender());
                scanner.addScanRule(jobHistoryRunTimeRule);
            }
        } catch (Exception e) {
            logger.warn("Jobhistory Monitor JobResultRunTime Faily: "+ e.getMessage());
        }
        // jobResultRunTimeForDSS
        try {
            Map<String, AlertDesc> dssJobResultAlerts =
                    JobHistoryScanImsAlertPropFileParserUtils.getAlerts(Constants.SCAN_PREFIX_ERRORCODE());
            if (dssJobResultAlerts == null || dssJobResultAlerts.size() == 0) {
                logger.info("[INFO] Loaded 0 jobResult alert from alert-rule properties file.");
            } else {
                logger.info(
                        "[INFO] Loaded {} alerts jobResult alert-rules from alert properties file.",
                        dssJobResultAlerts.size());
                shouldStart = true;

                CommonJobRunTimeRule commonJobRunTimeRule =
                        new CommonJobRunTimeRule(new CommonRunTimeAlertSender());
                scanner.addScanRule(commonJobRunTimeRule);
            }
        } catch (Exception e) {
            logger.warn("Jobhistory JobResultRunTimeForDSS ErrorCode Faily: "+ e.getMessage());
        }
        run(scanner, fetchers, shouldStart);
    }

    /***
     * Whether scanning data within 12 hours has timed out
     */
    @Scheduled(cron = "${linkis.monitor.jobHistory.ScanTask.cron}")
    public void jobHistoryUnfinishedScan() {
        long id =
                Optional.ofNullable(CacheUtils.cacheBuilder.getIfPresent("jobhistoryScan"))
                        .orElse(MonitorConfig.JOB_HISTORY_TIME_EXCEED.getValue());
        long intervalMs = Constants.SCAN_INTERVALS_SECONDS() * 1000;
        long maxIntervalMs = Constants.MAX_INTERVALS_SECONDS() * 1000;
        long endTime = System.currentTimeMillis();
        long startTime = endTime - intervalMs;
        long realIntervals = endTime - startTime < maxIntervalMs ? endTime - startTime : maxIntervalMs;
        AnomalyScanner scanner = new DefaultScanner();
        boolean shouldStart = false;
        List<DataFetcher> fetchers = generateFetchers(startTime, endTime, maxIntervalMs, id, "created_time");
        if (fetchers == null) {
            logger.warn("generated 0 dataFetchers, plz check input");
            return;
        }
        Map<String, AlertDesc> jobTimeAlerts =
                JobHistoryScanImsAlertPropFileParserUtils.getAlerts(
                        Constants.SCAN_PREFIX_UNFINISHED_JOBTIME_EXCEED_SEC());
        if (jobTimeAlerts == null || jobTimeAlerts.size() == 0) {
            logger.info("[INFO] Loaded 0 alerts jobtime alert-rule from alert properties file.");
        } else {
            logger.info(
                    "[INFO] Loaded {} alerts jobtime alert-rules from alert properties file.",
                    jobTimeAlerts.size());
            shouldStart = true;
            addIntervalToImsAlerts(jobTimeAlerts, realIntervals);
            JobTimeExceedRule jobTimeExceedRule =
                    new JobTimeExceedRule(
                            jobTimeAlerts.keySet(), new JobTimeExceedAlertSender(jobTimeAlerts));
            scanner.addScanRule(jobTimeExceedRule);
        }
        run(scanner, fetchers, shouldStart);
    }

    public static void run(AnomalyScanner scanner, List<DataFetcher> fetchers, Boolean shouldStart) {
        if (shouldStart) {
            scanner.addDataFetchers(fetchers);
            scanner.run();
            //            scanner.shutdown(); // wait all alert to be send
        }
    }

    private static List<DataFetcher> generateFetchers(
            long startTime, long endTime, long maxIntervalMs, long id, String timeType) {
        List<DataFetcher> ret = new ArrayList<>();
        long pe = endTime;
        long ps;
        while (pe > startTime) {
            ps = pe - maxIntervalMs > startTime ? pe - maxIntervalMs : startTime;
            String[] fetcherArgs =
                    new String[]{String.valueOf(ps), String.valueOf(pe), String.valueOf(id), timeType};
            ret.add(new JobHistoryDataFetcher(fetcherArgs, MapperFactory.getJobHistoryMapper()));
            logger.info(
                    "Generated dataFetcher for startTime: "
                            + new Date(ps).toString()
                            + ". EndTime: "
                            + new Date(pe).toString());
            pe = pe - maxIntervalMs;
        }
        return ret;
    }

    private static List<DataFetcher> generateFetchersfortime(long startTime, long endTime, long id, String timeType) {
        List<DataFetcher> fetchers = new ArrayList<>();
        String[] fetcherArgs =
                new String[]{String.valueOf(startTime), String.valueOf(endTime), String.valueOf(id), timeType};
        fetchers.add(new JobHistoryDataFetcher(fetcherArgs, MapperFactory.getJobHistoryMapper()));
        logger.info(
                "Generated dataFetcher for startTime: "
                        + new Date(startTime).toString()
                        + ". EndTime: "
                        + new Date(endTime).toString());
        return fetchers;
    }

    private static void addIntervalToImsAlerts(Map<String, AlertDesc> alerts, long realIntervals) {
        for (AlertDesc alert : alerts.values()) {
            if (!(alert instanceof ImsAlertDesc)) {
                logger.info("[warn]  ignore wrong alert" + alert);
            } else {
                ((ImsAlertDesc) alert).hitIntervalMs_$eq(realIntervals);
            }
        }
    }
}
