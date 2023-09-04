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

import org.apache.linkis.server.utils.LinkisMainHelper;


public class LinkisJobHistoryScanApplicationTest {
  // @Before
  public void before() {
    System.getProperties().setProperty(LinkisMainHelper.SERVER_NAME_KEY(), "linkis-et-monitor");
    System.getProperties()
        .setProperty("log4j.configurationFile", "src/test/resources/log4j2-console.xml");
    //        System.getProperties().setProperty("wds.linkis.server.conf",
    // "linkis-et-monitor.properties");
  }

  // @Test
  public void main() throws Exception {
    LinkisJobHistoryScanApplication.main(new String[] {});
    //        LinkisJobHistoryScanApplication.main(new String[]{"2021122919", "2021122921"});
  }
}
