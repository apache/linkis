package com.webank.wedatasphere.linkis.manager.persistence;

/**
 * @Author: chaogefeng
 * @Date: 2020/7/8
 */
public interface ManagerPersistence {
    NodeManagerPersistence getNodeManagerPersistence();
    LabelManagerPersistence getLabelManagerPersistence();
    LockManagerPersistence getLockManagerPersistence();
    ResourceManagerPersistence getResourceManagerPersistence();
    NodeMetricManagerPersistence getNodeMetricManagerPersistence();
}
