package com.webank.wedatasphere.linkis.manager.am.exception;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;


public class AMErrorException extends ErrorException {

    public AMErrorException(int errCode, String desc) {
        super(errCode, desc);
    }

    public AMErrorException(int errCode, String desc, Throwable t) {
        this(errCode, desc);
        this.initCause(t);
    }
}
