package org.apache.linkis.gateway.authentication.exception;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.exception.ExceptionLevel;


public class TokenAuthException extends ErrorException {
    public TokenAuthException(int errCode, String desc) {
        super(errCode, desc);
    }

    public TokenAuthException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }

    @Override
    public ExceptionLevel getLevel() {
        return super.getLevel();
    }
}
