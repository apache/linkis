/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconn.computation.executor.upstream.access;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.conf.ServerConfiguration;

import org.apache.commons.lang3.StringUtils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ECTaskEntranceInfoAccessTest {

    @Before
    public void before() {
        //        System.getProperties().setProperty("wds.linkis.server.conf",
        // "linkis-et-jobhistory-scan.properties");
        System.out.println("Spring is enabled, now try to start SpringBoot.");
        System.out.println("<--------------------Start SpringBoot App-------------------->");
        String existsExcludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES().getValue();
        if (!StringUtils.isEmpty(existsExcludePackages)) {
            DataWorkCloudApplication.setProperty(
                    ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES().key(), existsExcludePackages);
        }

        String[] args =
                new String[] {
                    "--spring-conf", "eureka.client.serviceUrl.defaultZone=http://ip:port/eureka/",
                    "--spring-conf", "logging.config=classpath:log4j2.xml",
                    "--spring-conf", "spring.profiles.active=engineconn",
                    "--spring-conf", "server.port=28899",
                    "--spring-conf", "spring.application.name=linkis-cg-engineconn"
                };
        // 加载spring类
        try {
            //            ECTaskEntranceInfoAccessHelper.initApp(args);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        ServiceInstance[] instances =
                Sender.getInstances(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue());

        System.out.println("<--------------------SpringBoot App init succeed-------------------->");
    }

    @Test
    public void main() throws Exception {

        //        LinkisJobHistoryScanApplication.main(new String[]{"2021122919", "2021122921"});
    }
}
