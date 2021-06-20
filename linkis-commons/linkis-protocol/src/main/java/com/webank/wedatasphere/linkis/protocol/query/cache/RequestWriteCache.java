package com.webank.wedatasphere.linkis.protocol.query.cache;

import com.webank.wedatasphere.linkis.protocol.query.QueryProtocol;

import java.util.List;

public class RequestWriteCache implements QueryProtocol {
    private String executionContent;
    private String user;
    private Long cacheExpireAfter;
    private List<String> labelsStr;
    private String resultSet;

    public RequestWriteCache(String executionContent, String user, Long cacheExpireAfter, List<String> labelsStr, String resultSet) {
        this.executionContent = executionContent;
        this.user = user;
        this.cacheExpireAfter = cacheExpireAfter;
        this.labelsStr = labelsStr;
        this.resultSet = resultSet;
    }

    public String getExecutionContent() {
        return executionContent;
    }

    public String getUser() {
        return user;
    }

    public Long getCacheExpireAfter() {
        return cacheExpireAfter;
    }

    public List<String> getLabelsStr() {
        return labelsStr;
    }

    public String getResultSet() {
        return resultSet;
    }
}
