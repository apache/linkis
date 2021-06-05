package com.webank.wedatasphere.linkis.resourcemanager.protocol;

import com.webank.wedatasphere.linkis.manager.common.protocol.em.EMRequest;

public class TimeoutEMEngineRequest implements EMRequest {

    private String user;
    private String ticketId;

    @Override
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
}
