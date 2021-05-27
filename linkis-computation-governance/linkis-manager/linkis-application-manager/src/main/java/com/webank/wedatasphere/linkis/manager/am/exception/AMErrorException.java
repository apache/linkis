package com.webank.wedatasphere.linkis.manager.am.exception;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;

/**
 * @author peacewong
 * @date 2020/7/6 17:26
 */
public class AMErrorException extends ErrorException {

    public AMErrorException(int errCode, String desc) {
        super(errCode, desc);
    }

    public AMErrorException(int errCode, String desc, Throwable t) {
        this(errCode, desc);
        this.initCause(t);
    }
}
