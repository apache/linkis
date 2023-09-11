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

import org.apache.linkis.monitor.utils.alert.ims.ImsAlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.ImsAlertLevel;
import org.apache.linkis.monitor.utils.alert.ims.ImsAlertWay;
import org.apache.linkis.monitor.utils.alert.ims.PooledImsAlertSender;
import org.apache.linkis.server.utils.LinkisMainHelper;

import java.util.HashSet;
import java.util.Set;

public class PooledImsAlertSenderTest {
  // @Before
  public void before() {
    System.getProperties().setProperty(LinkisMainHelper.SERVER_NAME_KEY(), "linkis-et-monitor");
    System.getProperties()
        .setProperty("log4j.configurationFile", "src/test/resources/log4j2-console.xml");
    //        System.getProperties().setProperty("wds.linkis.server.conf",
    // "linkis-et-monitor.properties");
  }

  // @org.junit.Test
  public void doSendAlert() throws Exception {
    Set<scala.Enumeration.Value> ways = new HashSet<>();
    ways.add(ImsAlertWay.WeChat());
    ways.add(ImsAlertWay.Email());

    Set<String> receivers = new HashSet<>();
    receivers.add("shangda, johnnwang");
    ImsAlertDesc desc =
        new ImsAlertDesc(
            "5435",
            "linkis_alert_test",
            "linkis_alert",
            "this is a test for linkis",
            ImsAlertLevel.MINOR(),
            "10.127.0.0.1",
            0,
            ways,
            receivers,
            3,
            12);

    System.out.println(desc);
    String url = "http://172.21.0.130:10812/ims_data_access/send_alarm_by_json.do";

    PooledImsAlertSender sender = new PooledImsAlertSender(url);
    sender.doSendAlert(desc);
  }
}
