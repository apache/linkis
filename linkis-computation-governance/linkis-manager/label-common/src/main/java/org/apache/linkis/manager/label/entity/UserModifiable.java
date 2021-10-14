package org.apache.linkis.manager.label.entity;

import org.apache.linkis.manager.label.exception.LabelErrorException;

public interface UserModifiable {

    Boolean modifiable = true;

    Boolean getModifiable();

    void valueCheck(String stringValue) throws LabelErrorException;

}
