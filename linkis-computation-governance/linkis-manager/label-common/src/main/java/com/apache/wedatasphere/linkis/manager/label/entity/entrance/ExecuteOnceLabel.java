package com.apache.wedatasphere.linkis.manager.label.entity.entrance;

import com.apache.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.apache.wedatasphere.linkis.manager.label.entity.Feature;
import com.apache.wedatasphere.linkis.manager.label.entity.GenericLabel;

public class ExecuteOnceLabel extends GenericLabel {

    public ExecuteOnceLabel(){
        setLabelKey(LabelKeyConstant.EXECUTE_ONCE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

}
