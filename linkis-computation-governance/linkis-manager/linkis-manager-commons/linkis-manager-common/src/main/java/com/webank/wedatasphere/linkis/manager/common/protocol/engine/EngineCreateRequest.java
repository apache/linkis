package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.protocol.message.RequestMethod;

import java.util.Map;

/**
 * @author peacewong
 * @date 2020/6/10 17:18
 */
public class EngineCreateRequest implements EngineRequest, RequestMethod {

    private Map<String, String> properties;

    private Map<String, Object> labels;

    private long timeOut;

    private String user;

    private String createService;

    private String description;

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

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

    @Override
    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCreateService() {
        return createService;
    }

    public void setCreateService(String createService) {
        this.createService = createService;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String method() {
        return "/engine/create";
    }

    @Override
    public String toString() {
        return "EngineCreateRequest{" +
                "labels=" + labels +
                ", timeOut=" + timeOut +
                ", user='" + user + '\'' +
                ", createService='" + createService + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
