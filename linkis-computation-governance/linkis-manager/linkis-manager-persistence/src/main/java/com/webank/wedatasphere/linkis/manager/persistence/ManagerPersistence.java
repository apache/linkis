package com.webank.wedatasphere.linkis.manager.persistence;


public interface ManagerPersistence {
    NodeManagerPersistence getNodeManagerPersistence();
    LabelManagerPersistence getLabelManagerPersistence();
    LockManagerPersistence getLockManagerPersistence();
    ResourceManagerPersistence getResourceManagerPersistence();
    NodeMetricManagerPersistence getNodeMetricManagerPersistence();
}
