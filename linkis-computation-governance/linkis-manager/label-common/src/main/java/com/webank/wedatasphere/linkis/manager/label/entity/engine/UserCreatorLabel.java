package com.webank.wedatasphere.linkis.manager.label.entity.engine;

import com.webank.wedatasphere.linkis.manager.label.constant.LabelConstant;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.*;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;
import com.webank.wedatasphere.linkis.manager.label.exception.LabelErrorException;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;


public class UserCreatorLabel extends GenericLabel implements EngineNodeLabel, UserModifiable {

    public UserCreatorLabel() {
        setLabelKey(LabelKeyConstant.USER_CREATOR_TYPE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.CORE;
    }

    @ValueSerialNum(0)
    public void setUser(String user) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("user", user);
    }

    @ValueSerialNum(1)
    public void setCreator(String creator) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("creator", creator);
    }

    public String getUser() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get("user");
    }

    public String getCreator() {
        if (null == getValue()) {
            return null;
        }
        return getValue().get("creator");
    }

    @Override
    public Boolean getModifiable() {
        return modifiable;
    }

    @Override
    public void valueCheck(String stringValue) throws LabelErrorException {
        if(!StringUtils.isEmpty(stringValue)){
            if(stringValue.split(SerializableLabel.VALUE_SEPARATOR).length != 2){
                throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE,
                        "标签usercreator的值设置错误，需要2个值，并且需要使用"+VALUE_SEPARATOR+"隔开");
            }
        }
    }
}
