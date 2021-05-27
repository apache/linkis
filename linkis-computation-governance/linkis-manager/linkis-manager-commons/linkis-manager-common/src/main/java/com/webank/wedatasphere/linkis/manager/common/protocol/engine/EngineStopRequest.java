package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.protocol.message.RequestMethod;

/**
 * @author peacewong
 * @date 2020/7/3 20:38
 */
public class EngineStopRequest implements EngineRequest, RequestMethod {

    private ServiceInstance serviceInstance;

    private String user;

    public EngineStopRequest() {

    }

    public EngineStopRequest(ServiceInstance serviceInstance, String user) {
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
    public String method() {
        return "/engine/stop";
    }

    @Override
    public String toString() {
        return "EngineStopRequest{" +
                "serviceInstance=" + serviceInstance +
                ", user='" + user + '\'' +
                '}';
    }
}
