package com.webank.wedatasphere.linkis.cs.persistence;

import com.webank.wedatasphere.linkis.cs.persistence.persistence.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by patinousward on 2020/2/23.
 */
@Configuration
public class ContextPersistenceBeans {

    @Bean
    @ConditionalOnMissingBean(ContextPersistenceManager.class)
    public ContextPersistenceManager getContextPersistenceManager(
            ContextIDPersistence contextIDPersistence,
            ContextMapPersistence contextMapPersistence,
            ContextMetricsPersistence contextMetricsPersistence,
            ContextIDListenerPersistence contextIDListenerPersistence,
            ContextKeyListenerPersistence contextKeyListenerPersistence,
            TransactionManager transactionManager
    ) {
        ContextPersistenceManagerImpl manager = new ContextPersistenceManagerImpl();
        manager.setContextIDPersistence(contextIDPersistence);
        manager.setContextMapPersistence(contextMapPersistence);
        manager.setContextMetricsPersistence(contextMetricsPersistence);
        manager.setContextKeyListenerPersistence(contextKeyListenerPersistence);
        manager.setContextIDListenerPersistence(contextIDListenerPersistence);
        manager.setTransactionManager(transactionManager);
        return manager;
    }
}
