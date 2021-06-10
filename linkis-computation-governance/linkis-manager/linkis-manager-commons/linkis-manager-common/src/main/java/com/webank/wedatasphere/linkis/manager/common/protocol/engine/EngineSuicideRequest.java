package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.common.ServiceInstance;


public class EngineSuicideRequest implements EngineRequest {

    private ServiceInstance serviceInstance;

    private String user;

    public EngineSuicideRequest() {

    }

    public EngineSuicideRequest(ServiceInstance serviceInstance, String user) {
        this.serviceInstance = serviceInstance;
        this.user = user;
    }


    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "EngineSuicideRequest{" +
                "serviceInstance=" + serviceInstance +
                ", user='" + user + '\'' +
                '}';
    }
}
