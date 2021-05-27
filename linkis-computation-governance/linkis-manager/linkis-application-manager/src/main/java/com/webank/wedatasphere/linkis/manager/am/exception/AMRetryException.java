package com.webank.wedatasphere.linkis.manager.am.exception;

import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException;

/**
 * @author peacewong
 * @date 2020/7/2 21:29
 */
public class AMRetryException extends LinkisRetryException {

    public AMRetryException(int errCode, String desc) {
        super(errCode, desc);
    }

    public AMRetryException(int errCode, String desc, Throwable t) {
        this(errCode, desc);
        initCause(t);
    }

}
