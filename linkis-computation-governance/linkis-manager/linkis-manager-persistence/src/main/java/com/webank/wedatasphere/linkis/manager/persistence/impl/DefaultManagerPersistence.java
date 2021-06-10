package com.webank.wedatasphere.linkis.manager.persistence.impl;

import com.webank.wedatasphere.linkis.manager.persistence.*;


public class DefaultManagerPersistence implements ManagerPersistence {

    private NodeManagerPersistence nodeManagerPersistence;
    private LabelManagerPersistence labelManagerPersistence;
    private LockManagerPersistence lockManagerPersistence;
    private ResourceManagerPersistence resourceManagerPersistence;
    private NodeMetricManagerPersistence nodeMetricManagerPersistence;
    private ResourceLabelPersistence resourceLabelPersistence;

    @Override
    public NodeMetricManagerPersistence getNodeMetricManagerPersistence() {
        return nodeMetricManagerPersistence;
    }

    public void setNodeMetricManagerPersistence(NodeMetricManagerPersistence nodeMetricManagerPersistence) {
        this.nodeMetricManagerPersistence = nodeMetricManagerPersistence;
    }

    @Override
    public ResourceManagerPersistence getResourceManagerPersistence() {
        return resourceManagerPersistence;
    }

    public void setResourceManagerPersistence(ResourceManagerPersistence resourceManagerPersistence) {
        this.resourceManagerPersistence = resourceManagerPersistence;
    }



    @Override
    public NodeManagerPersistence getNodeManagerPersistence() {
        return nodeManagerPersistence;
    }

    public void setNodeManagerPersistence(NodeManagerPersistence nodeManagerPersistence) {
        this.nodeManagerPersistence = nodeManagerPersistence;
    }

    @Override
    public LabelManagerPersistence getLabelManagerPersistence() {
        return labelManagerPersistence;
    }

    public void setLabelManagerPersistence(LabelManagerPersistence labelManagerPersistence) {
        this.labelManagerPersistence = labelManagerPersistence;
    }

    @Override
    public LockManagerPersistence getLockManagerPersistence() {
        return lockManagerPersistence;
    }

    public void setLockManagerPersistence(LockManagerPersistence lockManagerPersistence) {
        this.lockManagerPersistence = lockManagerPersistence;
    }

    public ResourceLabelPersistence getResourceLabelPersistence() {
        return resourceLabelPersistence;
    }

    public void setResourceLabelPersistence(ResourceLabelPersistence resourceLabelPersistence) {
        this.resourceLabelPersistence = resourceLabelPersistence;
    }
}
