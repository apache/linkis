package com.webank.wedatasphere.linkis.enginemanager.flink.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;
import com.webank.wedatasphere.linkis.enginemanager.EngineHook;
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration;
import com.webank.wedatasphere.linkis.enginemanager.hook.ConsoleConfigurationEngineHook;
import com.webank.wedatasphere.linkis.enginemanager.hook.JarLoaderEngineHook;
import com.webank.wedatasphere.linkis.resourcemanager.LoadInstanceResource;
import com.webank.wedatasphere.linkis.resourcemanager.Resource;
import com.webank.wedatasphere.linkis.resourcemanager.ResourceRequestPolicy;
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo;
import com.webank.wedatasphere.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-07-28 16:42
 */

@Configuration
public class FlinkEngineManagerConfiguration {


    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean(name="resources")
    ModuleInfo createResource()  {
        Resource totalresource = new LoadInstanceResource(
                EnvConfiguration.ENGINE_MANAGER_MAX_MEMORY_AVAILABLE().getValue().toLong() ,
                EnvConfiguration.ENGINE_MANAGER_MAX_CORES_AVAILABLE().getValue(),
                EnvConfiguration.ENGINE_MANAGER_MAX_CREATE_INSTANCES().getValue());

        Resource protectresource = new LoadInstanceResource(
                EnvConfiguration.ENGINE_MANAGER_PROTECTED_MEMORY().getValue().toLong(),
                EnvConfiguration.ENGINE_MANAGER_PROTECTED_CORES().getValue(),
                EnvConfiguration.ENGINE_MANAGER_PROTECTED_CORES().getValue());
        return new ModuleInfo(Sender.getThisServiceInstance(), totalresource, protectresource, ResourceRequestPolicy.LoadInstance());
    }

    @Bean(name="hooks")
    EngineHook[] createEngineHook()  {
        return new EngineHook[] {new ConsoleConfigurationEngineHook(), new JarLoaderEngineHook()};
    }

}
