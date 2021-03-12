package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;


public class NodeLabelRemoveRequest implements LabelRequest {

    private ServiceInstance serviceInstance;

    private boolean isEngine;

    public NodeLabelRemoveRequest() {

    }

    public NodeLabelRemoveRequest(ServiceInstance serviceInstance, boolean isEngine) {
        this.serviceInstance = serviceInstance;
        this.isEngine = isEngine;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public boolean isEngine() {
        return isEngine;
    }

    public void setEngine(boolean engine) {
        isEngine = engine;
    }
}
