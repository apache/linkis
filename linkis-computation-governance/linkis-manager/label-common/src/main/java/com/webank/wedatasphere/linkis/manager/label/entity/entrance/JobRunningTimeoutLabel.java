package org.apache.linkis.manager.label.entity.entrance;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;


public class JobRunningTimeoutLabel extends GenericLabel {

    public JobRunningTimeoutLabel() {
        setLabelKey(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY);
    }

    public String getRunningTimeout() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY);
    }

    @ValueSerialNum(0)
    public void setJobRunningTimeout(String runningTimeout) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put(LabelKeyConstant.JOB_RUNNING_TIMEOUT_KEY, runningTimeout);
    }

    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        return true;
    }
}
