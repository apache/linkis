package com.webank.wedatasphere.linkis.resourcemanager.protocol;

public class TimeoutEMEngineResponse {
    private String ticketId;
    private Boolean canReleaseResource;
    private Long nextAskInterval;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Boolean getCanReleaseResource() {
        return canReleaseResource;
    }

    public void setCanReleaseResource(Boolean canReleaseResource) {
        this.canReleaseResource = canReleaseResource;
    }

    public Long getNextAskInterval() {
        return nextAskInterval;
    }

    public void setNextAskInterval(Long nextAskInterval) {
        this.nextAskInterval = nextAskInterval;
    }
}
