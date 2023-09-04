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

package org.apache.linkis.monitor.scan.app;

import org.apache.linkis.monitor.scan.app.factory.MapperFactory;
import org.apache.linkis.monitor.scan.app.jobhistory.JobHistoryDataFetcher;
import org.apache.linkis.monitor.scan.app.jobhistory.errorcode.JobHistoryErrCodeRule;
import org.apache.linkis.monitor.scan.app.jobhistory.errorcode.JobHistoryErrorCodeAlertSender;
import org.apache.linkis.monitor.scan.app.jobhistory.jobtime.JobTimeExceedAlertSender;
import org.apache.linkis.monitor.scan.app.jobhistory.jobtime.JobTimeExceedRule;
import org.apache.linkis.monitor.scan.constants.Constants;
import org.apache.linkis.monitor.scan.core.pac.DataFetcher;
import org.apache.linkis.monitor.scan.core.scanner.AnomalyScanner;
import org.apache.linkis.monitor.scan.core.scanner.DefaultScanner;
import org.apache.linkis.monitor.scan.utils.alert.AlertDesc;
import org.apache.linkis.monitor.scan.utils.alert.ims.ImsAlertDesc;
import org.apache.linkis.monitor.scan.utils.alert.ims.JobHistoryScanImsAlertPropFileParserUtils;
import org.apache.linkis.monitor.scan.utils.log.LogUtils;
import org.apache.linkis.server.utils.LinkisMainHelper;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class LinkisJobHistoryScanApplication {
  private static final Logger logger = LogUtils.stdOutLogger();

  /** @param args: args[0]: startTime, args[1] endTime */
  public static void main(String[] args) throws ReflectiveOperationException {

    String serviceName = System.getProperty(LinkisMainHelper.SERVER_NAME_KEY());
    LinkisMainHelper.formatPropertyFiles(serviceName);

    long intervalMs = Constants.SCAN_INTERVALS_SECONDS() * 1000;
    long maxIntervalMs = Constants.MAX_INTERVALS_SECONDS() * 1000;
    long endTime = System.currentTimeMillis();
    long startTime = endTime - intervalMs;
    /** parse input into timestamp */
    if (args != null && args.length == 2) {
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
      Long tmpStart;
      Long tmpEnd;
      try {
        tmpStart = format.parse(args[0]).getTime();
        tmpEnd = format.parse(args[1]).getTime();
      } catch (ParseException e) {
        logger.info(
            "Failed to parse input into startTime and endTime." + ExceptionUtils.getMessage(e));
        return;
      }
      if (endTime <= startTime) {
        logger.info("[error] startTime larger than endTime");
        return;
      }
      if (tmpStart != null && tmpEnd != null) {
        startTime = tmpStart;
        endTime = tmpEnd;
      }
    }
    long realIntervals = endTime - startTime < maxIntervalMs ? endTime - startTime : maxIntervalMs;

    runApp(startTime, endTime, realIntervals, maxIntervalMs);
  }

  private static void runApp(long startTime, long endTime, long realIntervals, long maxIntervalMs) {

    AbstractApplicationContext context =
        new AnnotationConfigApplicationContext(LinkisJobHistoryScanSpringConfiguration.class);

    AnomalyScanner scanner = new DefaultScanner();
    boolean shouldStart = false;

    List<DataFetcher> fetchers = generateFetchers(startTime, endTime, maxIntervalMs);
    if (fetchers == null) {
      logger.warn("generated 0 dataFetchers, plz check input");
      return;
    }

    Map<String, AlertDesc> errorCodeAlerts =
        JobHistoryScanImsAlertPropFileParserUtils.getAlerts(Constants.SCAN_PREFIX_ERRORCODE());
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
    if (shouldStart) {
      scanner.addDataFetchers(fetchers);
      scanner.run();
      scanner.shutdown(); // wait all alert to be send
    }
    context.close();
  }

  private static List<DataFetcher> generateFetchers(
      long startTime, long endTime, long maxIntervalMs) {
    List<DataFetcher> ret = new ArrayList<>();
    long pe = endTime;
    long ps;
    while (pe > startTime) {
      ps = pe - maxIntervalMs > startTime ? pe - maxIntervalMs : startTime;
      String[] fetcherArgs = new String[] {String.valueOf(ps), String.valueOf(pe)};
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
