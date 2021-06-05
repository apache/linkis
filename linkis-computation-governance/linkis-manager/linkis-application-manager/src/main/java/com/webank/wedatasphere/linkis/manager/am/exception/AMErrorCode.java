package com.webank.wedatasphere.linkis.manager.am.exception;



public enum AMErrorCode {

    QUERY_PARAM_NULL(21001,"query param cannot be null(请求参数不能为空)"),

    UNSUPPORT_VALUE(21002,"unsupport value(不支持的值类型)"),

    PARAM_ERROR(210003,"param error(参数错误)")

    ;


    AMErrorCode(int errorCode, String message) {
    }

    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
