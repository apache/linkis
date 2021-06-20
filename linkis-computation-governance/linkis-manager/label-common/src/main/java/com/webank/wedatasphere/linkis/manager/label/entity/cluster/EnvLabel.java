package com.webank.wedatasphere.linkis.manager.label.entity.cluster;

import static com.webank.wedatasphere.linkis.manager.label.constant.LabelConstant.LABEL_BUILDER_ERROR_CODE;
import static com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant.ENV_TYPE_KEY;

import com.webank.wedatasphere.linkis.manager.label.entity.Feature;
import com.webank.wedatasphere.linkis.manager.label.entity.GenericLabel;
import com.webank.wedatasphere.linkis.manager.label.exception.LabelRuntimeException;
import java.util.HashMap;


public class EnvLabel extends GenericLabel {

    public static final String DEV = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";

    public EnvLabel() {
        setLabelKey(ENV_TYPE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

    public void setEnvType(String envType) {
        if(!envType.equals(DEV) && !envType.equals(TEST) && !envType.equals(PROD)) {
            throw new LabelRuntimeException(LABEL_BUILDER_ERROR_CODE, "Not support envType: " + envType);
        }
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put(ENV_TYPE_KEY, envType);
    }

    public String getEnvType() {
        if (getValue() != null && null != getValue().get(ENV_TYPE_KEY)) {
            return getValue().get(ENV_TYPE_KEY);
        }
        return null;
    }
}
