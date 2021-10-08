package com.apache.wedatasphere.linkis.manager.label.entity;

import com.apache.wedatasphere.linkis.manager.label.exception.LabelErrorException;

public interface UserModifiable {

    Boolean modifiable = true;

    Boolean getModifiable();

    void valueCheck(String stringValue) throws LabelErrorException;

}
