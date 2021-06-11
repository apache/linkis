package com.webank.wedatasphere.linkis.common.errorcode;


public enum LinkisFrameErrorCodeSummary {

    VALIDATE_ERROR_CODE_FAILED
            (10000, "错误码定义有误", "错误码定义超过最大值或者小于最小值", "linkis-frame")

    ;

    private int errorCode;

    private String errorDesc;

    private String comment;

    private String module;

    LinkisFrameErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
        ErrorCodeUtils.validateErrorCode(errorCode, 10000, 10999);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.comment = comment;
        this.module = module;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }


    @Override
    public String toString() {
        return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
    }
}
