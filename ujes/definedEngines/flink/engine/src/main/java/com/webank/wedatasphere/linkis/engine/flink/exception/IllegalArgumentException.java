package com.webank.wedatasphere.linkis.engine.flink.exception;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.exception.WarnException;


public class IllegalArgumentException extends ErrorException {

    public IllegalArgumentException(int errCode, String desc) {
        super(errCode, desc);
    }

    public IllegalArgumentException(String desc) {
        super(20000, desc);
    }

    public IllegalArgumentException(Exception e) {
        super(20000, e.getMessage());
    }

    public IllegalArgumentException() {
        super(20000, "argument illegal");
    }

    public IllegalArgumentException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }

}
