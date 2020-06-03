package com.webank.wedatasphere.linkis.cs.highavailable.ha;

import com.webank.wedatasphere.linkis.cs.common.entity.source.HAContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

public interface ContextHAChecker {

    boolean isHAIDValid(String haIDKey);

    boolean isHAContextIDValid(HAContextID haContextID) throws CSErrorException;

    String convertHAIDToHAKey(HAContextID haContextID) throws CSErrorException;

    HAContextID parseHAIDFromKey(String haIDKey) throws CSErrorException;
}
