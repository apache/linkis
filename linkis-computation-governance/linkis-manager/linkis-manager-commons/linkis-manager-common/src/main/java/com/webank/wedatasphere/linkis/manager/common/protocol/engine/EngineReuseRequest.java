package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import java.util.Map;


public class EngineReuseRequest implements EngineRequest {

    private Map<String, Object> labels;

    private long timeOut;

    private int reuseCount;

    private String user;

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public int getReuseCount() {
        return reuseCount;
    }

    public void setReuseCount(int reuseCount) {
        this.reuseCount = reuseCount;
    }

    @Override
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "EngineReuseRequest{" +
                "timeOut=" + timeOut +
                ", reuseCount=" + reuseCount +
                ", user='" + user + '\'' +
                '}';
    }
}
