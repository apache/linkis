package org.apache.linkis.engineconnplugin.datax.client.exception;


import org.apache.linkis.common.exception.ErrorException;

public class JobExecutionException extends ErrorException {
    private static final long serialVersionUID = 1L;

    public static final int ERROR_CODE = 16023;

    public JobExecutionException(String message) {
        super(ERROR_CODE, message);
    }

    public JobExecutionException(String message, Throwable e) {
        super(ERROR_CODE, message);
        this.initCause(e);
    }
}
