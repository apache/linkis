package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

import java.util.Map;


public class InsLabelRefreshRequest extends InsLabelAttachRequest{

    public InsLabelRefreshRequest(){

    }

    public InsLabelRefreshRequest(ServiceInstance serviceInstance, Map<String, Object> labels){
        super(serviceInstance, labels);
    }
}
