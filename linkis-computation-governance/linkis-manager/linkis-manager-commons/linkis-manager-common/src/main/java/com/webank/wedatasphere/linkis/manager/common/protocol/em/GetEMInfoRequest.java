package com.webank.wedatasphere.linkis.manager.common.protocol.em;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

/**
 * @author peacewong
 * @date 2020/6/10 17:24
 */
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
