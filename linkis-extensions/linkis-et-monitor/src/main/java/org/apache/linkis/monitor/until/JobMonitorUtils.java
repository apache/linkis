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

package org.apache.linkis.monitor.until;

import org.apache.linkis.monitor.core.pac.DataFetcher;
import org.apache.linkis.monitor.core.scanner.AnomalyScanner;
import org.apache.linkis.monitor.factory.MapperFactory;
import org.apache.linkis.monitor.jobhistory.JobHistoryDataFetcher;
import org.apache.linkis.monitor.utils.alert.AlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.ImsAlertDesc;
import org.apache.linkis.monitor.utils.log.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class JobMonitorUtils {

  private static final Logger logger = LogUtils.stdOutLogger();

  public static void run(AnomalyScanner scanner, List<DataFetcher> fetchers, Boolean shouldStart) {
    if (shouldStart) {
      scanner.addDataFetchers(fetchers);
      scanner.run();
    }
  }

  public static List<DataFetcher> generateFetchers(
      long startTime, long endTime, long maxIntervalMs, long id, String jobStatus) {
    List<DataFetcher> ret = new ArrayList<>();
    long pe = endTime;
    long ps;
    while (pe > startTime) {
      ps = Math.max(pe - maxIntervalMs, startTime);
      String[] fetcherArgs =
          new String[] {String.valueOf(ps), String.valueOf(pe), String.valueOf(id), jobStatus};
      ret.add(new JobHistoryDataFetcher(fetcherArgs, MapperFactory.getJobHistoryMapper()));
      logger.info(
          "Generated dataFetcher for startTime: " + new Date(ps) + ". EndTime: " + new Date(pe));
      pe = pe - maxIntervalMs;
    }
    return ret;
  }

  public static List<DataFetcher> generateFetchersfortime(
      long startTime, long endTime, long id, String jobStatus) {
    List<DataFetcher> fetchers = new ArrayList<>();
    String[] fetcherArgs =
        new String[] {
          String.valueOf(startTime), String.valueOf(endTime), String.valueOf(id), jobStatus
        };
    fetchers.add(new JobHistoryDataFetcher(fetcherArgs, MapperFactory.getJobHistoryMapper()));
    logger.info(
        "Generated dataFetcher for startTime: "
            + new Date(startTime)
            + ". EndTime: "
            + new Date(endTime));
    return fetchers;
  }

  public static void addIntervalToImsAlerts(Map<String, AlertDesc> alerts, long realIntervals) {
    for (AlertDesc alert : alerts.values()) {
      if (!(alert instanceof ImsAlertDesc)) {
        logger.info("[warn]  ignore wrong alert" + alert);
      } else {
        ((ImsAlertDesc) alert).hitIntervalMs_$eq(realIntervals);
      }
    }
  }
}
