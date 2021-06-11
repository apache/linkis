package com.webank.wedatasphere.linkis.common.errorcode;


public enum LinkisCommonsErrorCodeSummary {

    ENGINE_FAILED_STARTED(11000, "引擎启动失败", "引擎启动失败", "hiveEngineConn")

    ;
    /**
     * 错误码
     */
    private int errorCode;
    /**
     * 错误描述
     */
    private String errorDesc;
    /**
     * 错误可能出现的原因
     */
    private String comment;
    /**
     * 所属的linkis的模块
     */
    private String module;

    LinkisCommonsErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
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
