package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;


public class InsLabelQueryRequest implements LabelRequest{

    /**
     * Service instance
     */
    private ServiceInstance serviceInstance;

    public InsLabelQueryRequest(ServiceInstance serviceInstance)  {
        this.serviceInstance = serviceInstance;
    }

    public InsLabelQueryRequest() {}

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
