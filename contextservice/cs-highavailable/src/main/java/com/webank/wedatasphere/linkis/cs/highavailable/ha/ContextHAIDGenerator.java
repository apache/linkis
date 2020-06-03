package com.webank.wedatasphere.linkis.cs.highavailable.ha;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.HAContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

public interface ContextHAIDGenerator {

    HAContextID generateHAContextID(ContextID contextID) throws CSErrorException;

}
