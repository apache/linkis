package org.apache.linkis.publicservice.common.lock;

import org.apache.linkis.publicservice.common.lock.dao.CommonLockMapper;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.apache.linkis.publicservice.common.lock.service.impl.DefaultCommonLockService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonLockSpringConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CommonLockService getDefaultLockManagerPersistence(CommonLockMapper commonLockMapper) {
        DefaultCommonLockService defaultCommonLockService = new DefaultCommonLockService();
        defaultCommonLockService.setLockManagerMapper(commonLockMapper);
        return defaultCommonLockService;
    }
}
