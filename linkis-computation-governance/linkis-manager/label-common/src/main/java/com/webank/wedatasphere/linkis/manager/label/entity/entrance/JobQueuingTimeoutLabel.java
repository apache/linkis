package com.webank.wedatasphere.linkis.manager.label.entity.entrance;

import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;


public class JobQueuingTimeoutLabel extends GenericLabel {

    public JobQueuingTimeoutLabel() {
        setLabelKey(LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY);
    }

    public String getQueuingTimeout() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get(LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY);
    }

    @ValueSerialNum(0)
    public void setJobQueuingTimeout(String queuingTimeout) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put(LabelKeyConstant.JOB_QUEUING_TIMEOUT_KEY, queuingTimeout);
    }

    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        return true;
    }
}
