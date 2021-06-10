package com.webank.wedatasphere.linkis.manager.common.protocol.em;

import com.webank.wedatasphere.linkis.common.ServiceInstance;


public class GetEMInfoRequest implements EMRequest {

    private String user;

    private ServiceInstance em;

    @Override
    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ServiceInstance getEm() {
        return em;
    }

    public void setEm(ServiceInstance em) {
        this.em = em;
    }
}
