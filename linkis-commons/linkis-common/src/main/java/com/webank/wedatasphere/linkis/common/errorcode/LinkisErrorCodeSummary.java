package com.webank.wedatasphere.linkis.common.errorcode;


public enum LinkisErrorCodeSummary {

    /**
     * 10000-10999 linkis-frame
     * 11000-12999 linkis-commons
     * 13000-14999 linkis-spring-cloud-services
     * 15000-19999 linkis-public-enhancements
     * 20000-24999 linkis-computation-governance
     * 25000-25999 linkis-extensions
     * 26000-29999 linkis-engineconn-plugins
     */
    EngineManagerErrorException
            (321, "引擎启动失败", "在某种情况下启动失败", "cooperyang", "EngineConnManager")
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
     * 评论
     */
    private String comment;

    /**
     * errorCode的创建人
     */
    private String creator;
    /**
     * 所属的linkis的模块
     */
    private String module;

    LinkisErrorCodeSummary(int errorCode, String errorDesc, String comment, String creator, String module) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.comment = comment;
        this.creator = creator;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
