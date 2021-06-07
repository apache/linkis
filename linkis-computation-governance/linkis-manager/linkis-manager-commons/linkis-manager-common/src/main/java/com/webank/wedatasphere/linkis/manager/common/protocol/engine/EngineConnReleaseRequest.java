package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.common.ServiceInstance;


public class EngineConnReleaseRequest implements EngineRequest {


    private ServiceInstance serviceInstance;

    private String user;

    private String msg;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    private String ticketId;

    public EngineConnReleaseRequest() {

    }

    public EngineConnReleaseRequest(ServiceInstance serviceInstance, String user, String msg, String ticketId) {
        this.serviceInstance = serviceInstance;
        this.user = user;
        this.msg = msg;
        this.ticketId = ticketId;
    }


    @Override
    public String getUser() {
        return this.user;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
