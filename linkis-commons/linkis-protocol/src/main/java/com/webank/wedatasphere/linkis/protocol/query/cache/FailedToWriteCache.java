package com.webank.wedatasphere.linkis.protocol.query.cache;

public class FailedToWriteCache implements ResponseWriteCache {

    private String errorMessage;

    public FailedToWriteCache(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
