package org.apache.linkis.manager.label.entity.entrance;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;

public class RetryCountLabel extends GenericLabel implements JobStrategyLabel {

    public RetryCountLabel() {
        setLabelKey(LabelKeyConstant.RETRY_COUNT_KEY);
    }

    public Integer getJobRetryCount() {
        if (null == getValue()) {
            return -1;
        }
        return Integer.parseInt(getValue().getOrDefault(LabelKeyConstant.RETRY_COUNT_KEY, "-1"));
    }

    @ValueSerialNum(0)
    public void setJobRetryCount(String count) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put(LabelKeyConstant.RETRY_COUNT_KEY, count);
    }
}
