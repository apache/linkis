package com.webank.wedatasphere.linkis.protocol.query.cache;

import com.webank.wedatasphere.linkis.protocol.query.QueryProtocol;

public class RequestDeleteCache implements QueryProtocol {

    private String executionCode;
    private String engineType;
    private String user;

    public RequestDeleteCache(String executionCode, String engineType, String user) {
        this.executionCode = executionCode;
        this.engineType = engineType;
        this.user = user;
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
}
