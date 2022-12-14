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

package org.apache.linkis.ecm.server.service.impl;

import org.apache.linkis.engineconn.common.conf.EngineConnConf;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultEngineConnKillServiceTest {

  @Test
  @DisplayName("testParseYarnAppId")
  public void testParseYarnAppId() {
    String log =
        "2022-07-14 14:08:46.854 INFO  [Linkis-Default-Scheduler-Thread-1] org.apache.hadoop.mapreduce.Job 1294 submit - The url to track the job: http://hadoop:8088/proxy/application_1645869964061_2740/";
    String regex = EngineConnConf.SQOOP_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX().getValue();
    Pattern pattern = Pattern.compile(regex);
    Matcher mApp = pattern.matcher(log);
    if (mApp.find()) {
      String c = mApp.group(mApp.groupCount());
      assertEquals(c, "application_1645869964061_2740");
    }
  }

  @Test
  @DisplayName("testKillYarnAppIdOfOneEc")
  public void testKillYarnAppIdOfOneEc() {
    String line1 =
        "15:44:04.370 ERROR org.apache.linkis.manager.engineplugin.shell.executor.YarnAppIdExtractor$$anonfun$addYarnAppIds$1 123 apply - Submitted application application_1609166102854_970911";
    String line2 =
        "15:44:04.370 ERROR org.apache.linkis.manager.engineplugin.shell.executor.YarnAppIdExtractor$$anonfun$addYarnAppIds$1 123 apply - Submitted application application_1609166102854_970912";
    String[] logs = new String[] {line1, line2};
    String regex = EngineConnConf.SPARK_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX().getValue();
    Pattern pattern = Pattern.compile(regex);
    List<String> appIds = new ArrayList<>(2);
    for (String log : logs) {
      Matcher mApp = pattern.matcher(log);
      if (mApp.find()) {
        String c = mApp.group(mApp.groupCount());
        if (!appIds.contains(c)) {
          appIds.add(c);
        }
      }
    }
    assertEquals(appIds.size(), 2);
    assertEquals(appIds.get(0), "application_1609166102854_970911");
    assertEquals(appIds.get(1), "application_1609166102854_970912");
    String yarnAppKillScriptPath = "/tmp/sbin/kill-yarn-jobs.sh";
    String[] cmdArr = new String[appIds.size() + 2];
    cmdArr[0] = "sh";
    cmdArr[1] = yarnAppKillScriptPath;
    for (int i = 0; i < appIds.size(); i++) {
      cmdArr[i + 2] = appIds.get(i);
    }
    assertEquals(cmdArr.length, 4);
    String cmd = StringUtils.join(cmdArr, " ");
    assertEquals(
        cmd,
        "sh /tmp/sbin/kill-yarn-jobs.sh application_1609166102854_970911 application_1609166102854_970912");
  }
}
