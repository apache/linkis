package com.webank.wedatasphere.linkis.manager.label.entity;

import com.webank.wedatasphere.linkis.manager.label.constant.LabelConstant;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;
import com.webank.wedatasphere.linkis.manager.label.exception.LabelErrorException;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class TenantLabel extends GenericLabel implements EMNodeLabel, EngineNodeLabel, UserModifiable {

    public TenantLabel() {
        setLabelKey(LabelKeyConstant.TENANT_KEY);
    }

    @ValueSerialNum(0)
    public void setTenant(String tenant){
        if (getValue() == null) {
            setValue(new HashMap<>());
        }
        getValue().put(getLabelKey(), tenant);
    }

    public String getTenant(){
        if(getValue() == null){
            return null;
        }
        return getValue().get(getLabelKey());
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

    @Override
    public Boolean getModifiable() {
        return true;
    }

    @Override
    public void valueCheck(String stringValue) throws LabelErrorException {
        if(!StringUtils.isEmpty(stringValue)){
            if(stringValue.split(SerializableLabel.VALUE_SEPARATOR).length != 1){
                throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE,
                        "标签route的值设置错误，只能设置1个值，并且不能使用符号" + VALUE_SEPARATOR);
            }
        }
    }
}
