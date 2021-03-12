package com.webank.wedatasphere.linkis.jobhistory.cache.domain;

public class TaskResult {

    String executionCode;
    String engineType;
    String user;
    String resultSet;
    Long createdAt;
    Long expireAt;

    public TaskResult(String executionCode, String engineType, String user, String resultSet, Long expireAfter) {
        this.executionCode = executionCode;
        this.engineType = engineType;
        this.user = user;
        this.resultSet = resultSet;
        this.createdAt = System.currentTimeMillis();
        this.expireAt = this.createdAt + expireAfter * 1000;
    }

    public String getExecutionCode() {
        return executionCode;
    }

    public void setExecutionCode(String executionCode) {
        this.executionCode = executionCode;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getResultSet() {
        return resultSet;
    }

    public void setResultSet(String resultSet) {
        this.resultSet = resultSet;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }
}
