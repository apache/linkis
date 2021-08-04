package com.webank.wedatasphere.linkis.manager;

import com.webank.wedatasphere.linkis.manager.dao.*;
import com.webank.wedatasphere.linkis.manager.persistence.*;
import com.webank.wedatasphere.linkis.manager.persistence.impl.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class PersistenceSpringConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ManagerPersistence getDefaultManagerPersistence(NodeManagerPersistence nodeManagerPersistence,
                                                           NodeMetricManagerPersistence nodeMetricManagerPersistence,
                                                           LabelManagerPersistence labelManagerPersistence,
                                                           LockManagerPersistence lockManagerPersistence,
                                                           ResourceManagerPersistence resourceManagerPersistence,
                                                           ResourceLabelPersistence resourceLabelPersistence) {
        DefaultManagerPersistence defaultManagerPersistence = new DefaultManagerPersistence();
        defaultManagerPersistence.setResourceManagerPersistence(resourceManagerPersistence);
        defaultManagerPersistence.setNodeMetricManagerPersistence(nodeMetricManagerPersistence);
        defaultManagerPersistence.setNodeManagerPersistence(nodeManagerPersistence);
        defaultManagerPersistence.setLockManagerPersistence(lockManagerPersistence);
        defaultManagerPersistence.setLabelManagerPersistence(labelManagerPersistence);
        defaultManagerPersistence.setResourceLabelPersistence(resourceLabelPersistence);
        return defaultManagerPersistence;
    }

    @Bean
    @ConditionalOnMissingBean
    public NodeManagerPersistence getDefaultNodeManagerPersistence(NodeManagerMapper nodeManagerMapper,
                                                                   NodeMetricManagerMapper metricManagerMapper) {
        DefaultNodeManagerPersistence defaultNodeManagerPersistence = new DefaultNodeManagerPersistence();
        defaultNodeManagerPersistence.setMetricManagerMapper(metricManagerMapper);
        defaultNodeManagerPersistence.setNodeManagerMapper(nodeManagerMapper);
        return defaultNodeManagerPersistence;
    }

    @Bean
    @ConditionalOnMissingBean
    public LabelManagerPersistence getDefaultLabelManagerPersistence(LabelManagerMapper labelManagerMapper,
                                                                     NodeManagerMapper nodeManagerMapper) {
        DefaultLabelManagerPersistence defaultLabelManagerPersistence = new DefaultLabelManagerPersistence();
        defaultLabelManagerPersistence.setLabelManagerMapper(labelManagerMapper);
        defaultLabelManagerPersistence.setNodeManagerMapper(nodeManagerMapper);
        return defaultLabelManagerPersistence;
    }

    @Bean
    @ConditionalOnMissingBean
    public LockManagerPersistence getDefaultLockManagerPersistence(LockManagerMapper lockManagerMapper) {
        DefaultLockManagerPersistence defaultLockManagerPersistence = new DefaultLockManagerPersistence();
        defaultLockManagerPersistence.setLockManagerMapper(lockManagerMapper);
        return defaultLockManagerPersistence;
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceManagerPersistence getDefaultResourceManagerPersistence(ResourceManagerMapper resourceManagerMapper,
                                                                           NodeManagerMapper nodeManagerMapper,
                                                                           LabelManagerMapper labelManagerMapper) {
        DefaultResourceManagerPersistence defaultResourceManagerPersistence = new DefaultResourceManagerPersistence();
        defaultResourceManagerPersistence.setLabelManagerMapper(labelManagerMapper);
        defaultResourceManagerPersistence.setNodeManagerMapper(nodeManagerMapper);
        defaultResourceManagerPersistence.setResourceManagerMapper(resourceManagerMapper);
        return defaultResourceManagerPersistence;
    }

    @Bean
    @ConditionalOnMissingBean
    public NodeMetricManagerPersistence getDefaultNodeMetricManagerPersistence(NodeManagerMapper nodeManagerMapper,
                                                                               NodeMetricManagerMapper nodeMetricManagerMapper) {
        DefaultNodeMetricManagerPersistence defaultNodeMetricManagerPersistence = new DefaultNodeMetricManagerPersistence();
        defaultNodeMetricManagerPersistence.setNodeManagerMapper(nodeManagerMapper);
        defaultNodeMetricManagerPersistence.setNodeMetricManagerMapper(nodeMetricManagerMapper);
        return defaultNodeMetricManagerPersistence;
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceLabelPersistence getDefaultResourceLabelPersistence(LabelManagerMapper labelManagerMapper,ResourceManagerMapper resourceManagerMapper) {
        DefaultResourceLabelPersistence defaultResourceLabelPersistence = new DefaultResourceLabelPersistence();
        defaultResourceLabelPersistence.setLabelManagerMapper(labelManagerMapper);
        defaultResourceLabelPersistence.setResourceManagerMapper(resourceManagerMapper);
        return defaultResourceLabelPersistence;
    }

}
