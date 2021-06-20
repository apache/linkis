package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

import java.util.List;


public class LabelInsQueryResponse {

    private List<ServiceInstance> insList;

    public LabelInsQueryResponse() {}

    public LabelInsQueryResponse(List<ServiceInstance> insList) {
        this.insList = insList;
    }

    public List<ServiceInstance> getInsList() {
        return insList;
    }

    public LabelInsQueryResponse setInsList(List<ServiceInstance> insList) {
        this.insList = insList;
        return this;
    }
}
