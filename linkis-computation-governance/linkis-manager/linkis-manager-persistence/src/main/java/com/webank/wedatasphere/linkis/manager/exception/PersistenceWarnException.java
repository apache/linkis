package com.webank.wedatasphere.linkis.manager.exception;

import com.webank.wedatasphere.linkis.common.exception.WarnException;

/**
 * created by v_wbjftang on 2020/8/25
 */
public class PersistenceWarnException extends WarnException {
    public PersistenceWarnException(int errCode, String desc) {
        super(errCode, desc);
    }

    public PersistenceWarnException(int errCode, String desc, Throwable e) {
        super(errCode, desc);
        this.initCause(e);
    }
}
