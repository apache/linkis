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

package org.apache.linkis.monitor.utils.alert;

import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.utils.alert.ims.MonitorAlertUtils;
import org.apache.linkis.monitor.utils.alert.ims.PooledImsAlertUtils;
import org.apache.linkis.server.utils.LinkisMainHelper;

import java.util.Map;

public class PooledImsAlertUtilsTest {
  // @Before
  public void before() {
    System.getProperties().setProperty(LinkisMainHelper.SERVER_NAME_KEY(), "linkis-et-monitor");
    System.getProperties()
        .setProperty("log4j.configurationFile", "src/test/resources/log4j2-console.xml");
    //        System.getProperties().setProperty("wds.linkis.server.conf",
    // "linkis-et-monitor.properties");
  }

  // @Test
  public void addAlert() throws Exception {
    PooledImsAlertUtils.addAlert("1st test");
    Map<String, AlertDesc> alerts =
        MonitorAlertUtils.getAlerts((Constants.SCAN_PREFIX_ERRORCODE()), null);
    for (Map.Entry<String, AlertDesc> kv : alerts.entrySet()) {
      System.out.println(kv.getKey() + ": " + kv.getValue().toString());
      PooledImsAlertUtils.addAlert(kv.getValue());
    }
    Thread.sleep(2000l);
    PooledImsAlertUtils.shutDown(true, -1);
  }
}
