package org.apache.linkis.manager.exception;

import org.apache.linkis.common.exception.ErrorException;


public class PersistenceErrorException  extends ErrorException  {
    public PersistenceErrorException(int errCode, String desc) {
        super(errCode, desc);
    }
    public PersistenceErrorException(int errCode, String desc, Throwable e){
        super(errCode,desc);
        this.initCause(e);
    }
}
