package com.webank.wedatasphere.linkis.manager.label.entity.engine;

import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.Feature;
import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class ReuseExclusionLabel extends GenericLabel {

    public ReuseExclusionLabel(){
        setLabelKey(LabelKeyConstant.REUSE_EXCLUSION_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.OPTIONAL;
    }

    public String[] getInstances() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get("instances").split(";");
    }

    @ValueSerialNum(0)
    public ReuseExclusionLabel setInstances(String[] instances) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("instances", StringUtils.join(instances,";"));
        return this;
    }
}
