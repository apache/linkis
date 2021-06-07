package com.webank.wedatasphere.linkis.manager.common.protocol.em;

import com.webank.wedatasphere.linkis.manager.common.entity.node.EMNode;


public class EMInfoClearRequest implements EMRequest {

    private String user;

    private EMNode em;

    @Override
    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public EMNode getEm() {
        return em;
    }

    public void setEm(EMNode em) {
        this.em = em;
    }
}