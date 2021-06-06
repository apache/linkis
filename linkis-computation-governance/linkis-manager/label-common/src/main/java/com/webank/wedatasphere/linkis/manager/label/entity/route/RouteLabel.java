package com.webank.wedatasphere.linkis.manager.label.entity.route;

import com.webank.wedatasphere.linkis.manager.label.constant.LabelConstant;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.InheritableLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.SerializableLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.UserModifiable;
import com.webank.wedatasphere.linkis.manager.label.entity.annon.ValueSerialNum;
import com.webank.wedatasphere.linkis.manager.label.exception.LabelErrorException;
import org.apache.commons.lang.StringUtils;


public class RouteLabel extends InheritableLabel<String> implements UserModifiable {


    public RouteLabel(){
        setLabelKey(LabelKeyConstant.ROUTE_KEY);
    }

    @ValueSerialNum(0)
    public void setRoutePath(String value){
        super.setValue(value);
    }

    @Override
    public Boolean getModifiable() {
        return modifiable;
    }

    @Override
    public void valueCheck(String stringValue) throws LabelErrorException {
        if(!StringUtils.isEmpty(stringValue)){
            if(stringValue.split(SerializableLabel.VALUE_SEPARATOR).length != 1){
                throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE,
                        "标签route的值设置错误，只能设置1个值，并且不能使用符号"+VALUE_SEPARATOR);
            }
        }
    }
    @Override
    public String getStringValue() {
        return getValue();
    }

    @Override
    protected void setStringValue(String stringValue) {
        setRoutePath(stringValue);
    }
}
