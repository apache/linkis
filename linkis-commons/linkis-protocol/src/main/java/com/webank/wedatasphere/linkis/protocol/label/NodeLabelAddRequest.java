package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

import java.util.Map;


public class NodeLabelAddRequest implements LabelRequest {

    private ServiceInstance serviceInstance;

    private Map<String, Object> labels;


    public NodeLabelAddRequest() {

    }

    public NodeLabelAddRequest(ServiceInstance serviceInstance, Map<String, Object> labels) {
        this.serviceInstance = serviceInstance;
        this.labels = labels;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }
}
