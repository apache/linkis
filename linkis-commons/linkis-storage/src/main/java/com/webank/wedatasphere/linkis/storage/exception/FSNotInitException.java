package com.webank.wedatasphere.linkis.storage.exception;

import com.webank.wedatasphere.linkis.common.exception.WarnException;


public class FSNotInitException extends WarnException {

    public FSNotInitException(){
        super(52000, "FSNotInitException");
    }

    public FSNotInitException(int errCode, String desc) {
        super(errCode, desc);
    }
}
