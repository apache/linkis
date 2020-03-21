package com.webank.wedatasphere.linkis.engine.impala.client.exception;

/**
 * 请求提交错误
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class SubmitException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5045965784519089392L;

    public SubmitException(String message) {
        super(message);
    }

    public SubmitException(Exception exception) {
        super(exception);
    }

    public static SubmitException of(ExceptionCode code) {
        return new SubmitException(code.getMessage());
    }

    public static SubmitException of(ExceptionCode code, String massage) {
        return new SubmitException(String.format("%s: %s", code.getMessage(), massage));
    }

    public static SubmitException of(Exception exception) {
        return new SubmitException(exception);
    }
}
