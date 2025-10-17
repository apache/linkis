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

package org.apache.linkis.entrance.parser;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.entrance.utils.CommonLogPathUtils;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.manager.label.utils.LabelUtil;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public final class ParserUtils {

  public static void generateLogPath(JobRequest jobRequest, Map<String, String> params) {
    String logPath = null;
    String logPathPrefix = null;
    String logMid = "log";
    if (StringUtils.isEmpty(logPathPrefix)) {
      logPathPrefix = GovernanceCommonConf.DEFAULT_LOGPATH_PREFIX();
    }
    /*Determine whether logPathPrefix is terminated with /, if it is, delete */
    /*判断是否logPathPrefix是否是以 / 结尾， 如果是，就删除*/
    if (logPathPrefix.endsWith("/")) { // NOSONAR
      logPathPrefix = logPathPrefix.substring(0, logPathPrefix.length() - 1);
    }
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat hourFormat = new SimpleDateFormat("HH"); // 新增：小时格式（24小时制）
    String dateString = dateFormat.format(date);
    String hourString = hourFormat.format(date); // 新增：当前小时（如 "08", "14"）
    String creator = LabelUtil.getUserCreator(jobRequest.getLabels())._2;
    String umUser = jobRequest.getExecuteUser();
    FsPath lopPrefixPath = new FsPath(logPathPrefix);
    if (StorageUtils.HDFS().equals(lopPrefixPath.getFsType())) {
      String commonLogPath = logPathPrefix + "/" + "log" + "/" + dateString + "/";
      if (Configuration.HDFS_HOUR_DIR_SWITCH()) {
        commonLogPath = commonLogPath + hourString + "/" + creator;
      } else {
        commonLogPath = commonLogPath + creator;
      }
      logPath = commonLogPath + "/" + umUser + "/" + jobRequest.getId() + ".log";
      CommonLogPathUtils.buildCommonPath(commonLogPath, false);
    } else {
      logPath = logPathPrefix + "/" + umUser + "/log/" + creator + "/";
      if (Configuration.HDFS_HOUR_DIR_SWITCH()) {
        logPath = logPath + dateString + "/" + hourString + "/" + jobRequest.getId() + ".log";
      } else {
        logPath = logPath + hourString + "/" + jobRequest.getId() + ".log";
      }
    }
    jobRequest.setLogPath(logPath);
  }
}
