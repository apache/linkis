package com.webank.wedatasphere.linkis.protocol.label;

import java.util.Map;


public class LabelInsQueryRequest implements LabelRequest {

    private Map<String, Object> labels;

    public LabelInsQueryRequest() {

    }

    public LabelInsQueryRequest(Map<String, Object> labels) {
        this.labels = labels;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }
}
