package com.webank.wedatasphere.linkis.manager.exception;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;

/**
 * @Author: chaogefeng
 * @Date: 2020/7/9
 */
public class PersistenceErrorException  extends ErrorException  {
    public PersistenceErrorException(int errCode, String desc) {
        super(errCode, desc);
    }
    public PersistenceErrorException(int errCode, String desc, Throwable e){
        super(errCode,desc);
        this.initCause(e);
    }
}
