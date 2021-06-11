package com.webank.wedatasphere.linkis.common.errorcode;


public enum LinkisPublicEnhancementErrorCodeSummary {
    ;
    private int errorCode;
    private String errorDesc;
    private String comment;
    private String module;

    LinkisPublicEnhancementErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
        ErrorCodeUtils.validateErrorCode(errorCode, 15000, 19999);
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
