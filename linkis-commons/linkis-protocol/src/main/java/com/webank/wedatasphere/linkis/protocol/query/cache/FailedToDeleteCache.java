package com.webank.wedatasphere.linkis.protocol.query.cache;

public class FailedToDeleteCache {
    private String errorMessage;

    public FailedToDeleteCache(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
