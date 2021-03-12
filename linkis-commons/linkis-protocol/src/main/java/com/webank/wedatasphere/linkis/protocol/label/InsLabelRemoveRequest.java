package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;


public class InsLabelRemoveRequest implements LabelRequest{

    private ServiceInstance serviceInstance;

    public InsLabelRemoveRequest(){

    }

    public InsLabelRemoveRequest(ServiceInstance serviceInstance){
        this.serviceInstance = serviceInstance;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
