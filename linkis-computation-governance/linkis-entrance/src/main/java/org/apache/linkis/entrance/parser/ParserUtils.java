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

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.entrance.conf.EntranceConfiguration$;
import org.apache.linkis.entrance.utils.CommonLogPathUtils;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.manager.label.utils.LabelUtil;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class ParserUtils {

  private static final Map<String, String> types = new HashMap<>();

  static {
    types.put("py", "python");
    types.put("python", "python");
    types.put("sql", "sql");
    types.put("pyspark", "python");
    types.put("scala", "scala");
    types.put("rspark", "r");
    types.put("r", "r");
    types.put("java", "java");
    types.put("hql", "hql");
    types.put("sparksql", "sql");
  }

  public static void generateLogPath(JobRequest jobRequest, Map<String, String> params) {
    String logPath = null;
    String logPathPrefix = null;
    String logMid = "log";
    if (StringUtils.isEmpty(logPathPrefix)) {
      logPathPrefix = EntranceConfiguration$.MODULE$.DEFAULT_LOGPATH_PREFIX().getValue();
    }
    /*Determine whether logPathPrefix is terminated with /, if it is, delete */
    /*判断是否logPathPrefix是否是以 / 结尾， 如果是，就删除*/
    if (logPathPrefix.endsWith("/")) {
      logPathPrefix = logPathPrefix.substring(0, logPathPrefix.length() - 1);
    }
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = dateFormat.format(date);
    String creator = LabelUtil.getUserCreator(jobRequest.getLabels())._2;
    String umUser = jobRequest.getExecuteUser();
    FsPath lopPrefixPath = new FsPath(logPathPrefix);
    if (StorageUtils.HDFS.equals(lopPrefixPath.getFsType())) {
      String commonLogPath = logPathPrefix + "/" + "log" + "/" + dateString + "/" + creator;
      logPath = commonLogPath + "/" + umUser + "/" + jobRequest.getId() + ".log";
      CommonLogPathUtils.buildCommonPath(commonLogPath);
    } else {
      logPath =
          logPathPrefix
              + "/"
              + umUser
              + "/"
              + "log"
              + "/"
              + creator
              + "/"
              + dateString
              + "/"
              + jobRequest.getId()
              + ".log";
    }
    jobRequest.setLogPath(logPath);
  }

  public static String getCorrespondingType(String runType) {
    return types.get(runType.toLowerCase());
  }
}
