package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

import java.util.HashMap;
import java.util.Map;


public class InsLabelAttachRequest implements LabelRequest {
    /**
     * Service instance
     */
    private ServiceInstance serviceInstance;

    /**
     * Labels stored as map structure
     */
    private Map<String, Object> labels = new HashMap<>();


    public InsLabelAttachRequest(){

    }

    public InsLabelAttachRequest(ServiceInstance serviceInstance, Map<String, Object> labels){
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
