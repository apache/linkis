
package org.apache.linkis.engineconn.computation.executor.upstream.access;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.DWCArgumentsParser;
import org.apache.linkis.engineconn.common.creation.DefaultEngineCreationContext;
import org.apache.linkis.engineconn.common.creation.EngineCreationContext;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.utils.EngineConnArgumentsParser;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.conf.ServerConfiguration;
import org.apache.linkis.server.utils.LinkisMainHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shangda on 2022/2/9.
 */
public class ECTaskEntranceInfoAccessTest {

    @Before
    public void before() {
//        System.getProperties().setProperty("wds.linkis.server.conf", "linkis-et-jobhistory-scan.properties");
        System.out.println("Spring is enabled, now try to start SpringBoot.");
        System.out.println("<--------------------Start SpringBoot App-------------------->");
        String existsExcludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES().getValue();
        if (!StringUtils.isEmpty(existsExcludePackages)) {
            DataWorkCloudApplication.setProperty(ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES().key(), existsExcludePackages);
        }

        String[] args = new String[]{
                "--spring-conf","eureka.client.serviceUrl.defaultZone=http://ip:port/eureka/",
                "--spring-conf", "logging.config=classpath:log4j2.xml",
                "--spring-conf", "spring.profiles.active=engineconn",
                "--spring-conf", "server.port=28899",
                "--spring-conf", "spring.application.name=linkis-cg-engineconn"};
        // 加载spring类
        try {
//            ECTaskEntranceInfoAccessHelper.initApp(args);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        ServiceInstance[] instances = Sender.getInstances(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue());

        System.out.println("<--------------------SpringBoot App init succeed-------------------->");
    }

    @Test
    public void main() throws Exception {


//        LinkisJobHistoryScanApplication.main(new String[]{"2021122919", "2021122921"});
    }

}