package org.apache.linkis.manager.exception;

import org.apache.linkis.common.exception.WarnException;


public class PersistenceWarnException extends WarnException {
    public PersistenceWarnException(int errCode, String desc) {
        super(errCode, desc);
    }

    public PersistenceWarnException(int errCode, String desc, Throwable e) {
        super(errCode, desc);
        this.initCause(e);
    }
}
