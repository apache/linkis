package com.webank.wedatasphere.linkis.protocol.query.cache;

import com.webank.wedatasphere.linkis.protocol.query.QueryProtocol;

import java.util.List;

public class RequestReadCache implements QueryProtocol {
    private String executionContent;
    private String user;
    private Long readCacheBefore;
    private List<String> labelsStr;

    public RequestReadCache(String executionContent, String user, List<String> labelsStr, Long readCacheBefore) {
        this.executionContent = executionContent;
        this.user = user;
        this.labelsStr = labelsStr;
        this.readCacheBefore = readCacheBefore;
    }

    public String getExecutionContent() {
        return executionContent;
    }

    public String getUser() {
        return user;
    }

    public Long getReadCacheBefore() {
        return readCacheBefore;
    }

    public List<String> getLabelsStr() {
        return labelsStr;
    }

}
