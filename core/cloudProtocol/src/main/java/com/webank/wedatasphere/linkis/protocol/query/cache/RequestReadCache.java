package com.webank.wedatasphere.linkis.protocol.query.cache;

import com.webank.wedatasphere.linkis.protocol.query.QueryProtocol;

public class RequestReadCache implements QueryProtocol {
    private String executionCode;
    private String engineType;
    private String user;
    private Long readCacheBefore;

    public RequestReadCache(String executionCode, String engineType, String user, Long readCacheBefore) {
        this.executionCode = executionCode;
        this.engineType = engineType;
        this.user = user;
        this.readCacheBefore = readCacheBefore;
    }

    public String getExecutionCode() {
        return executionCode;
    }

    public String getEngineType() {
        return engineType;
    }

    public String getUser() {
        return user;
    }

    public Long getReadCacheBefore() {
        return readCacheBefore;
    }
}
