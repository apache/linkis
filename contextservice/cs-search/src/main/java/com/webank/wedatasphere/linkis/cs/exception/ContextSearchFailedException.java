package com.webank.wedatasphere.linkis.cs.exception;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;

public class ContextSearchFailedException extends ErrorException {
    public ContextSearchFailedException(int errCode, String desc) {
        super(errCode, desc);
    }
}
