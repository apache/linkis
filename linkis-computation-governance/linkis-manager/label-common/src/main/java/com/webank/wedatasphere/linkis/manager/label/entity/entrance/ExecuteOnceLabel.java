package org.apache.linkis.manager.label.entity.entrance;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.Feature;
import org.apache.linkis.manager.label.entity.GenericLabel;

public class ExecuteOnceLabel extends GenericLabel {

    public ExecuteOnceLabel(){
        setLabelKey(LabelKeyConstant.EXECUTE_ONCE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

}
